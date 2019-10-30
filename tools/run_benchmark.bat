@echo off

:: they can only be -Dxxx arguments to the JVM
set ALL_ARGS=%*

set ORIG_DIR=%cd%
cd ../target

:: 40m max usage for 8 threads. Times 5 because we warm up 3 iterations and measure 2 iterations
set mem_alloc=200m

java -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xms%mem_alloc% -Xmx%mem_alloc% %ALL_ARGS% -jar e2solver_benchmark.jar

chdir /d %ORIG_DIR%
