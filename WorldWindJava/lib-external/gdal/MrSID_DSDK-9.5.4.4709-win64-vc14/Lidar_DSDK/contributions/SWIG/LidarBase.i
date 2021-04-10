
%header %{

// host helper functions

#if defined SWIGPYTHON

static PyObject *doubleArrayToObject(const double *value, size_t length)
{
   PyObject *obj = Py_None;
   if(value != NULL)
   {
      obj = PyTuple_New(length);
      for(size_t i = 0; i < length; i += 1)
         PyTuple_SetItem(obj, i, PyFloat_FromDouble(value[i]));
   }
   return obj;
}

static PyObject *stringArrayToObject(char **value, size_t length)
{
   PyObject *obj = Py_None;
   if(value != NULL)
   {
      obj = PyTuple_New(length);
      for(size_t i = 0; i < length; i += 1)
         PyTuple_SetItem(obj, i, PyString_FromString(value[i]));
   }
   return obj;
}

static double *objectToDouble3(PyObject *obj, double value[3])
{
   if(obj == Py_None)
      return NULL;
   else if(PyTuple_Check(obj) && PyObject_Length(obj) == 3)
   {
      for(int i = 0; i < 3; i += 1)
         value[i] = PyFloat_AsDouble(PyTuple_GetItem(obj, i));
      return value;
   }
   //SWIG_exception(SWIG_TypeError, "expected an Array with 3 doubles");
   return NULL;
}

#elif defined SWIGRUBY

static VALUE doubleArrayToObject(const double *value, size_t length)
{
   VALUE obj = Qnil;
   if(value != NULL)
   {
      obj = rb_ary_new2(length);
      for(size_t i = 0; i < length; i += 1)
         rb_ary_store(obj, i, rb_float_new(value[i]));
   }
   return obj;
}

static VALUE stringArrayToObject(char **value, size_t length)
{
   VALUE obj = Qnil;
   if(value != NULL)
   {
      obj = rb_ary_new2(length);
      for(size_t i = 0; i < length; i += 1)
         rb_ary_store(obj, i, rb_str_new2(value[i]));
   }
   return obj;
}

static double *objectToDouble3(VALUE obj, double value[3])
{
   if(obj == Qnil)
      return NULL;
   else if(TYPE(obj) == T_ARRAY && RARRAY_LEN(obj) == 3)
   {
      for(int i = 0; i < 3; i += 1)
         value[i] = NUM2DBL(rb_ary_entry(obj, i));
      return value;
   }
   //SWIG_exception(SWIG_TypeError, "expected an Array with 3 doubles");
   return NULL;
}

#elif defined SWIGCSHARP

#pragma warning(disable:4702)
// all the helper code is in the typemaps because it lives in the C# world


#endif
%}

#if defined SWIGCSHARP
// HACK: the following code was lifed from
// swigwin-2.0.4/Lib/csharp/csharp.swg.  The "internal" where changed to
// "public" so we could break the ESDK and DSDK into two assemblies where
// the ESDK is not a superset of the DSDK

// Proxy classes (base classes, ie, not derived classes)
%typemap(csbody) SWIGTYPE %{
  private HandleRef swigCPtr;
  protected bool swigCMemOwn;

  public $csclassname(IntPtr cPtr, bool cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = new HandleRef(this, cPtr);
  }

  public static HandleRef getCPtr($csclassname obj) {
    return (obj == null) ? new HandleRef(null, IntPtr.Zero) : obj.swigCPtr;
  }
%}

// Derived proxy classes
%typemap(csbody_derived) SWIGTYPE %{
  private HandleRef swigCPtr;

  public $csclassname(IntPtr cPtr, bool cMemoryOwn) : base($imclassname.$csclazznameSWIGUpcast(cPtr), cMemoryOwn) {
    swigCPtr = new HandleRef(this, cPtr);
  }

  public static HandleRef getCPtr($csclassname obj) {
    return (obj == null) ? new HandleRef(null, IntPtr.Zero) : obj.swigCPtr;
  }
%}

// Typewrapper classes
%typemap(csbody) SWIGTYPE *, SWIGTYPE &, SWIGTYPE [] %{
  private HandleRef swigCPtr;

  public $csclassname(IntPtr cPtr, bool futureUse) {
    swigCPtr = new HandleRef(this, cPtr);
  }

  public $csclassname() {
    swigCPtr = new HandleRef(null, IntPtr.Zero);
  }

  public static HandleRef getCPtr($csclassname obj) {
    return (obj == null) ? new HandleRef(null, IntPtr.Zero) : obj.swigCPtr;
  }
%}

#endif

%include exception.i
%exception {
   try
   {
      $action
   }
   catch(std::exception &err)
   {
      SWIG_exception(SWIG_RuntimeError, err.what());
   }
   catch(...)
   {
      SWIG_exception(SWIG_RuntimeError,"Unknown exception");
   }
}

// make scale and offset native double[3]
#if defined SWIGCSHARP
%include "arrays_csharp.i"

%apply double INPUT[] { double [3] };

%typemap(ctype)   double output[3] "double *"
%typemap(cstype)  double output[3] "double[]"
%typemap(csout, excode=SWIGEXCODE) double output[3] {
   IntPtr raw = $imcall;$excode
   double[] ret = null;
   if(raw != IntPtr.Zero)
   {
      ret = new double[3];
      System.Runtime.InteropServices.Marshal.Copy(raw, ret, 0, 3);
   }
   return ret;
}
%typemap(out)      double output[3] "$result = $1;"

%apply double output[3] { double *getScale, double *getOffset };

#elif defined SWIGJAVA
%include "arrays_java.i"

%apply double[ANY] { const double *getScale, const double *getOffset };
%typemap(out) const double *getScale, const double *getOffset {
   $result = SWIG_JavaArrayOutDouble(jenv, $1, 3);
}

#else

%typemap(in) double[3] (double temp[3]) {
   $1 = objectToDouble3($input, temp);
}

%typemap(out) const double *getScale, const double *getOffset {
   $result = doubleArrayToObject($1, 3);
}
#endif

// handle getClassIdNames()
#if defined SWIGCSHARP
%include "wchar.i"

%typemap(ctype)   char const * const *getClassIdNames "char **"
%typemap(cstype)  char const * const *getClassIdNames "string[]"
%typemap(csout, excode=SWIGEXCODE) char const * const *getClassIdNames {
   IntPtr raw1 = $imcall;$excode
   int length = (int)$imclassname.PointSource_getNumClassIdNames(swigCPtr);
   IntPtr[] raw2 = new IntPtr[length];
   System.Runtime.InteropServices.Marshal.Copy(raw1, raw2, 0, length);
   string[] ret = new string[length];
   for (uint i = 0; i < length; i += 1)
      ret[i] = System.Runtime.InteropServices.Marshal.PtrToStringAnsi(raw2[i]);
   return ret;
}
%typemap(out)      char const * const *getClassIdNames "$result = $1;"

#elif defined SWIGJAVA

%typemap(jni)     char const * const *getClassIdNames "jobjectArray"
%typemap(jtype)   char const * const *getClassIdNames "String[]"
%typemap(jstype)  char const * const *getClassIdNames "String[]"
%typemap(out)     char const * const *getClassIdNames {
    const jclass clazz = JCALL1(FindClass, jenv, "java/lang/String");
    size_t len = arg1->getNumClassIdNames();
    jresult = JCALL3(NewObjectArray, jenv, len, clazz, NULL);
    /* exception checking omitted */

    for(size_t i = 0; i < len; i += 1)
    {
      jstring temp = JCALL1(NewStringUTF, jenv, *result++);
      JCALL3(SetObjectArrayElement, jenv, jresult, i, temp);
      JCALL1(DeleteLocalRef, jenv, temp);
    }
}
%typemap(javaout) char const * const *getClassIdNames {
    return $jnicall;
  }

#else

%typemap(out) char const * const *getClassIdNames {
   $result = stringArrayToObject($1, arg1->getNumClassIdNames());
}

#endif

#if defined(SWIGCSHARP)

%typemap(ctype)   char const * const * collections "char **"
%typemap(cstype)  char const * const * collections "string[]"
%typemap(imtype, inattributes="[MarshalAs(UnmanagedType.LPArray, ArraySubType=UnmanagedType.LPStr)]") char const * const * collections "string[]"
%typemap(csin)    char const * const * collections "$csinput"
//%typemap(in)      char const * const * collections "$1 = $input;"

#else

#endif

// handle Metadata::getValue()
#if defined SWIGCSHARP

%typemap(ctype)   const void *getValue "void *"
%typemap(cstype)  const void *getValue "System.Object"
%typemap(csout, excode=SWIGEXCODE) const void *getValue {
   IntPtr ptr = $imcall;$excode
   int length = (int)$imclassname.Metadata_getValueLength(swigCPtr, idx);
   switch((MetadataDataType)$imclassname.Metadata_getDataType(swigCPtr, idx))
   {
      case MetadataDataType.METADATA_DATATYPE_STRING:
         return System.Runtime.InteropServices.Marshal.PtrToStringAnsi(ptr);
      case MetadataDataType.METADATA_DATATYPE_BLOB:
         byte[] blob = new byte[length];
         System.Runtime.InteropServices.Marshal.Copy(ptr, blob, 0, length);
         return blob;
      case MetadataDataType.METADATA_DATATYPE_REAL_ARRAY:
         double[] array = new double[length];
         System.Runtime.InteropServices.Marshal.Copy(ptr, array, 0, length);
         return array;
      default:
         return null;
   }
}
%typemap(out)      const void *getValue "$result = $1;"

#elif defined SWIGJAVA

%typemap(jni)     const void *getValue "jobject"
%typemap(jtype)   const void *getValue "Object"
%typemap(jstype)  const void *getValue "Object"
%typemap(out)     const void *getValue {
   size_t length = arg1->getValueLength(arg2);
   switch(arg1->getDataType(arg2))
   {
      case LizardTech::METADATA_DATATYPE_STRING:
         $result = JCALL1(NewStringUTF, jenv, static_cast<const char *>($1));
         break;
      case LizardTech::METADATA_DATATYPE_BLOB:
         $result = SWIG_JavaArrayOutSchar(jenv, static_cast<signed char *>($1), length);
         break;
      case LizardTech::METADATA_DATATYPE_REAL_ARRAY:
         $result = SWIG_JavaArrayOutDouble(jenv, static_cast<double *>($1), length);
         break;
      default:
         $result = NULL;
         break;
   }
}
%typemap(javaout) const void *getValue {
    return $jnicall;
  }

#elif defined SWIGPYTHON

%typemap(out) const void *getValue {
   size_t length = arg1->getValueLength(arg2);
   switch(arg1->getDataType(arg2))
   {
      case LizardTech::METADATA_DATATYPE_STRING:
         $result = PyString_FromString(static_cast<const char *>($1));
         break;
      case LizardTech::METADATA_DATATYPE_BLOB:
         $result = PyString_FromStringAndSize(static_cast<const char *>($1), length);
         break;
      case LizardTech::METADATA_DATATYPE_REAL_ARRAY:
         $result = doubleArrayToObject(static_cast<const double *>($1), length);
         break;
      default:
         $result = Py_None;
         break;
   }
}

#elif defined SWIGRUBY

%typemap(out) const void *getValue {
   size_t length = arg1->getValueLength(arg2);
   switch(arg1->getDataType(arg2))
   {
      case LizardTech::METADATA_DATATYPE_STRING:
         $result = rb_str_new2(static_cast<const char *>($1));
         break;
      case LizardTech::METADATA_DATATYPE_BLOB:
         $result = rb_str_new(static_cast<const char *>($1), length);
         break;
      case LizardTech::METADATA_DATATYPE_REAL_ARRAY:
         $result = doubleArrayToObject(static_cast<const double *>($1), length);
         break;
      default:
         $result = Qnil;
         break;
   }
}

#endif

// handle RefCounting
%define SETUP_RC(classname)
   %feature("ref") LizardTech::classname ""
   %feature("unref") LizardTech::classname "$this->release();"
   %extend LizardTech::classname {
     ~classname() {
       //::fprintf(stderr, "in ~" #classname "\n");
       $self->release();
     }
   }
   %ignore LizardTech::classname::~classname;
%enddef

%define FIXUP_RC(classname)
   SETUP_RC(classname)
   %ignore LizardTech::classname::create;
   %feature("notabstract") classname;
   %extend LizardTech::classname {
      classname()
      {
         //::fprintf(stderr, "in " #classname "\n");
         return LizardTech::classname::create();
      }
   }
   %ignore LizardTech::classname::classname;
%enddef 

// Handle returning a reference to a member variable
#if defined(SWIGCSHARP)
%define ADD_REFERENCE(RefClass)
   // make sure the GC does not collect the owning object while RefClass is alive
   %typemap(cscode) RefClass %{
      private System.Object refToOwner;
      internal void setOwnerObject(System.Object owner) { refToOwner = owner; }
   %}
%enddef
%define FIXUP_GET_REFERENCE(RefClass, Method)
   %typemap(csout, excode=SWIGEXCODE) RefClass &Method {
      IntPtr cPtr = $imcall;$excode
      $csclassname ret = null;
      if(cPtr != IntPtr.Zero)
      {
         ret = new $csclassname(cPtr, $owner);
         ret.setOwnerObject(this);
      }
      return ret;
   }
%enddef
%define FIXUP_GET_POINTER(RefClass, Method)
   %typemap(csout, excode=SWIGEXCODE) RefClass *Method {
      IntPtr cPtr = $imcall;$excode
      $csclassname ret = null;
      if(cPtr != IntPtr.Zero)
      {
         ret = new $csclassname(cPtr, $owner);
         ret.setOwnerObject(this);
      }
      return ret;
   }
%enddef

%define KEEP_REFERENCE(Class, RefClass)
   // make sure the GC does not collect the owned object while Class is alive
   %typemap(cscode) LizardTech::Class %{
      private RefClass refTo##RefClass;
      internal void set##RefClass##Ref(RefClass obj) { refTo##RefClass = obj; }
   %}
%enddef
%define PASS_REFERENCE(ReturnType, Method, RefClass, Arg)
   %typemap(csout, excode=SWIGEXCODE) ReturnType Method {
      IntPtr cPtr = $imcall;$excode
      $csclassname ret = null;
      if(cPtr != IntPtr.Zero)
      {
         ret = new $csclassname(cPtr, $owner);
         ret.set##RefClass##Ref(Arg);
      }
      return ret;
   }
%enddef
%define PASS_REFERENCE_VOID(Method, RefClass, Arg)
   %typemap(csout, excode=SWIGEXCODE) void Method {
      $imcall;$excode
      set##RefClass##Ref(Arg);
   }
%enddef

#else
// references to member a member variables????? Python and Ruby

%define ADD_REFERENCE(RefClass)
%enddef
%define FIXUP_GET_REFERENCE(RefClass, Method)
%enddef
%define FIXUP_GET_POINTER(RefClass, Method)
%enddef

%define KEEP_REFERENCE(Class, RefClass)
%enddef
%define PASS_REFERENCE(RetrunType, Method, RefClass, Arg)
%enddef
%define PASS_REFERENCE_VOID(Method, RefClass, Arg)
%enddef

#endif

