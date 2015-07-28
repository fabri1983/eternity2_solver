#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

orig_dir=$(pwd)
cd ../target
export MPJ_HOME="lib/mpj-v0_44"
# 40m max usage per VM instance
mem_alloc="40m"

# edit mpjrun.sh to select the desired JVM
$MPJ_HOME/bin/mpjrun.sh -np $(nproc) -Xms$mem_alloc -Xmx$mem_alloc -XX:MaxPermSize=512m $all_args e2solver_mpje.jar $orig_dir/mpje.props