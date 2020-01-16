@echo off

@setlocal enableextensions enabledelayedexpansion

set "jvm_args="
set "main_args="
set flag=0
for %%a in (%*) do (
    set "arg=%%~a"
    ::echo !arg!
    if !flag! EQU 0 if "!arg:~0,1!" NEQ "-" (
        set "main_args= !main_args! %%a"
        set flag=2
    )
    if "!arg:~0,1!" EQU "-" (
        set "jvm_args=!jvm_args! !arg!"
        set flag=1
        echo !arg!|find "=" >nul 2>&1 && set flag=0
    )
    if !flag! EQU 1 if "!arg:~0,1!" NEQ "-"  (
        set "jvm_args=!jvm_args!=!arg!"
        set flag=0
    )
)

::echo %jvm_args% %main_args%

set ORIG_DIR=%cd%
cd ../target

:: 10m max usage for 8 threads with NO UI. (imposed by the MPI api)
:: 22m max usage for 8 threads with UI.
set mem_alloc=22m
echo(!jvm_args!|find "-Dui.show=false" >nul && set mem_alloc=10m

:: Options to reduce mem usage and number of threads:
::  -Xss180k   Thread Stack Size
::  -XX:+UseCompressedClassPointers   (NOT VALID since JVM 11.0.6) 
::  -XX:ObjectAlignmentInBytes=n   (NOT VALID since JVM 11.0.6)
::  -XX:+UseCompressedOops   (NOT VALID since JVM 11.0.6) Enables the use of compressed pointers (object references represented as 32 bit offsets instead of 64-bit pointers) for optimized 64-bit performance with Java heap sizes less than 32gb.
::  -Dsun.rmi.transport.tcp.maxConnectionThreads=0   Removes the creation of 4 threads, however no CPU profiling is available.
::  -XX:+UseSerialGC   Disables Parallel or Concurrent GC.
::  -XX:CICompilerCount=2   Reduces JIT compiler threads (but you can set 1 is adding before -XX:-TieredCompilation).
::  -XX:+ReduceSignalUsage   Disables Signal Dispatcher thread. E.g. JVM will not handle SIGQUIT to dump threads.
::  -XX:+DisableAttachMechanism   Prevents AttachListener thread from starting.
:: Options to reduce thread presure:
::  -XX:+UseTLAB   To use thread-local object allocation
:: Options for performance:
::  -XX:+AlwaysPreTouch   Pre-touch the Java heap during JVM initialization. Every page of the heap is thus demand-zeroed during initialization rather than incrementally during application execution.
::  -XX:AllocatePrefetchStyle=n   Generated code style for prefetch instructions: 2= use TLAB allocation watermark pointer to gate when prefetch instructions are executed.
::  -XX:CompileThreshold=n   Number of method invocations/branches before compiling.
::  -XX:HeapBaseMinAddress=0
::  -XX:FreqInlineSize=600   Using 600 bytes as the threshold for "too big for inline" which corresponds to method exploracionStandard() with a size of 595 bytes
::  -XX:MaxRecursiveInlineLevel=0   Threshold to recursive method calls for inlining
set e2_jvm_opts=-Xss180k -XX:FreqInlineSize=600 -XX:MaxRecursiveInlineLevel=0 -XX:HeapBaseMinAddress=0 -XX:+AlwaysPreTouch -XX:CompileThreshold=100 -XX:+UseTLAB -XX:AllocatePrefetchStyle=2 -Dsun.rmi.transport.tcp.maxConnectionThreads=0 -XX:+UseSerialGC -XX:CICompilerCount=2 -XX:+ReduceSignalUsage -XX:+DisableAttachMechanism

set MPJ_HOME=%ORIG_DIR%/../target/libs/mpj-v0_44
set PATH=%PATH%;%MPJ_HOME%/bin

:: edit mpjrun.bat to select the desired JVM
%MPJ_HOME%/bin/mpjrun.bat -np %NUMBER_OF_PROCESSORS% ^
  %e2_jvm_opts% -Xms%mem_alloc% -Xmx%mem_alloc% %jvm_args% e2solver_mpje.jar %main_args% ^
  & chdir /d %ORIG_DIR%

endlocal
