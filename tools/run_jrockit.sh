#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

orig_dir=$(pwd)
cd ../target

java="/usr/local/java/jrockit-jdk1.6.0_45-R28.2.7-4.1.0/bin/java"
jsr166="lib/jsr166.jar"

# 22m max usage for 8 threads
mem_alloc="22m"

# Options to reduce mem usage and number of threads:
#  -Dsun.rmi.transport.tcp.maxConnectionThreads=0 removes the creation of 4 threads, however no CPU profiling is available
#  -XX:+UseSerialGC disables Parallel or Concurrent GC
#  -XX:CICompilerCount=2 reduces JIT compiler threads (but you can set 1 is adding before -XX:-TieredCompilation).
#  -XX:+ReduceSignalUsage disables Signal Dispatcher thread. E.g. JVM will not handle SIGQUIT to dump threads.
#  -XX:+DisableAttachMechanism prevents AttachListener thread from starting.
e2_jvm_opts="-XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Dsun.rmi.transport.tcp.maxConnectionThreads=0 -XX:+UseSerialGC -XX:CICompilerCount=2 -XX:+ReduceSignalUsage -XX:+DisableAttachMechanism"

java $e2_jvm_opts -Xbootclasspath/p:$jsr166 -server -Xms$mem_alloc -Xmx$mem_alloc $all_args -jar e2solver_jrockit.jar
