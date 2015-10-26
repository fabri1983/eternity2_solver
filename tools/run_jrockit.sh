#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

orig_dir=$(pwd)
cd ../target

java="/usr/local/java/jrockit-jdk1.6.0_45-R28.2.7-4.1.0/bin/java"
jsr166="lib/jsr166.jar"
# 900m max usage for 8 threads
mem_alloc="900m"

java -Xbootclasspath/p:$jsr166 -server -Xms$mem_alloc -Xmx$mem_alloc -XX:MaxPermSize=512m $all_args -jar e2solver_jrockit.jar