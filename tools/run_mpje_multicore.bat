@echo off

:: they can only be -Dxxx arguments to the JVM
set ALL_ARGS=%*

set MPJ_HOME=%cd%/../external-libs/mpj-v0_44
set PATH=%PATH%;%MPJ_HOME%/bin
set ORIG_DIR=%cd%
cd ../target
:: 50m max usage per VM instance. For GraalVM it needs 60m.
set mem_alloc=60m

:: edit mpjrun.bat to select the desired JVM
%MPJ_HOME%/bin/mpjrun.bat -np %NUMBER_OF_PROCESSORS% -Xms%mem_alloc% -Xmx%mem_alloc% %ALL_ARGS% e2solver_mpje.jar

chdir /d %ORIG_DIR%
pause