@echo off
setlocal EnableDelayedExpansion
setlocal EnableExtensions

REM for /f "delims=" %%i in ('git describe --tags ') do set a=%%i
for /f "delims=" %%i in ('git rev-parse --show-toplevel') do set git_root=%%i

for /f "delims=" %%i in ('git rev-parse --show-toplevel') do set git_root=%%i

cd dronekit

set local_repo=!git_root!/maven-local-repository
echo root_git^=!git_root!
echo local_repo^=!local_repo!

set a=./ClientLib/target/ClientLib-1.0-SNAPSHOT.jar
set b=./dependencyLibs/AndroidCompat/target/AndroidCompat-1.0-SNAPSHOT.jar
set c=./dependencyLibs/Mavlink/target/Mavlink-1.0-SNAPSHOT.jar

REM ---- build
cmd /c mvn install

REM ---- install to *root*/maven-local-repository
cmd /c mvn install:install-file -DlocalRepositoryPath^=!local_repo! ^
 -Dfile^=!a! -DgroupId=com.intel.dronekit -DartifactId=ClientLib -Dversion=1.0-SNAPSHOT -Dpackaging=jar

cmd /c mvn install:install-file -DlocalRepositoryPath^=!local_repo! ^
 -Dfile^=!b! -DgroupId=com.intel.dronekit -DartifactId=AndroidCompat -Dversion=1.0-SNAPSHOT -Dpackaging=jar

cmd /c mvn install:install-file -DlocalRepositoryPath^=!local_repo! ^
 -Dfile^=!c! -DgroupId=com.intel.dronekit -DartifactId=Mavlink -Dversion=1.0-SNAPSHOT -Dpackaging=jar