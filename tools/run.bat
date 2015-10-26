@echo off

:: they can only be -Dxxx arguments to the JVM
set ALL_ARGS=%*

set ORIG_DIR=%cd%
cd ../target
:: 900m max usage for 8 threads
set mem_alloc=900m

java -XX:+AggressiveOpts -server -Xms%mem_alloc% -Xmx%mem_alloc% -XX:MaxPermSize=512m %ALL_ARGS% -jar e2solver.jar

chdir /d %ORIG_DIR%
pause