#!/bin/sh

# they can only be -Dxxx arguments to the JVM
all_args=$@

orig_dir=$(pwd)
cd ../target

java="/usr/local/java/jrockit-jdk1.6.0_45-R28.2.7-4.1.0/bin/java"
jsr166="lib/jsr166.jar"

# 40m max usage for 8 threads
mem_alloc="40m"

java -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xbootclasspath/p:$jsr166 -server -Xms$mem_alloc -Xmx$mem_alloc $all_args -jar e2solver_jrockit.jar
