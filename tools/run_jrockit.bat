@echo off

:: they can only be -Dxxx arguments to the JVM
set ALL_ARGS=%*

set ORIG_DIR=%cd%
cd ../target

set java=C:/java/jrockit-jdk1.6.0_45-R28.2.7-4.1.0/bin/java
set jsr166=lib/jsr166.jar

:: 6m max usage for 8 threads
set mem_alloc=6m

:: Options to reduce mem usage and number of threads:
::  -XX:+UseCompressedClassPointers 
::  -XX:ObjectAlignmentInBytes=n
::  -XX:+UseCompressedOops Enables the use of compressed pointers (object references represented as 32 bit offsets instead of 64-bit pointers) for optimized 64-bit performance with Java heap sizes less than 32gb.
::  -Dsun.rmi.transport.tcp.maxConnectionThreads=0 removes the creation of 4 threads, however no CPU profiling is available.
::  -XX:+UseSerialGC disables Parallel or Concurrent GC.
::  -XX:CICompilerCount=2 reduces JIT compiler threads (but you can set 1 is adding before -XX:-TieredCompilation).
::  -XX:+ReduceSignalUsage disables Signal Dispatcher thread. E.g. JVM will not handle SIGQUIT to dump threads.
::  -XX:+DisableAttachMechanism prevents AttachListener thread from starting.
:: Options to reduce thread presure:
::  -XX:+UseTLAB to use thread-local object allocation
:: Options for performance:
::  -XX:+AlwaysPreTouch Pre-touch the Java heap during JVM initialization. Every page of the heap is thus demand-zeroed during initialization rather than incrementally during application execution.
::  -XX:AllocatePrefetchStyle=n Generated code style for prefetch instructions: 2= use TLAB allocation watermark pointer to gate when prefetch instructions are executed.
::  -XX:CompileThreshold=n Number of method invocations/branches before compiling.
::  -XX:HeapBaseMinAddress=0
set e2_jvm_opts=-XX:HeapBaseMinAddress=0 -XX:ObjectAlignmentInBytes=8 -XX:+AlwaysPreTouch -XX:CompileThreshold=100 -XX:+UseTLAB -XX:AllocatePrefetchStyle=2 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Dsun.rmi.transport.tcp.maxConnectionThreads=0 -XX:+UseSerialGC -XX:CICompilerCount=2 -XX:+ReduceSignalUsage -XX:+DisableAttachMechanism

%java% %e2_jvm_opts% -Xbootclasspath/p:%jsr166% -server -Xms%mem_alloc% -Xmx%mem_alloc% %ALL_ARGS% -jar e2solver_jrockit.jar & chdir /d %ORIG_DIR%
