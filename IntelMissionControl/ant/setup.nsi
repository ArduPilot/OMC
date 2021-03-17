# General Symbol Definitionss
RequestExecutionLevel admin
!define PRODUCT_ICON "..\resources\com\intel\missioncontrol\app-icon\mission-control-icon.ico"
!define NAME "Open Mission Control"
#!define VERSION 2.0.0.8100
#!define VERSIONTXT 2.0.b8100
Name "${NAME}"

!define REGKEY "SOFTWARE\${NAME}"

# !define COMPANY "Public"
# !define URL http://www.todo.com/drones

RequestExecutionLevel user
!define MULTIUSER_EXECUTIONLEVEL Power

# Included files
!include Sections.nsh
!include "MUI2.nsh"
!include "x64.nsh"
!include "MultiUser.nsh"
!include "FileAssociation.nsh"
!include "DumpLog.nsh"
!addplugindir "..\ant"


; See http://nsis.sourceforge.net/Check_if_a_file_exists_at_compile_time for documentation
!macro !defineifexist _VAR_NAME _FILE_NAME
	!tempfile _TEMPFILE
	!ifdef NSIS_WIN32_MAKENSIS
		; Windows - cmd.exe
		!system 'if exist "${_FILE_NAME}" echo !define ${_VAR_NAME} > "${_TEMPFILE}"'
	!else
		; Posix - sh
		!system 'if [ -e "${_FILE_NAME}" ]; then echo "!define ${_VAR_NAME}" > "${_TEMPFILE}"; fi'
	!endif
	!include '${_TEMPFILE}'
	!delfile '${_TEMPFILE}'
	!undef _TEMPFILE
!macroend
!define !defineifexist "!insertmacro !defineifexist"


!define MUI_ABORTWARNING

# Reserved Files
ReserveFile "${NSISDIR}\Plugins\StartMenu.dll"

# Variables
Var StartMenuGroup
Var AppDataOMC
Var delName

# Installer pages
!insertmacro MUI_PAGE_LICENSE "$(myLicense)"
!insertmacro MUI_PAGE_DIRECTORY
!define MUI_STARTMENUPAGE_NODISABLE
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_PAGE_FINISH


#uninstaller pages
#!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

#Licence and language stuff
!insertmacro MUI_LANGUAGE "English" ;first language is the default language
#!insertmacro MUI_LANGUAGE "German"
LicenseLangString myLicense ${LANG_ENGLISH} ".\licenseFile.rtf"


# Installer attributes
OutFile ..\setupFiles\OpenMissionControlSetup.exe
CRCCheck on
XPStyle on
Icon "..\resources\com\intel\missioncontrol\app-icon\mission-control-icon.ico"
ShowInstDetails show
AutoCloseWindow false
VIProductVersion "1.0.1.1"
VIAddVersionKey ProductName "${NAME}"
VIAddVersionKey ProductVersion "${VERSION}"
VIAddVersionKey CompanyName "${COMPANY}"
VIAddVersionKey CompanyWebsite "${URL}"
VIAddVersionKey FileVersion "${VERSION}"
VIAddVersionKey FileDescription "${NAME}"
VIAddVersionKey LegalCopyright "Copyright by ${COMPANY}"
InstallDirRegKey HKLM "${REGKEY}" Path
UninstallIcon ".\uninstall.ico"
ShowUninstDetails show


#Hiding NSIS installer information stuff
BrandingText "${NAME} ${VERSIONTXT}"


!define /date MyTIMESTAMP "%Y-%m-%d-%H-%M-%S"


# Installer sections
# the next section uses two existing files "jogl.dll" and OpenMissionControl.exe , which should reside in the root directory
Section -Main SEC0000
	DetailPrint "Installing ${NAME} ${VERSIONTXT}"
	DetailPrint "on ${MyTIMESTAMP}"

	${If} ${RunningX64}
		DetailPrint "64-bit Windows detected"
		SetRegView 64
	${Else}
		DetailPrint "32-bit Windows detected"
		SetRegView 32
		Abort "Open Mission Control is not available for 32-bit Windows versions"
	${EndIf}

	ReadRegStr $0 HKLM "${REGKEY}" Path
	DetailPrint "Installation Path in Registry: $0"

	SetOutPath $INSTDIR
    SetOverwrite on

    DetailPrint "deleting old ${NAME}-java runtime"
    RmDir /r $INSTDIR\jre
    RmDir /r $INSTDIR\lib64


    #${If} ${RunningX64}
		File ..\lib64\Elevate.exe
	    File /oname=dcraw.exe ..\lib64\dcraw-9.26-ms-64-bit.exe


		DetailPrint "Installing java-64"
	    SetOutPath $INSTDIR\jre
	    File /r ..\jre\64\*


	    SetOutPath $INSTDIR\lib64
	    File ..\lib64\jogl_desktop.dll
		File ..\lib64\jogl_mobile.dll
		File ..\lib64\nativewindow_awt.dll
		File ..\lib64\nativewindow_win32.dll
		File ..\lib64\newt.dll
		File ..\lib64\gluegen-rt.dll
		File ..\lib64\jSSC-2.8_x86_64.dll
		File ..\lib64\jspWin.dll
		File ..\lib64\libwinpthread-1.dll
		File ..\lib64\libgcc_s_seh-1.dll
		File ..\lib64\libstdc++-6.dll
		File ..\lib64\TrinityLogReader.dll
		File ..\lib64\libFalconLog.dll
		#File ..\lib64\glass.dll

		File ..\lib64\jinput-dx8_64.dll
		File ..\lib64\jinput-raw_64.dll
		File ..\lib32\jinput-wintab.dll

		#dont ship those libs as they are not bug scanned by intel AND not nessesary since bluecove falls back to windows DLLs
		#File ..\lib32\bluecove.dll
		#File ..\lib64\intelbth_x64.dll

	    File ..\resources\gdal\win64\*.dll

	    SetOutPath $INSTDIR
	    File /oname=OpenMissionControl.exe ..\wrapper\OpenMissionControl.exe

	#${Else}
	    #File ..\lib32\Elevate.exe
		#File /oname=dcraw.exe ..\lib32\dcraw-9.26-ms-32-bit.exe


		#DetailPrint "Installing java-32"
	    #SetOutPath $INSTDIR\jre
	    #File /r ..\jre\32\*


		#SetOutPath $INSTDIR\lib32
	    #File ..\lib32\jogl_desktop.dll
		#File ..\lib32\jogl_mobile.dll
		#File ..\lib32\nativewindow_awt.dll
		#File ..\lib32\nativewindow_win32.dll
		#File ..\lib32\newt.dll
		#File ..\lib32\gluegen-rt.dll
		#File ..\lib32\jSSC-2.8_x86.dll
		#File ..\lib32\jspWin.dll
		#File ..\lib32\TrinityLogReader.dll
		#File ..\lib32\libFalconLog.dll

		#File ..\lib32\jinput-dx8.dll
		#File ..\lib32\jinput-raw.dll
		#File ..\lib32\jinput-wintab.dll

		#File ..\lib32\bluecove.dll
		#File ..\lib32\intelbth.dll

	    #File ..\resources\gdal\gdalalljni32.dll

	    #SetOutPath $INSTDIR
	    #File /oname=OpenMissionControl.exe ..\wrapper\OpenMissionControl.exe
	#${EndIf}

	#DetailPrint "Installing java-common"
	#SetOutPath $INSTDIR\jre
	#File /r ..\..\OpenMissionControl\jre\common\*
	#SetOutPath $INSTDIR



	######################################################


	SetOutPath $INSTDIR
    File ..\lib32\exiftool.exe
    File ..\lib32\.ExifTool_config
    File ..\lib32\putty.exe
    #File ..\lib32\TeamViewerQS.exe

    # log4j configuration which is supposed to be loaded during app startup is now included in JAR
    Delete /REBOOTOK log4j2.xml

    File ..\setupFiles\OpenMissionControl.jar


    SetOutPath $INSTDIR\gdal-data
    File ..\resources\gdal\data\*
    SetOutPath $INSTDIR\pix4dUpload
    #File /r /x .svn ..\..\pix4d\files\*

	RmDir /r $INSTDIR\dependencies
    SetOutPath $INSTDIR\dependencies
    File /r ..\target\dependency\*

    SetOutPath $INSTDIR\manuals
    File /r ..\..\manuals\*

    SetOutPath $INSTDIR
    WriteRegStr HKLM "${REGKEY}\Components" Main 1


	nsisFirewall::AddAuthorizedApplication "$INSTDIR\OpenMissionControl.exe" "OpenMissionControl"

	nsisFirewall::AddAuthorizedApplication "$INSTDIR\jre\bin\java.exe" "OMCJava"
	nsisFirewall::AddAuthorizedApplication "$INSTDIR\jre\bin\javaw.exe" "OMCJavaw"
	nsisFirewall::AddAuthorizedApplication "$INSTDIR\jre\bin\javaws.exe" "OMCJavaws"

	#nsisFirewall::AddAuthorizedApplication "$INSTDIR\TeamViewerQS.exe" "OMCLiveSupport"
	nsisFirewall::AddAuthorizedApplication "$INSTDIR\putty.exe" "OMCPutty"

	Pop $0
	IntCmp $0 0 FirewallOK
		DetailPrint "An error happened while adding program to Firewall exception list (result=$0)"
		GoTo FirewallDone
	FirewallOK:
		DetailPrint "Program added to Firewall exception list"
	FirewallDone:


	DetailPrint "copying installer to tmp dir, so Open Mission Control can pick it up for later incremental updates"
	Delete $TEMP\OpenMissionControl*.exe
	CopyFiles $EXEPATH $TEMP

SectionEnd


!macro GET_STARTMENUGROUP_FOR_INSTDIR
	!define Index 'Line${__LINE__}'
	ClearErrors
	StrCpy $R1 "0"
	"${Index}-Loop:"

	EnumRegValue $R0 HKLM "${REGKEY}\StartMenu" "$R1"
	IfErrors "${Index}-End"

	StrCmp $R0 "" "${Index}-False"
	  IntOp $R1 $R1 + 1
	  ReadRegStr $R2 HKLM "${REGKEY}\StartMenu" "$R0"
#	  DetailPrint "found $R1: $R0 : $R2"
	  StrCmp $R2 "$INSTDIR" "${Index}-True" "${Index}-Loop"

	"${Index}-True:"
		StrCpy $StartMenuGroup $R0
		DetailPrint "$INSTDIR found in Start Menu $StartMenuGroup"
		goto "${Index}-End"

	"${Index}-False:"
		StrCpy $StartMenuGroup ""
		goto "${Index}-End"

	"${Index}-End:"
		!undef Index
!macroend


Section -post SEC0001
    
	WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    WriteRegStr HKLM "${REGKEY}" StartMenuGroup $StartMenuGroup

    StrCpy $1 $StartMenuGroup
    !insertmacro GET_STARTMENUGROUP_FOR_INSTDIR
    StrCpy $0 $StartMenuGroup
    StrCpy $StartMenuGroup $1
    ${IF} $StartMenuGroup == $0;
    	DetailPrint "Use key StartMenu $StartMenuGroup for $INSTDIR (old: $0)"
    ${Else}
	    ${IF} $0 == "";
	    	DetailPrint "Use key StartMenu $StartMenuGroup for $INSTDIR (old: $0)"
	    ${Else}
	    	DetailPrint "delete Start Menu $0, use new $StartMenuGroup for $INSTDIR"
	    	DeleteRegValue HKLM "${REGKEY}\StartMenu" $0
	    	RmDir /r $SMPROGRAMS\$0
	    ${EndIf}
    ${EndIf}

    WriteRegStr HKLM "${REGKEY}\StartMenu" $StartMenuGroup $INSTDIR

#    RegDLL "$INSTDIR\jogl.dll"
#    RegDLL "$INSTDIR\jogl_cg.dll"
#    RegDLL "$INSTDIR\jogl_awt.dll"
#    RegDLL "$INSTDIR\gluegen-rt.dll"

    #CreateShortcut "$SMPROGRAMS\$StartMenuGroup\${NAME}.lnk" "$INSTDIR\OpenMissionControl.exe"

    #ReadRegStr $0 HKCU "Software\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders" Personal
    #SetOutPath "$0\Open Mission Control"

    #remove old style link on desktop
    Delete /REBOOTOK "$DESKTOP\Open Mission Control.lnk"
    #remove old path with links in start menu
    RmDir /r /REBOOTOK "$SMPROGRAMS\Open Mission Control"

    CreateShortCut "$DESKTOP\${NAME}.lnk" "$INSTDIR\OpenMissionControl.exe"

    SetOutPath $SMPROGRAMS\$StartMenuGroup

    #CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Live Support Open Mission Control.lnk" $INSTDIR\TeamViewerQS.exe
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Uninstall ${NAME}.lnk" $INSTDIR\uninstall.exe

    #CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Manual.lnk" $INSTDIR\manuals\en\manual.pdf


    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}" DisplayName "${NAME}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}" DisplayVersion "${VERSION}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}" Publisher "${COMPANY}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}" URLInfoAbout "${URL}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}" UninstallString $INSTDIR\uninstall.exe
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}" StartMenuGroup $StartMenuGroup
    WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}" NoModify 1
    WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${NAME}" NoRepair 1

    SetOutPath $INSTDIR
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\${NAME}.lnk" "$INSTDIR\OpenMissionControl.exe"

    #${registerExtension} "$INSTDIR\OpenMissionControl.exe" ".mfs" "OpenMissionControl_Session"
    #${registerExtension} "$INSTDIR\OpenMissionControl.exe" ".mlf" "OpenMissionControl_Licence"
    #${registerExtension} "$INSTDIR\OpenMissionControl.exe" ".camera" "OpenMissionControl_Camera"
    #${registerExtension} "$INSTDIR\OpenMissionControl.exe" ".fml" "OpenMissionControl_Flight_Plan"
    #${registerExtension} "$INSTDIR\OpenMissionControl.exe" ".ptg" "OpenMissionControl_Matching"
    #${registerExtension} "$INSTDIR\OpenMissionControl.exe" ".kml" "KML"
    #${registerExtension} "$INSTDIR\OpenMissionControl.exe" ".kmz" "KMZ"

	WriteUninstaller $INSTDIR\uninstall.exe
    
    StrCpy $0 "$INSTDIR\install.log"
	Push $0
	Call DumpLog
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
#DetailPrint "use macro SELECT_UNSECTION"
	${If} ${RunningX64}
		DetailPrint "64-bit Windows detected"
		SetRegView 64
	${Else}
		DetailPrint "32-bit Windows detected"
		SetRegView 32
	${EndIf}
    Push $R0
    ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend





!macro CHECK_INSTDIR_STARTMENUGROUP_UNINIT

	#Aenderung $INSTDIR nicht moeglich in GUI, daher nur Ermittlung $StartMenuGroup

# test paths if available, else exit
# $INSTDIR
	IfFileExists $INSTDIR\OpenMissionControl.exe PathGood
		  MessageBox MB_ICONEXCLAMATION|MB_OK "Open Mission Control not available in $INSTDIR, abort deinstallation"
          DetailPrint "Open Mission Control not available in $INSTDIR, abort deinstallation"
	  	  Abort ;
    PathGood:

# $SMPROGRAMS\$StartMenuGroup
  	${IF} $StartMenuGroup == "";
  		MessageBox MB_ICONEXCLAMATION|MB_OKCANCEL "Open Mission Control not available in Start Menu for this installation path, deinstallation will not delete entries in Start Menu!" IDOK PathGood1 IDCANCEL abortInst1
		abortInst1:
		  	DetailPrint "Open Mission Control not available in Start Menu for this installation path, abort deinstallation"
		  	Abort
	  	PathGood1:
  	${EndIf}

	IfFileExists $SMPROGRAMS\$StartMenuGroup PathGood2
		${IF} $StartMenuGroup == "";
			GoTo PathGood2
		${Else}
			MessageBox MB_ICONEXCLAMATION|MB_OKCANCEL "Open Mission Control not available in Start Menu ($SMPROGRAMS\$StartMenuGroup), deinstallation will not delete entries in Start Menu!" IDOK PathGood2 IDCANCEL abortInst2
		${EndIf}
	abortInst2:
	  DetailPrint "Open Mission Control not available in Start Menu ($SMPROGRAMS\$StartMenuGroup), abort deinstallation"
	  Abort ;
	PathGood2:

	DetailPrint "Open Mission Control available in $INSTDIR, Start Menu $SMPROGRAMS\$StartMenuGroup"

!macroend


!macro GET_INSTDIR_STARTMENUGROUP_UNINIT

	#installation path can be different to Registry
	#DetailPrint $INSTDIR
    ${IF} $INSTDIR == "";
    	ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
    	${IF} $INSTDIR == "";
	    	DetailPrint "Installation path not found in registry, use standard"
	        ${If} ${RunningX64}
				DetailPrint "64-bit Windows detected"
				${If} $PROGRAMFILES64 == "" ;
				    DetailPrint "Installation path: PROGRAMFILES64 not set, use PROGRAMFILES"
				    StrCpy $INSTDIR "$PROGRAMFILES\Open\Open Mission Control"
				${Else}
					StrCpy $INSTDIR "$PROGRAMFILES64\Open\Open Mission Control"
				${EndIf}
				DetailPrint $INSTDIR
			${Else}
				DetailPrint "32-bit Windows detected"
				StrCpy $INSTDIR "$PROGRAMFILES\Open\Open Mission Control"
				DetailPrint $INSTDIR
			${EndIf}
			StrCpy $StartMenuGroup "${NAME}"
			DetailPrint "Start Menu path not found in registry, use standard: $StartMenuGroup"
		${Else}
			DetailPrint "set from Registry $INSTDIR"
			ReadRegStr $StartMenuGroup HKLM "${REGKEY}" StartMenuGroup
			DetailPrint "set from Registry Start Menu $StartMenuGroup"
		${EndIf}
	${Else}
		DetailPrint "set from Installation $INSTDIR"
		!insertmacro GET_STARTMENUGROUP_FOR_INSTDIR
		DetailPrint "set from Installation Start Menu $StartMenuGroup"
	${EndIf}

	#$SMPROGRAMS\$StartMenuGroup
  	${IF} $StartMenuGroup == "";
  		DetailPrint "Open Mission Control not available in Start Menu for this installation path ($INSTDIR), will abort deinstallation"
	${EndIf}

	IfFileExists $SMPROGRAMS\$StartMenuGroup PathGood2
      DetailPrint "Open Mission Control not available in Start Menu ($SMPROGRAMS\$StartMenuGroup), will abort deinstallation"
	PathGood2:

# test paths if available, else exit
# $INSTDIR
	IfFileExists $INSTDIR\OpenMissionControl.exe PathGood
	      DetailPrint "Open Mission Control not available in $INSTDIR, will abort deinstallation"
	PathGood:
	
!macroend

# Uninstaller sections
Section /o -un.Main UNSEC0000
#DetailPrint "use section un.Main"
	${If} ${RunningX64}
		DetailPrint "64-bit Windows detected"
		SetRegView 64
	${Else}
		DetailPrint "32-bit Windows detected"
		SetRegView 32
	${EndIf}

    # the line is giving an error and this directory is being deleted none the less
    #RMDir /r /REBOOTOK $INSTDIR
    #DeleteRegValue HKLM "${REGKEY}\Components" Main
SectionEnd

Section -un.post UNSEC0001
#DetailPrint "use section un.post"
	${If} ${RunningX64}
		DetailPrint "64-bit Windows detected"
		SetRegView 64
	${Else}
		DetailPrint "32-bit Windows detected"
		SetRegView 32
	${EndIf}

#	!insertmacro GET_INSTDIR_STARTMENUGROUP_UNINIT
	!insertmacro CHECK_INSTDIR_STARTMENUGROUP_UNINIT

	
	#Verify the uninstallation of appdata 
	SetShellVarContext current
	DetailPrint "check settings in $APPDATA"
	
	StrCpy $delName "Open Mission Control"
	
	IfFileExists "$APPDATA\Open Mission Control" PathGood3
		DetailPrint "Open Mission Control not available in $APPDATA"
		StrCpy $AppDataOMC ""
	PathGood3:
	StrCpy $AppDataOMC "$APPDATA\Open Mission Control"
	MessageBox MB_YESNO|MB_ICONQUESTION|MB_DEFBUTTON2 "Would you like to remove the application settings, custom templates and license files from$\n$AppDataOMC?$\nIf you remove them now, you will need to request your license from Open again when re-installing." IDYES next
		DetailPrint "Do not delete $AppDataOMC"
		StrCpy $AppDataOMC ""
	next:

	SetShellVarContext all

    Delete /REBOOTOK $INSTDIR\uninstall.exe
    Delete /REBOOTOK $INSTDIR\*.jar

    #UnRegDLL "$INSTDIR\jogl.dll"
    #UnRegDLL "$INSTDIR\jogl_cg.dll"
    #UnRegDLL "$INSTDIR\jogl_awt.dll"
    #UnRegDLL "$INSTDIR\gluegen-rt.dll"


	nsisFirewall::RemoveAuthorizedApplication "$INSTDIR\OpenMissionControl.exe"

	nsisFirewall::RemoveAuthorizedApplication "$INSTDIR\jre\bin\java.exe"
	nsisFirewall::RemoveAuthorizedApplication "$INSTDIR\jre\bin\javaw.exe"
	nsisFirewall::RemoveAuthorizedApplication "$INSTDIR\jre\bin\javaws.exe"

	#nsisFirewall::RemoveAuthorizedApplication "$INSTDIR\TeamViewerQS.exe"
	nsisFirewall::RemoveAuthorizedApplication "$INSTDIR\putty.exe"

	RmDir /r /REBOOTOK $INSTDIR\gdal-data
    RmDir /r /REBOOTOK $INSTDIR\jre
    RmDir /r /REBOOTOK $INSTDIR\pix4dUpload
    RmDir /r /REBOOTOK $INSTDIR\dependencies
    RmDir /r /REBOOTOK $INSTDIR\manuals
    RmDir /r /REBOOTOK $INSTDIR

#check entries, if they link to active Regkey; only in this case delete:
    ReadRegStr $1 HKLM "${REGKEY}" Path
    ${IF} $1 == "$INSTDIR";
    	#Path $1 in Registry identical with $INSTDIR to be deinstalled
    	DetailPrint "delete Registry values $(NAME)"
    	
    	DetailPrint "Path $1 in Registry"
    	DeleteRegValue HKLM "${REGKEY}" Path
    	DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    	DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"

		DetailPrint "delete $DESKTOP\$delName.lnk"
    	Delete /REBOOTOK "$DESKTOP\$delName.lnk"

 	    ${unregisterExtension} ".mfs" "OpenMissionControl_Session"
	    ${unregisterExtension} ".mlf" "OpenMissionControl_Licence"
	    ${unregisterExtension} ".camera" "OpenMissionControl_Camera"
	    ${unregisterExtension} ".fml" "OpenMissionControl_Flight_Plan"
	    ${unregisterExtension} ".pmt" "OpenMissionControl_Matching"
	    ${unregisterExtension} ".ptg" "OpenMissionControl_Matching"
        ${unregisterExtension} ".kml" "KML"
	    ${unregisterExtension} ".kmz" "KMZ"
		
	    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\$delName"


		${IF} $StartMenuGroup == "";
			DetailPrint "StartMenuGroup not available"
		${Else}
    		RMDir /r /REBOOTOK "$SMPROGRAMS\$StartMenuGroup"
    		DeleteRegValue HKLM "${REGKEY}\StartMenu" $StartMenuGroup
    		DetailPrint "deleted Registry key StartMenuGroup in $StartMenuGroup"
		${EndIf}
    	DeleteRegKey /IfEmpty HKLM "${REGKEY}"
	${ELSE}
		DetailPrint "Path $1 in Registry, $INSTDIR to be deinstalled, leave Extensions in registry, leave RegValues and Keys in registry"
		${IF} $StartMenuGroup == "";
			DetailPrint "StartMenuGroup $2 in Registry, StartMenuGroup for deinstallation path not found, leave StartMenuGroup $2 in registry"
		${ELSE}
			ReadRegStr $2 HKLM "${REGKEY}" StartMenuGroup
			${IF} $2 == "$StartMenuGroup";
				DetailPrint "StartMenuGroup $2 in Registry same as to be deinstalled, leave StartMenuGroup $2 in registry, used by active installation"
				DeleteRegValue HKLM "${REGKEY}\StartMenu" $StartMenuGroup
	    	${ELSE}
			DetailPrint "StartMenuGroup $2 in Registry, $StartMenuGroup to be deinstalled, StartMenuGroup $2 not used by active installation"
				RMDir /r /REBOOTOK "$SMPROGRAMS\$StartMenuGroup"
	    		DeleteRegValue HKLM "${REGKEY}\StartMenu" $StartMenuGroup
	  		${EndIf}
    	${EndIf}
	${EndIf}
			
	${IF} $AppDataOMC == "";
	${ELSE}
		DetailPrint "Delete $AppDataOMC"
		RMDir /r /REBOOTOK "$AppDataOMC"
	${EndIf}		

SectionEnd


# if all is well make the shortcuts to the start menu
Function .onInstSuccess
	ShellExecAsUser::ShellExecAsUser "open" '$INSTDIR\OpenMissionControl.exe'
FunctionEnd

Function .onInit

    !insertmacro MULTIUSER_INIT
#	!insertmacro MUI_LANGDLL_DISPLAY
    InitPluginsDir

	${If} ${RunningX64}
		DetailPrint "64-bit Windows detected"
		SetRegView 64
	${Else}
		DetailPrint "32-bit Windows detected"
		SetRegView 32
	${EndIf}

	#detect value from system configuration if already installed
	#installation path can be different to Registry
	#DetailPrint $INSTDIR
    #${IF} $INSTDIR == "";
    	ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
		DetailPrint "Installation Path in Registry: $INSTDIR"
    	${IF} $INSTDIR == "";
	    	DetailPrint "Installation path not found in registry, use standard"
	        ${If} ${RunningX64}
				DetailPrint "64-bit Windows detected"
				${If} $PROGRAMFILES64 == "" ;
				    DetailPrint "Installation path: PROGRAMFILES64 not set, use PROGRAMFILES"
				    StrCpy $INSTDIR "$PROGRAMFILES\Open\Open Mission Control"
				${Else}
					StrCpy $INSTDIR "$PROGRAMFILES64\Open\Open Mission Control"
				${EndIf}
				DetailPrint $INSTDIR
			${Else}
				DetailPrint "32-bit Windows detected"
				StrCpy $INSTDIR "$PROGRAMFILES\Open\Open Mission Control"
				DetailPrint $INSTDIR
			${EndIf}
			StrCpy $StartMenuGroup "$(NAME)"
			DetailPrint "Start Menu path not found in registry, use standard: $StartMenuGroup"
		${Else}
			DetailPrint "set from Registry $INSTDIR"
			ReadRegStr $StartMenuGroup HKLM "${REGKEY}" StartMenuGroup
			DetailPrint "set from Registry Start Menu $StartMenuGroup"
		${EndIf}
	#${Else}
	#	DetailPrint "set from Installation $INSTDIR"
	#	!insertmacro GET_STARTMENUGROUP_FOR_INSTDIR
	#	DetailPrint "set from Installation Start Menu $StartMenuGroup"
	#${EndIf}


FunctionEnd

# Uninstaller functions
Function un.onInit
#Ausfuehrung vor GUI-Anzeige, kein Logging
#DetailPrint "use section un.onInit"
	${If} ${RunningX64}
		DetailPrint "64-bit Windows detected"
		SetRegView 64
	${Else}
		DetailPrint "32-bit Windows detected"
		SetRegView 32
	${EndIf}
    !insertmacro MULTIUSER_UNINIT
#	!insertmacro MUI_UNGETLANGUAGE

	!insertmacro GET_INSTDIR_STARTMENUGROUP_UNINIT

    !insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd
