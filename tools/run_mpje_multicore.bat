@echo off

:: they can only be -Dxxx arguments to the JVM
set ALL_ARGS=%*

set ORIG_DIR=%cd%
cd ../target
set MPJ_HOME=lib/mpj-v0_44
:: 40m max usage per VM instance
set mem_alloc=40m

:: edit mpjrun.bat to select the desired JVM
%MPJ_HOME%/bin/mpjrun.bat -np %NUMBER_OF_PROCESSORS% -Xms%mem_alloc% -Xmx%mem_alloc% %ALL_ARGS% e2solver_mpje.jar %ORIG_DIR%/mpje.props

chdir /d %ORIG_DIR%
pause