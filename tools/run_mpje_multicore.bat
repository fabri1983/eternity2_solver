@echo off

:: they can only be -Dxxx arguments to the JVM
set ALL_ARGS=%*

set MPJ_HOME=%cd%/../target/libs/mpj-v0_44
set PATH=%PATH%;%MPJ_HOME%/bin
set ORIG_DIR=%cd%
cd ../target

:: 40m max usage per VM instance
set mem_alloc=40m

:: edit mpjrun.bat to select the desired JVM
%MPJ_HOME%/bin/mpjrun.bat -np %NUMBER_OF_PROCESSORS% -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xms%mem_alloc% -Xmx%mem_alloc% %ALL_ARGS% e2solver_mpje.jar

chdir /d %ORIG_DIR%
