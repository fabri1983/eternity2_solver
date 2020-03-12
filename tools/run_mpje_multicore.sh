#!/bin/sh

# they can only be -Dxxx arguments for the spawn processes
all_args=$@

export MPJ_HOME=$(pwd)"/../target/libs/mpj-v0_44"
export PATH=$PATH:$MPJ_HOME/bin
orig_dir=$(pwd)
cd ../target

# 10m max usage for 8 threads with NO UI. (imposed by the MPI api)
# 22m max usage for 8 threads with UI.
mem_alloc="22m"
no_ui_options="-Djava.awt.headless=true -Dsun.java2d.xrender=false"

# Options to enable SerialGC and its configuration for minor usage:
#  -XX:+UseSerialGC   Disables Parallel or Concurrent GC. It uses just 1 thread.
#  -XX:NewRatio=1   Ratio between tenured space vs. new generation space. 1 means: both spaces have 1/2 of the heap size. 2 means: 1/3 for new gen and 2/3 for tenured. 3 means: 1/4 for new gen and 3/4 for tenured.
#  -XX:SurvivorRatio=1   Ratio between eden space and 1 of the survivor space. 1 means: 1/2 for eden, 1/2 divided by 2 for S0 and S1. 2 means: 2/3 for eden, 1/3 divided by 2 for S0 and S1
#  -XX:TargetSurvivorRatio=5   Maximum survivor space usage (in percentage). When this limit is reached, all remaining live objects will be promoted to Tenured generation regardless of their age. Small values mean premature tenuring
#  -XX:InitiatingHeapOccupancyPercent=99   The Old gen space can become crowded. At a certain percentage (45% as default) of total heap, a mixed GC is triggered. It collects both Young gen and Old gen. It generates pauses.
# Options to reduce mem usage and number of threads:
#  -Xss180k   Thread Stack Size
#  -XX:+UseCompressedClassPointers   (NOT VALID since JVM 11.0.6) 
#  -XX:ObjectAlignmentInBytes=n   (NOT VALID since JVM 11.0.6)
#  -XX:+UseCompressedOops   (NOT VALID since JVM 11.0.6) Enables the use of compressed pointers (object references represented as 32 bit offsets instead of 64-bit pointers) for optimized 64-bit performance with Java heap sizes less than 32gb.
#  -Dsun.rmi.transport.tcp.maxConnectionThreads=0   Removes the creation of 4 threads, however no CPU profiling is available.
#  -XX:CICompilerCount=2   Reduces JIT compiler threads (you can't set 1 if not adding before -XX:-TieredCompilation).
#  -XX:+ReduceSignalUsage   Disables Signal Dispatcher thread. E.g. JVM will not handle SIGQUIT to dump threads.
#  -XX:+DisableAttachMechanism   Prevents AttachListener thread from starting.
# Options to reduce thread presure:
#  -XX:+UseTLAB   To use thread-local object allocation
#  -XX:-ResizePLAB   Promotion Local Allocation Buffers (PLABs) are used during Young collection. Each GC thread may need to allocate space for objects being copied either in Survivor or Old space. PLABs are required to avoid competition of threads for shared data structures that manage free memory. Each GC thread has one PLAB for Survival space and one for Old space. Disabling the feature stops resizing PLABs to avoid the large communication cost among GC threads, as well as variations during each GC.
# Options for performance:
#  -XX:+AlwaysPreTouch   Pre-touch the Java heap during JVM initialization. Every page of the heap is thus demand-zeroed during initialization rather than incrementally during application execution.
#  -XX:AllocatePrefetchStyle=n   Generated code style for prefetch instructions: 2= use TLAB allocation watermark pointer to gate when prefetch instructions are executed.
#  -XX:CompileThreshold=n   Number of method invocations/branches before compiling.
#  -XX:HeapBaseMinAddress=0
#  -XX:FreqInlineSize=600   Using 600 bytes as the threshold for "too big for inline" which corresponds to method exploracionStandard() with a size of 595 bytes
#  -XX:MaxRecursiveInlineLevel=0   Threshold to recursive method calls for inlining
e2_jvm_opts="$no_ui_options -XX:+UseSerialGC -XX:NewRatio=1 -XX:SurvivorRatio=1 -XX:TargetSurvivorRatio=5 -XX:InitiatingHeapOccupancyPercent=99 -Xss180k -XX:FreqInlineSize=600 -XX:MaxRecursiveInlineLevel=0 -XX:HeapBaseMinAddress=0 -XX:+AlwaysPreTouch -XX:CompileThreshold=100 -XX:+UseTLAB -XX:-ResizePLAB -XX:AllocatePrefetchStyle=2 -Dsun.rmi.transport.tcp.maxConnectionThreads=0 -XX:CICompilerCount=2 -XX:+ReduceSignalUsage -XX:+DisableAttachMechanism"

# edit mpjrun.sh to select the desired JVM
$MPJ_HOME/bin/mpjrun.sh -np $(nproc) $e2_jvm_opts -Xms$mem_alloc -Xmx$mem_alloc $all_args e2solver_mpje.jar ; cd $orig_dir
