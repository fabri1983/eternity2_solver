@echo off

:: they can only be -Dxxx arguments to the JVM
set ALL_ARGS=%*

set ORIG_DIR=%cd%
cd ../target

:: 25m max usage for 8 threads
set mem_alloc=25m

java -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xms%mem_alloc% -Xmx%mem_alloc% %ALL_ARGS% -jar e2solver.jar

chdir /d %ORIG_DIR%
