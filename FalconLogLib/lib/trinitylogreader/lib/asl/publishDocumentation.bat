for %%a in (.) do set currentfolder=%%~na
set share=\\ascteclinux.asctec.local\doxygen\
pushd %share% && (
    rmdir /S /Q %currentfolder%
    popd
)
xcopy doc\html\*.* \\ascteclinux.asctec.local\doxygen\%currentfolder%\ /S /Q