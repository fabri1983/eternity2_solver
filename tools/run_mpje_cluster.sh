#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

orig_dir=$(pwd)
cd ../target
export MPJ_HOME="lib/mpj-v0_44"
# 40m max usage per VM instance
mem_alloc="40m"
# set the amount of total threads in the cluster. It has to be an homogeneous cluster
TOTAL_THREADS_IN_CLUSTER=8

# edit mpjrun.sh to select the desired JVM
$MPJ_HOME/bin/mpjrun.sh -np $TOTAL_THREADS_IN_CLUSTER -dev hybdev -Xms$mem_alloc -Xmx$mem_alloc -XX:MaxPermSize=512m $all_args e2solver_mpje.jar
