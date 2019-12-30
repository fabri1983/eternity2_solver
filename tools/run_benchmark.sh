#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

orig_dir=$(pwd)
cd ../target

# 22m max usage for 8 threads. Times 5 because we warm up 3 iterations and measure 2 iterations
mem_alloc="110m"

# Options to reduce mem usage and number of threads:
#  -Dsun.rmi.transport.tcp.maxConnectionThreads=0 removes the creation of 4 threads, however no CPU profiling is available
#  -XX:+UseSerialGC disables Parallel or Concurrent GC
#  -XX:CICompilerCount=2 reduces JIT compiler threads (but you can set 1 is adding before -XX:-TieredCompilation).
#  -XX:+ReduceSignalUsage disables Signal Dispatcher thread. E.g. JVM will not handle SIGQUIT to dump threads.
#  -XX:+DisableAttachMechanism prevents AttachListener thread from starting.
e2_jvm_opts="-XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Dsun.rmi.transport.tcp.maxConnectionThreads=0 -XX:+UseSerialGC -XX:CICompilerCount=2 -XX:+ReduceSignalUsage -XX:+DisableAttachMechanism"

java $e2_jvm_opts -Xms$mem_alloc -Xmx$mem_alloc $all_args -jar e2solver_benchmark.jar
