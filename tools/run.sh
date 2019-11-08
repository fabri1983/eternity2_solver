#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

orig_dir=$(pwd)
cd ../target

# 25m max usage for 8 threads
mem_alloc="25m"

java -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xms$mem_alloc -Xmx$mem_alloc $all_args -jar e2solver.jar
