#!/bin/sh

# they can only be -Dxxx arguments for the spawn processes
all_args=$@

export MPJ_HOME=$(pwd)"/../target/libs/mpj-v0_44"
export PATH=$PATH:$MPJ_HOME/bin
orig_dir=$(pwd)
cd ../target

# 22m max usage for 8 local process instances.
mem_alloc="22m"

# Options to reduce mem usage and number of threads:
#  -Dsun.rmi.transport.tcp.maxConnectionThreads=0 removes the creation of 4 threads, however no CPU profiling is available
#  -XX:+UseSerialGC disables Parallel or Concurrent GC
#  -XX:CICompilerCount=2 reduces JIT compiler threads (but you can set 1 is adding before -XX:-TieredCompilation).
#  -XX:+ReduceSignalUsage disables Signal Dispatcher thread. E.g. JVM will not handle SIGQUIT to dump threads.
#  -XX:+DisableAttachMechanism prevents AttachListener thread from starting.
e2_jvm_opts="-XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Dsun.rmi.transport.tcp.maxConnectionThreads=0 -XX:+UseSerialGC -XX:CICompilerCount=2 -XX:+ReduceSignalUsage -XX:+DisableAttachMechanism"

# edit mpjrun.sh to select the desired JVM
$MPJ_HOME/bin/mpjrun.sh -np $(nproc) $e2_jvm_opts -Xms$mem_alloc -Xmx$mem_alloc $all_args e2solver_mpje.jar
