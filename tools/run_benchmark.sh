#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

orig_dir=$(pwd)
cd ../target

# 900m max usage for 8 threads
mem_alloc="900m"

java -Xms$mem_alloc -Xmx$mem_alloc $all_args -jar e2solver_benchmark.jar