#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

export MPJ_HOME=$(pwd)"/../target/libs/mpj-v0_44"
export PATH=$PATH:$MPJ_HOME/bin
orig_dir=$(pwd)
cd ../target

# 40m max usage per VM instance
mem_alloc="40m"

# edit mpjrun.sh to select the desired JVM
$MPJ_HOME/bin/mpjrun.sh -np $(nproc) -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xms$mem_alloc -Xmx$mem_alloc $all_args e2solver_mpje.jar
