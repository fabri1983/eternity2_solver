#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

export MPJ_HOME=$(pwd)"/../external-libs/mpj-v0_44"
export PATH=$PATH:$MPJ_HOME/bin
orig_dir=$(pwd)
cd ../target
# 50m max usage per VM instance. For GraalVM it needs 60m.
mem_alloc="60m"
# set the amount of total threads in the cluster. It has to be an homogeneous cluster
TOTAL_THREADS_IN_CLUSTER=8

# edit mpjrun.sh to select the desired JVM
$MPJ_HOME/bin/mpjrun.sh -np $TOTAL_THREADS_IN_CLUSTER -dev hybdev -Xms$mem_alloc -Xmx$mem_alloc $all_args e2solver_mpje.jar
