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

:: Options to enable SerialGC and its configuration for minor usage:
::  -XX:+UseSerialGC   Disables Parallel or Concurrent GC. It uses just 1 thread.
::  -XX:NewRatio=1   Ratio between tenured space vs. new generation space. 1 means: both spaces have 1/2 of the heap size. 2 means: 1/3 for new gen and 2/3 for tenured. 3 means: 1/4 for new gen and 3/4 for tenured.
::  -XX:SurvivorRatio=1   Ratio between eden space and 1 of the survivor space. 1 means: 1/2 for eden, 1/2 divided by 2 for S0 and S1. 2 means: 2/3 for eden, 1/3 divided by 2 for S0 and S1
::  -XX:TargetSurvivorRatio=5   Maximum survivor space usage (in percentage). When this limit is reached, all remaining live objects will be promoted to Tenured generation regardless of their age. Small values mean premature tenuring
::  -XX:InitiatingHeapOccupancyPercent=99   The Old gen space can become crowded. At a certain percentage (45% as default) of total heap, a mixed GC is triggered. It collects both Young gen and Old gen. It generates pauses.
:: Options to reduce mem usage and number of threads:
::  -Xss180k   Thread Stack Size
::  -XX:+UseCompressedClassPointers   (NOT VALID since JVM 11.0.6) 
::  -XX:ObjectAlignmentInBytes=n   (NOT VALID since JVM 11.0.6)
::  -XX:+UseCompressedOops   (NOT VALID since JVM 11.0.6) Enables the use of compressed pointers (object references represented as 32 bit offsets instead of 64-bit pointers) for optimized 64-bit performance with Java heap sizes less than 32gb.
::  -Dsun.rmi.transport.tcp.maxConnectionThreads=0   Removes the creation of 4 threads, however no CPU profiling is available.
::  -XX:CICompilerCount=2   Reduces JIT compiler threads (you can't set 1 if not adding before -XX:-TieredCompilation).
::  -XX:+ReduceSignalUsage   Disables Signal Dispatcher thread. E.g. JVM will not handle SIGQUIT to dump threads.
::  -XX:+DisableAttachMechanism   Prevents AttachListener thread from starting.
:: Options to reduce thread presure:
::  -XX:+UseTLAB   To use thread-local object allocation
::  -XX:-ResizePLAB   Promotion Local Allocation Buffers (PLABs) are used during Young collection. Each GC thread may need to allocate space for objects being copied either in Survivor or Old space. PLABs are required to avoid competition of threads for shared data structures that manage free memory. Each GC thread has one PLAB for Survival space and one for Old space. Disabling the feature stops resizing PLABs to avoid the large communication cost among GC threads, as well as variations during each GC.
:: Options for performance:
::  -XX:+AlwaysPreTouch   Pre-touch the Java heap during JVM initialization. Every page of the heap is thus demand-zeroed during initialization rather than incrementally during application execution.
::  -XX:AllocatePrefetchStyle=n   Generated code style for prefetch instructions: 2= use TLAB allocation watermark pointer to gate when prefetch instructions are executed.
::  -XX:CompileThreshold=n   Number of method invocations/branches before compiling.
::  -XX:HeapBaseMinAddress=0
::  -XX:FreqInlineSize=600   Using 600 bytes as the threshold for "too big for inline" which corresponds to method exploracionStandard() with a size of 595 bytes
::  -XX:MaxRecursiveInlineLevel=0   Threshold to recursive method calls for inlining
set e2_jvm_opts=-XX:+UseSerialGC -XX:NewRatio=1 -XX:SurvivorRatio=1 -XX:TargetSurvivorRatio=5 -XX:InitiatingHeapOccupancyPercent=99 -Xss180k -XX:FreqInlineSize=600 -XX:MaxRecursiveInlineLevel=0 -XX:HeapBaseMinAddress=0 -XX:+AlwaysPreTouch -XX:CompileThreshold=100 -XX:+UseTLAB -XX:-ResizePLAB -XX:AllocatePrefetchStyle=2 -Dsun.rmi.transport.tcp.maxConnectionThreads=0 -XX:CICompilerCount=2 -XX:+ReduceSignalUsage -XX:+DisableAttachMechanism

set MPJ_HOME=%ORIG_DIR%/../target/libs/mpj-v0_44
set PATH=%PATH%;%MPJ_HOME%/bin

:: edit mpjrun.bat to select the desired JVM
%MPJ_HOME%/bin/mpjrun.bat -np %NUMBER_OF_PROCESSORS% ^
  %e2_jvm_opts% -Xms%mem_alloc% -Xmx%mem_alloc% %jvm_args% e2solver_mpje.jar %main_args% ^
  & chdir /d %ORIG_DIR%

endlocal
