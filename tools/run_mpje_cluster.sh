#!/bin/sh

# they can only be -Dxxx arguments for the spawn processes
all_args=$@

export MPJ_HOME=$(pwd)"/../target/libs/mpj-v0_44"
export PATH=$PATH:$MPJ_HOME/bin
orig_dir=$(pwd)
cd ../target

# 20m max usage for 8 threads with NO UI.
# 25m max usage for 8 threads with UI.
mem_alloc="20m"

# Options to reduce mem usage and number of threads:
#  -Xss180k   Thread Stack Size
#  -XX:+UseCompressedClassPointers 
#  -XX:ObjectAlignmentInBytes=n
#  -XX:+UseCompressedOops   Enables the use of compressed pointers (object references represented as 32 bit offsets instead of 64-bit pointers) for optimized 64-bit performance with Java heap sizes less than 32gb.
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
e2_jvm_opts="-Xss180k -XX:FreqInlineSize=600 -XX:MaxRecursiveInlineLevel=0 -XX:HeapBaseMinAddress=0 -XX:ObjectAlignmentInBytes=8 -XX:+AlwaysPreTouch -XX:CompileThreshold=100 -XX:+UseTLAB -XX:AllocatePrefetchStyle=2 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Dsun.rmi.transport.tcp.maxConnectionThreads=0 -XX:+UseSerialGC -XX:CICompilerCount=2 -XX:+ReduceSignalUsage -XX:+DisableAttachMechanism"

# set the amount of total threads in the cluster. It has to be an homogeneous cluster
TOTAL_THREADS_IN_CLUSTER=8

# edit mpjrun.sh to select the desired JVM
$MPJ_HOME/bin/mpjrun.sh -np $TOTAL_THREADS_IN_CLUSTER -dev hybdev $e2_jvm_opts -Xms$mem_alloc -Xmx$mem_alloc $all_args e2solver_mpje.jar ; cd $orig_dir
