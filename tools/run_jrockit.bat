@echo off

:: they can only be -Dxxx arguments to the JVM
set ALL_ARGS=%*

set ORIG_DIR=%cd%
cd ../target
set java=C:/Java/jrockit-jdk1.6.0_45-R28.2.7-4.1.0/bin/java
set jsr166=lib/jsr166.jar
:: 900m max usage for 8 threads
set mem_alloc=900m

%java% -Xbootclasspath/p:%jsr166% -server -Xms%mem_alloc% -Xmx%mem_alloc% -XX:MaxPermSize=512m %ALL_ARGS% -jar e2solver_jrockit.jar %ORIG_DIR%/forkjoin.props

chdir /d %ORIG_DIR%
pause