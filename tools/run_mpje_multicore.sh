#!/bin/sh

cd ../target
export MPJ_HOME="lib/mpj-v0_44"
# 40m max usage per VM instance
mem_alloc="40m"

# edit mpjrun.sh to select the desired JVM
$MPJ_HOME/bin/mpjrun.sh -np $(nproc) -Xms$mem_alloc -Xmx$mem_alloc -XX:MaxPermSize=512m e2solver_mpje.jar 12147483647 211 -1 2 -1 true true 28 100 false false 99