#!/bin/sh

# they can only be -Dxxx arguments for the spawn processes
all_args=$@

export MPJ_HOME=$(pwd)"/../target/libs/mpj-v0_44"
export PATH=$PATH:$MPJ_HOME/bin
orig_dir=$(pwd)
cd ../target

# 40m max usage for 8 local process instances.
mem_alloc="40m"

# set the amount of total threads in the cluster. It has to be an homogeneous cluster
TOTAL_THREADS_IN_CLUSTER=8

# edit mpjrun.sh to select the desired JVM
$MPJ_HOME/bin/mpjrun.sh -np $TOTAL_THREADS_IN_CLUSTER -dev hybdev -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xms$mem_alloc -Xmx$mem_alloc $all_args e2solver_mpje.jar
