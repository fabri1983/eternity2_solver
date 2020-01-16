#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

orig_dir=$(pwd)
cd ../target

# 2m max usage for 8 threads. Times 5 because we warm up 3 iterations and measure 2 iterations
mem_alloc="10m"

# Options to reduce mem usage and number of threads:
#  -Xss180k   Thread Stack Size
#  -XX:+UseCompressedClassPointers   (NOT VALID since JVM 11.0.6) 
#  -XX:ObjectAlignmentInBytes=n   (NOT VALID since JVM 11.0.6)
#  -XX:+UseCompressedOops   (NOT VALID since JVM 11.0.6) Enables the use of compressed pointers (object references represented as 32 bit offsets instead of 64-bit pointers) for optimized 64-bit performance with Java heap sizes less than 32gb.
#  -Dsun.rmi.transport.tcp.maxConnectionThreads=0   Removes the creation of 4 threads, however no CPU profiling is available.
#  -XX:+UseSerialGC   Disables Parallel or Concurrent GC.
#  -XX:CICompilerCount=2   Reduces JIT compiler threads (but you can set 1 is adding before -XX:-TieredCompilation).
#  -XX:+ReduceSignalUsage   Disables Signal Dispatcher thread. E.g. JVM will not handle SIGQUIT to dump threads.
#  -XX:+DisableAttachMechanism   Prevents AttachListener thread from starting.
# Options to reduce thread presure:
#  -XX:+UseTLAB   To use thread-local object allocation
# Options for performance:
#  -XX:+AlwaysPreTouch   Pre-touch the Java heap during JVM initialization. Every page of the heap is thus demand-zeroed during initialization rather than incrementally during application execution.
#  -XX:AllocatePrefetchStyle=n   Generated code style for prefetch instructions: 2= use TLAB allocation watermark pointer to gate when prefetch instructions are executed.
#  -XX:CompileThreshold=n   Number of method invocations/branches before compiling.
#  -XX:HeapBaseMinAddress=0
#  -XX:FreqInlineSize=600   Using 600 bytes as the threshold for "too big for inline" which corresponds to method exploracionStandard() with a size of 595 bytes
#  -XX:MaxRecursiveInlineLevel=0   Threshold to recursive method calls for inlining
e2_jvm_opts="-Xss180k -XX:FreqInlineSize=600 -XX:MaxRecursiveInlineLevel=0 -XX:HeapBaseMinAddress=0 -XX:+AlwaysPreTouch -XX:CompileThreshold=100 -XX:+UseTLAB -XX:AllocatePrefetchStyle=2 -Dsun.rmi.transport.tcp.maxConnectionThreads=0 -XX:+UseSerialGC -XX:CICompilerCount=2 -XX:+ReduceSignalUsage -XX:+DisableAttachMechanism"

java $e2_jvm_opts -Xms$mem_alloc -Xmx$mem_alloc $all_args -jar e2solver_benchmark.jar ; cd $orig_dir
