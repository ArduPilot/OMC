/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

#include <jni.h>
#include <iostream>
#include <string>
#include <regex>
#include <vector>
#include <algorithm>
#include <Windows.h>
#include "tinyxml2.h"
#include "resource/resource.h"

extern "C"
{
	__declspec(dllexport) DWORD NvOptimusEnablement = 1;
	__declspec(dllexport) int AmdPowerXpressRequestHighPerformance = 1;
}

namespace
{
	std::string format_error_message(DWORD error)
	{
		LPSTR msg = nullptr;
		auto size = FormatMessage(
			FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
			NULL, error, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPSTR)&msg, 0, nullptr);

		std::string message(msg, size);
		LocalFree(msg);
		return message;
	}

	const char* jni_err_name(int err) {
		switch (err) {
		case JNI_ERR: return "JNI_ERR";
		case JNI_EDETACHED: return "JNI_EDETACHED";
		case JNI_EVERSION: return "JNI_EVERSION";
		case JNI_ENOMEM: return "JNI_ENOMEM";
		case JNI_EEXIST: return "JNI_EEXIST";
		case JNI_EINVAL: return "JNI_EINVAL";
		default: return "JNI_OK";
		}
	}

	jclass jni_class(JNIEnv* env, std::string name)
	{
		std::replace(name.begin(), name.end(), '.', '/');
		jclass jcls = env->FindClass(name.c_str());
		if (jcls == nullptr) {
			throw std::runtime_error(name + " not found");
		}

		return jcls;
	}

	jmethodID jni_main_method(JNIEnv* env, jclass jcls, std::string const& class_name)
	{
		jmethodID methodId = env->GetStaticMethodID(jcls, "main", "([Ljava/lang/String;)V");
		if (methodId == nullptr) {
			throw std::runtime_error(class_name + ".main not found");
		}

		return methodId;
	}

	tinyxml2::XMLElement& first_child_element(tinyxml2::XMLNode& node, const char* name)
	{
		auto element = node.FirstChildElement(name);
		if (element == nullptr) {
			throw std::runtime_error(std::string("Element not found: ") + name);
		}

		return *element;
	}

	std::vector<tinyxml2::XMLElement*> all_children_elements(tinyxml2::XMLNode& node, const char* name)
	{
		std::vector<tinyxml2::XMLElement*> result;
		auto element = node.FirstChildElement(name);
		if (element == nullptr) {
			throw std::runtime_error(std::string("Element not found: ") + name);
		}

		result.push_back(element);
		while ((element = element->NextSiblingElement(name)) != nullptr) {
			result.push_back(element);
		}

		return result;
	}

	std::string to_utf8(std::wstring const& str)
	{
		std::string result;
		int count = WideCharToMultiByte(CP_UTF8, 0, str.c_str(), int(str.length()), nullptr, 0, nullptr, nullptr);
		result.resize(count);
		WideCharToMultiByte(CP_UTF8, 0, str.c_str(), int(str.length()), &result[0], count, nullptr, nullptr);
		return result;
	}

	std::wstring to_utf16(std::string const& str)
	{
		std::wstring result;
		int count = MultiByteToWideChar(CP_UTF8, 0, str.c_str(), int(str.length()), nullptr, 0);
		result.resize(count);
		MultiByteToWideChar(CP_UTF8, 0, str.c_str(), int(str.length()), &result[0], count);
		return result;
	}

	template<class String>
	String to_forward_slashes(String const& str)
	{
		String result = str;
		std::replace(result.begin(), result.end(), '\\', '/');
		return result;
	}

	template<class String>
	String to_back_slashes(String const& str)
	{
		String result = str;
		std::replace(result.begin(), result.end(), '/', '\\');
		return result;
	}

	std::wstring const& process_directory_name()
	{
		struct data_t {
			data_t() {
				constexpr int max_length = 4096;
				wchar_t buf[max_length];
				GetModuleFileNameW(nullptr, buf, max_length);
				wchar_t drive[max_length], dir[max_length];
				_wsplitpath_s(buf, drive, max_length, dir, max_length, nullptr, 0, nullptr, 0);
				std::wstring result = std::wstring(drive) + std::wstring(dir);
				value = to_forward_slashes(result);
			}

			std::wstring value;
		} static const data;

		return data.value;
	}

	std::wstring const& process_file_name()
	{
		struct data_t {
			data_t() {
				constexpr int max_length = 4096;
				wchar_t buf[max_length];
				GetModuleFileNameW(nullptr, buf, max_length);
				std::wstring path = to_forward_slashes(std::wstring(buf));

				size_t i = path.find_last_of('/');
				if (std::string::npos != i) {
					path.erase(0, i + 1);
				}

				i = path.rfind('.');
				if (std::string::npos != i) {
					path.erase(i);
				}

				value = path;
			}

			std::wstring value;
		} static const data;

		return data.value;
	}

	std::wstring expand_variables(std::wstring const& str)
	{
		static std::wregex procdir(L"%PROCDIR%");
		static std::wregex procname(L"%PROCNAME%");
		std::wstring result = str;
		result = std::regex_replace(result, procdir, process_directory_name());
		result = std::regex_replace(result, procname, process_file_name());
		return result;
	}

	std::vector<std::string> parse_command_line(LPWSTR lpCmdLine)
	{
		std::vector<std::string> args;
		int num_args;
		LPWSTR* wargs = CommandLineToArgvW(lpCmdLine, &num_args);
		args.reserve(num_args);
		for (int i = 0; i < num_args; ++i) {
			args.push_back(to_utf8(wargs[i]));
		}

		return args;
	}

	jobjectArray to_java_array(JNIEnv* env, std::vector<std::string> vec)
	{
		jclass jstring = env->FindClass("java/lang/String");
		jobjectArray array = env->NewObjectArray(jsize(vec.size()), jstring, nullptr);
		for (std::size_t i = 0; i < vec.size(); ++i) {
			env->SetObjectArrayElement(array, jsize(i), env->NewStringUTF(vec[i].c_str()));
		}

		return array;
	}

	std::string format_stack_trace(JNIEnv* env, jthrowable ex) {
		jboolean isCopy = false;
		jclass stringWriterClass = env->FindClass("java/io/StringWriter");
		jclass printWriterClass = env->FindClass("java/io/PrintWriter");
		jmethodID stringWriterCtor = env->GetMethodID(stringWriterClass, "<init>", "()V");
		jobject stringWriter = env->NewObject(stringWriterClass, stringWriterCtor);
		jmethodID printWriterCtor = env->GetMethodID(printWriterClass, "<init>", "(Ljava/io/Writer;)V");
		jobject printWriter = env->NewObject(printWriterClass, printWriterCtor, stringWriter);
		jclass throwableClass = env->FindClass("java/lang/Throwable");
		jmethodID printStackTrace = env->GetMethodID(throwableClass, "printStackTrace", "(Ljava/io/PrintWriter;)V");
		env->CallVoidMethod(ex, printStackTrace, printWriter);
		jmethodID toString = env->GetMethodID(env->FindClass("java/lang/Object"), "toString", "()Ljava/lang/String;");
		jstring jmsg = (jstring)env->CallObjectMethod(stringWriter, toString);
		const char* cmsg = env->GetStringUTFChars(jmsg, &isCopy);
		std::string msg(cmsg);
		env->ReleaseStringUTFChars(jmsg, cmsg);
		return std::move(msg);
	}
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow)
{
	JavaVM* javaVM = nullptr;
	JNIEnv* env = nullptr;

	try
	{
#if defined(_MSC_VER)
#	pragma warning(push)
#	pragma warning(disable: 4302)
#endif
		HRSRC config_res = FindResourceW(nullptr, MAKEINTRESOURCEW(IDR_CONFIG_XML), MAKEINTRESOURCEW(RT_HTML));
#if defined(_MSV_VER)
#	pragma warning(pop)
#endif
		
		HGLOBAL config_file = LoadResource(nullptr, config_res);
		if (config_file == nullptr) {
			throw std::runtime_error("LoadResource: " + format_error_message(GetLastError()));
		}
		
		DWORD config_data_size = SizeofResource(nullptr, config_res);
		const char* config_data = reinterpret_cast<const char*>(LockResource(config_file));

		tinyxml2::XMLDocument doc;
		doc.Parse(config_data, config_data_size);

		auto opts = all_children_elements(first_child_element(*doc.RootElement(), "vm"), "opt");
		std::vector<std::string> vm_options;
		std::transform(
			opts.begin(),
			opts.end(),
			std::back_inserter(vm_options),
			[](tinyxml2::XMLElement* e) { return e->GetText(); });

		auto vars = all_children_elements(first_child_element(*doc.RootElement(), "env"), "var");
		std::vector<std::pair<std::string, std::string>> env_vars;
		std::transform(
			vars.begin(),
			vars.end(),
			std::back_inserter(env_vars),
			[](tinyxml2::XMLElement* e) { return std::make_pair(e->Attribute("name"), e->GetText()); });

		constexpr int max_env_path_size = 1024 * 32;
		wchar_t env_path[max_env_path_size];

		for (auto const& env_var : env_vars) {
			memset(env_path, 0, max_env_path_size);
			auto varname = to_utf16(env_var.first);
			auto varvalue = to_utf16(env_var.second);
			GetEnvironmentVariableW(varname.c_str(), env_path, max_env_path_size);
			SetEnvironmentVariableW(
				varname.c_str(), (to_back_slashes(expand_variables(varvalue)) + L";" + env_path).c_str());
		}

		std::for_each(
			vm_options.begin(),
			vm_options.end(),
			[&](std::string& opt) { opt = to_utf8(expand_variables(to_utf16(opt))); });

		JavaVMOption* options = new JavaVMOption[vm_options.size()];
		for (std::size_t i = 0; i < vm_options.size(); ++i) {
			options[i].optionString = _strdup(vm_options[i].c_str());
			options[i].extraInfo = nullptr;
		}

		JavaVMInitArgs vm_args;
		memset(&vm_args, 0, sizeof(vm_args));
		vm_args.version = JNI_VERSION_10;
		vm_args.nOptions = jint(vm_options.size());
		vm_args.options = options;
		vm_args.ignoreUnrecognized = JNI_TRUE;

		HMODULE module = LoadLibraryW(L"jvm.dll");
		if (module == nullptr) {
			throw std::runtime_error(
				std::string("Failed to load jvm.dll: ")  + format_error_message(GetLastError()));
		}

		typedef jint(JNICALL *CREATE_JAVA_VM)(JavaVM**, JNIEnv**, JavaVMInitArgs*);
		CREATE_JAVA_VM createJavaVM = (CREATE_JAVA_VM)GetProcAddress(module, "JNI_CreateJavaVM");
		if (createJavaVM == nullptr) {
			throw std::runtime_error(std::string("GetProcAddress: ") + format_error_message(GetLastError()));
		}

		jint err = createJavaVM(&javaVM, (JNIEnv**)&env, &vm_args);
		if (err != JNI_OK) {
			throw std::runtime_error(std::string("JNI_CreateJavaVM: ") + jni_err_name(err));
		}

		std::string main_class;
		std::vector<std::string> args = parse_command_line(GetCommandLineW());
		
		auto it = args.begin();
		while (it != args.end()) {
			std::size_t pos = (*it).find('=');
			if (pos != std::string::npos && (*it).find("--mainclass") != std::string::npos) {
				main_class = (*it).substr(pos + 1);
				it = args.erase(it);
			}
			else {
				++it;
			}
		}

		if (main_class.empty()) {
			main_class = first_child_element(*doc.RootElement(), "mainclass").GetText();
		}

		jclass main_jclass = jni_class(env, main_class);
		jmethodID main_jmethod = jni_main_method(env, main_jclass, main_class);
		env->CallStaticVoidMethod(main_jclass, main_jmethod, to_java_array(env, args));
		
		jthrowable ex = env->ExceptionOccurred();
		if (ex != nullptr) {
			throw std::runtime_error(format_stack_trace(env, ex));
		}
	}
	catch (std::exception const& ex) {
		MessageBox(0, ex.what(), "Error", MB_ICONERROR);
	}

	if (javaVM != nullptr) {
		javaVM->DestroyJavaVM();
	}

	return 0;
}
