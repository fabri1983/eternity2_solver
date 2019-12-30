eternity2_solver
==
Java implementation of a backtracker solver for the Eternity II puzzle game released in August 2007.  
Game finished in 2010 with no single person claiming the solution. Prize for any valid solution was 2 million usd.  

| Linux | Windows |
| ----- | ------- |
| [![Travis](https://travis-ci.org/fabri1983/eternity2_solver.svg?branch=master)](https://travis-ci.org/fabri1983/eternity2_solver?branch=master) | [![Appveyor](https://ci.appveyor.com/api/projects/status/38ua6hnrh6xtyi8j/branch/master?svg=true)](https://ci.appveyor.com/project/fabri1983/eternity2-solver/branch/master) |

![eternity solver mpje 8 threads image](misc/eternity_solver_mpje_x8.jpg?raw=true "eternity solver mpje 8 threads")  

- The project is managed with **Maven 3.6.x**. If you don´t want to download and install Maven then use local `mvnw` alternative.  
- It provides several jar artifacts from **Java 6 to 12**.  
- Additionally, a maven profile and scripting instructions to compile a **native image using Graal's SubstrateVM**.  

The backtracker efficiency is backed by:
- smart prunes
- clever data structures for quickly accessing data
- primitive arrays whenever possible to reduce memory usage
- bitwise operations
- micro optimizations

There are two versions of the same solver: one using a **fork-join pool** and other using **MPI (for distributed execution)**.  
The placement of tiles follows a row-scan schema from top-left to bottom-right.  

The project is under continuous development, mostly on spare time. Every time I come up with an idea, improvement, or code re-factor is for performance gain purposes. I focus on 2 main strategies:  
- *Speed of tiles placed by second after all filtering has taken place*. A piece is consider placed in the board after it passes a series of filters. 
Note that only pre calculated candidates are eligible for filtering. Here is where micro/macro optimizations and new clever filtering algorithms come into action.
- *Time needed to reach a given position (eg 211) with a fixed configuration (eg 8 threads)*. Given the fact that board positions, tiles, and filtering structures are visited always in the same order, this gives us a frame in which CPU processing capabiliy is decoupled from game logic. Here is where only micro/macro optimizations come into action.
  

Some stats
----------

- Environment: Windows 10 Home, Intel Core i7-2630QM (2.9 GHz max per core), DDR3 Dual Channel. OpenJDK 12. Results:  
Placing approx **66.8 million tiles per second** running with a fork-join pool **with 8 threads**.  
Placing approx **68.0 million tiles per second** using MPJ Express framework as multi-core mode **with 8 solver instances**.  
Placing approx **40.0 million tiles per second** running the native image generated with **GraalVM 19.2.0.1**, **with 8 threads**.  

- Environment: Windows 10 Pro, Intel Core i7 8650U (3.891 GHz max per core). OpenJDK 13. Results:  
Placing approx **97 million tiles per second** running with a fork-join pool **with 8 threads**.  
Placing approx **107 million tiles per second** using MPJ Express framework as multi-core mode **with 8 solver instances**.  

I still need to solve some miss cache issues by shrinking data size and change access patterns, thus maximizing data temporal and space locality.  

In the past, experiments showed me that execution was faster using the JRockit JVM from Oracle. I saw a 25% speed up.  
However new JVMs since 1.7 brought a gain in performance which made me leave the JRockit execution as historical and no more JVM flags tuning.  


Papers that have influenced algorithms and hacks used in the solver
-------------------------------------------------------------------

- How many edges can be shared by N square tiles on a board? [link](http://tbenoist.pagesperso-orange.fr/papers/HowManyEdges.pdf) *link's dead :(*  
Thierry Benoist, e-lab Research Report - April 2008.

- Fast Global Filtering for Eternity II [link](https://www.semanticscholar.org/paper/Fast-Global-Filtering-for-Eternity-II-Eric-Bourreau-Benoist-Bourreau/e16db8447bd1afa9d92a2899afb9ede53039ba16)  
Thierry Benoist, e-lab - Bouygues SA, Paris.  
Eric Bourreau, LIRMM, Montpellier.

- Jigsaw Puzzles, Edge Matching, and Polyomino Packing: Connections and Complexity [link](http://erikdemaine.org/papers/Jigsaw_GC/paper.pdf)  
Erik D. Demaine, Martin L. Demaine.  
MIT Computer Science and Artificial Intelligence Laboratory.

- Optimizing Hash-Array Mapped Tries for Fast and Lean Immutable JVM Collections (2017). [link](https://michael.steindorfer.name/publications/oopsla15.pdf)  
Michael J. Steindorfer - Centrum Wiskunde & Informatica, The Netherlands  
Jurgen J. Vinju - Centrum Wiskunde & Informatica, The Netherlands  

- Hacker’s Delight (2nd Edition) - 2013 [link](https://en.wikipedia.org/wiki/Hacker%27s_Delight)  
Henry S. Warren, Jr.  


Third party APIs
----------------
**MPJ Express**. http://mpj-express.org/.  
It is included in the project as a system dependency  

**jsr166**. https://www.jcp.org/en/jsr/detail?id=166.  
Is the java concurrent api for JVM 1.6 target builds.  
This api provides fork/join concurrency functionality to run the program on the Oracle JRockit VM.  

**ProGuard**. http://proguard.sourceforge.net/.  
Tool for shrink, obfuscate, and optimize code.  
With this tool I could **decrease jar file size by 20%**. **Code execution is 50% faster** on Windows box using MPJe.  
I'm still playing with the program options.  
Helpful links:  
 - http://www.alexeyshmalko.com/2014/proguard-real-world-example/
 - http://proguard.sourceforge.net/manual/usage.html
 - http://proguard.sourceforge.net/manual/examples.html


Generate Artifact
-----------------
*Note: if you don't have local Maven installation then use provided* `mvnw`.  
*Note: if you are using a JVM version 8 or smaller then you need to apply next changes in* `proguard.conf`: *uncomment* `rt.jar` and `jsse.jar`, *comment* `jmods`.    

Generate the jar artifact:  
```sh
mvn clean package
```
It generates the jar file with **default profile java8** and copy the external dependencies under target folder.  
Also by default it uses ProGuard code processing. Add `-Dproguard.skip=true` to generate simple java jar.    

**Profiles (use -P)**
- `java8`, `java11`: for execution with either JVM. Creates `e2solver.jar`.
- `jrockit`: intended for running on Oracle's JRockit JVM (the one that is java 1.6 version only). Creates `e2solver_jrockit.jar`.
- `mpje`: intended for running in cluster/multi-core environment using MPJExpress api. Currently compiles to java 1.8. Creates `e2solver_mpje.jar`.
- `java8native`, `java11native`: only intended for Graal SubstrateVM native image generation. Creates `e2solver.jar`.
- `benchmark`: generate an artifact containing JMH (Java Microbenchmarking Harness) api to benchmarking the core algorithm. Creates `e2solver_benchmark.jar`. **WIP**.


Execution
---------
First generate the artifact (previous section).  
Go under tools folder and use one of the runXXX commands.  
E.g.:
```sh
	cd tools
	./run.sh
```

The app loads by default the next properties (may vary between forkjoin and mpje profiles). You can pass only those you want to change:
```sh
	max.ciclos.save_status=2147483647
	min.pos.save.partial=211
	exploration.limit=-1
	max.partial.files=2
	target.rollback.pos=-1
	ui.show=true            <-- this has no effect on native build
	ui.per.proc=false       <-- this has no effect on native build
	ui.cell.size=28         <-- this has no effect on native build
	ui.refresh.millis=100   <-- this has no effect on native build
	experimental.gif.fair=false
	experimental.borde.left.explorado=false
	task.distribution.pos=99
	forkjoin.num.processes=8   <-- this has no effect on MPJE build
```
E.g.:
```sh
	./run.sh -Dmin.pos.save.partial=215 -Dforkjoin.num.processes=4
	./run_mpje_multicore.sh -Dmin.pos.save.partial=215     <-- it uses environment variable %NUMBER_OF_PROCESSORS% or $(nproc)
```

**NOTE**: if running on a Linux terminal with no X11 server then use `-Djava.awt.headless=true`.  

Use `run.bat/sh` for running the `e2solver.jar` package generated with profiles *java8*, and *java11*.  
Use `run_jrockit.bat/sh` for running the `e2solver_jrockit.jar` package generated with profile *jrockit*.  
Use `run_mpje_[multicore|cluster].bat/sh` for running the `e2solver_mpje.jar` package generated with profile *mpje*.  
Use `run_benchmark.bat/sh` for running the `e2solver_benchmark.jar` package generated with profile *benchmark*.  


Known issues
------------
*Affects some JVM 8 builds:*  
I'm having an exception when using the jpanel:  
`java.lang.ClassCastException: sun.awt.image.BufImgSurfaceData cannot be cast to sun.java2d.xr.XRSurfaceData`  
It seems to be a known issue: https://netbeans.org/bugzilla/show_bug.cgi?id=248774  
Work around for this issue if you are using OpenJDK 8 version prior to 112: *-Dsun.java2d.xrender=false*


Using jdeps on generated jar to build custom JRE (Java 9+)
----------------------------------------------------------
Use `jdeps` to know which java modules the final application needs to run.  
Note that we are using `--multi-release=12`.  
Then you can build a custom and smaller JRE.  

- *NOTE*: depending on the maven profile you use to geenrate the artifact the name may be one of: `e2solver.jar`, `e2solver_mpje`, `e2solver_jrockit`. 

- Windows:
```sh
jdeps --add-modules=ALL-MODULE-PATH --ignore-missing-deps --multi-release=12 --print-module-deps ^
  -cp target\libs\*;target\libs\javamail-1.4.5\lib\*;target\libs\mpj-v0_44\lib\* target\e2solver.jar
```

- Linux:
```sh
jdeps --add-modules=ALL-MODULE-PATH --ignore-missing-deps --multi-release=12 --print-module-deps \
  -cp target/libs/*:target/libs/javamail-1.4.5/lib/*:target/libs/mpj-v0_44/lib/* target/e2solver.jar
```

- Example Output:
```sh
java.base,java.desktop,java.management,java.naming,java.security.sasl,java.sql
```

- Use ouput modules to build a smaller JRE:
	- Windows:
	```sh
	jlink ^
	     --module-path %JAVA_HOME%\jmods ^
	     --compress=2 ^
	     --add-modules java.base,java.desktop,java.management,java.naming,java.security.sasl,java.sql ^
	     --no-header-files ^
	     --no-man-pages ^
	     --strip-debug ^
	     --output %JAVA_HOME%\customjre
	```

	- Linux:
	```sh
	jlink \
	     --module-path ${JAVA_HOME}/jmods \
	     --compress=2 \
	     --add-modules java.base,java.desktop,java.management,java.naming,java.security.sasl,java.sql \
	     --no-header-files \
	     --no-man-pages \
	     --strip-debug \
	     --output ${JAVA_HOME}/customjre
	```
The custom JRE is now located at %JAVA_HOME%/customjre folder. In order to use it you have to update your `JAVA_HOME` environment variable and `PATH` too.


Build a Graal VM on Windows and run your jar
--------------------------------------------
We are going to build Graal VM for Windows platform. **Only upto GraalVM 19.2.0.1 so far**.
- Download Open JDK 11: https://adoptopenjdk.net/releases.html?variant=openjdk11#x64_win.
- Or you can download Oracle JDK 11 from http://jdk.java.net/11/ (build 20 or later). This build has support for JVMCI (JVM Compiler Interface) which Graal depends on. 
- Environment variables will be set later with specific scripts.
- Install an Open JDK 1.8/11 (which already has support for JVMCI) or Windows GraalVM Early Adopter based on JDK 1.8/11 (with support for JVMCI):
	- https://github.com/graalvm/openjdk8-jvmci-builder/releases
	- https://github.com/graalvm/labs-openjdk-11
	- https://www.oracle.com/technetwork/graalvm/downloads/index.html   <-- (I use the java8 version)
- Setup mx (build assistant tool written in python)
	- create a mx directory and locate into it:
	```sh
	mkdir mx
	cd mx
	```
	- clone mx project:
	```sh
	git clone https://github.com/graalvm/mx.git .
	```
	- add binary to PATH:
	```sh
	SET PATH=%PATH%;%cd%
	```
	Also you can create MX_HOME env variable and add append it to PATH.
- Building Graal VM:
	- create a graal directory (outside the mx directory previously created) and locate into it:
	```sh
	mkdir graal
	cd graal
	```
	- clone graal project:
	```sh
	git clone https://github.com/oracle/graal.git .
	```
	- you will need python2.7 to be in your PATH.
	- build the Graal VM
	```sh
	SET JAVA_HOME=c:\java\openjdk-11.0.5+10
	SET EXTRA_JAVA_HOMES=c:\java\graalvm-ee-19.2.0.1
	cd compiler
	mx --disable-polyglot --disable-libpolyglot --dynamicimports /substratevm --skip-libraries=true build
	mx vm -version
	```
- Run your jar with Graal VM
	```sh
	mx -v vm -cp e2solver.jar org.fabri1983.eternity2.faster.MainFasterNative     <-- no UI support
	```
- **Optional**: Using the Graal compiler with your JVMCI enabled JVM:
Now we’re going to use the Graal that we just built as our JIT-compiler in our Java 11 JVM. We need to add some more complicated flags here.  
    
    --module-path=... and --upgrade-module-path=... add Graal to the module path.  
    Remember that the module path is new in Java 9 as part of the Jigsaw module system, and you can think of it as being like 
    the classpath for our purposes here.  
    
    We need -XX:+UnlockExperimentalVMOptions because JVMCI (the interface that Graal uses) is just experimental at this stage.  
    
    We then use -XX:+EnableJVMCI to say that we want to use JVMCI, and -XX:+UseJVMCICompiler to say that we actually want to use 
    it and to install a new JIT compiler.  
    By default, Graal is only used for hosted compilation (i.e., the VM still uses C2 for compilation).  
    To make the VM use Graal as the top tier JIT compiler, add the -XX:+UseJVMCICompiler option to the command line.  
    To disable use of Graal altogether, use -XX:-EnableJVMCI.  
    	
    We use -XX:-TieredCompilation to disable tiered compilation to keep things simpler and to just have the one JVMCI compiler, 
    rather than using C1 and then the JVMCI compiler in tiered compilation.  

    In order to use the AOT compiled (native) GraalVM JIT compiler when loading the JVM Hotspot, use the following options:  
	-XX:+UseJVMCICompiler -XX:+UseJVMCINativeLibrary

- See also https://github.com/neomatrix369/awesome-graal/tree/master/build/x86_64/linux_macos


Build a native image using Graal's SubstrateVM on Windows
---------------------------------------------------------
We are going to generate a native image to run our solver. No UI supported by the moment. **Only upto GraalVM 19.2.0.1 so far**.
- Install an Open JDK 1.8/11 (which already has support for JVMCI) or Windows GraalVM Early Adopter based on JDK 1.8/11 (with support for JVMCI):
	- https://github.com/graalvm/openjdk8-jvmci-builder/releases
	- https://github.com/graalvm/labs-openjdk-11
	- https://www.oracle.com/technetwork/graalvm/downloads/index.html   <-- (I use the java8 version)
- You will need Python 2.7 (https://www.python.org/downloads/release/python-2715/) and Windows SDK for Windows 7 (https://www.microsoft.com/en-us/download/details.aspx?id=8442).
This will help you to decide which iso you need to download:
	- GRMSDK_EN_DVD.iso is a version for x86 environment.
	- GRMSDKIAI_EN_DVD.iso is a version for Itanium environment.
	- GRMSDKX_EN_DVD.iso is a version for x64 environment. (This one is for an AMD64 architecture)
- Install Windows SDK for Windows 7 from the download ISO.
	- If setup shows a warning message saying missing NET 4.x Framework tools then ignore it, and once installed install this:
		https://www.microsoft.com/en-us/download/details.aspx?id=4422
	- See this link for troubleshooting installation issues:
		https://stackoverflow.com/questions/32091593/cannot-install-windows-sdk-7-1-on-windows-10
- Download and setup mx tool, and add it to your PATH environment variable. Also you can create MX_HOME env variable and add append it to PATH. See previous section *Usage of Graal Compiler on Windows*.
- Download Graal project and build the Substrate VM and build a simple Hello World example:
	```sh
	Open a console:
		open the Windows SDK 7.1 Command Prompt going to Start -> Programs -> Microsoft Windows SDK v7.1
		or
		open a cmd console and run "C:\Program Files\Microsoft SDKs\Windows\v7.1\Bin\SetEnv.cmd"
	SET JAVA_HOME=C:\java\graalvm-ee-19.2.0.1
	cd substratevm
	mx build --all
	echo public class HelloWorld { public static void main(String[] args) { System.out.println("Hello World"); } } > HelloWorld.java
	%JAVA_HOME%/bin/javac HelloWorld.java
	mx native-image --verbose HelloWorld
	HelloWorld
	```
- Building a native image for eternity 2 solver:
	- Build the project with the native profile (select the one according your installed version of GraalVM):
	```sh
	mvn clean package -P java8native
	or
	mvn clean package -P java11native
	```
	- Build the static image
	```
	cd target
	mx native-image --static --no-fallback --report-unsupported-elements-at-runtime -H:Optimize=2 -H:CPUFeatures=HT,MMX,SSE,SSE2,SSE3,SSSE3,SSE4_1,SSE4_2,AES,AVX -H:+ReportExceptionStackTraces -J-Xms1700m -J-Xmx1700m -H:InitialCollectionPolicy="com.oracle.svm.core.genscavenge.CollectionPolicy$BySpaceAndTime" -H:IncludeResources=".*application.properties|.*e2pieces.txt" -jar e2solver.jar
	e2solver.exe -Dforkjoin.num.processes=8 -Dmin.pos.save.partial=211
	Times for position 215 and 4 processes:
		1 >>> 3232154 ms, cursor 215  (53.8 mins)
		0 >>> 3272859 ms, cursor 215  (54.5 mins)
	```
	There is also a possible optimization feature named Profile Guided Optimization:
	```sh
	mx native-image --pgo-instrument <same params than above>
	execute the executable for some seconds:
		e2solver.exe -Dforkjoin.num.processes=8 -Dmin.pos.save.partial=211
	mx native-image --pgo=default.iprof <same params than above>
	execute again and see if there is an improvement in execution speed
	```
- Tips:
	- Use *-H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy$BySpaceAndTime* for long lived processes.
	- Use *--report-unsupported-elements-at-runtime* to see which elements are not visible ahead of time for Graal since they are not explicitely declared in the classpath.
	- Use *-H:+ReportExceptionStackTraces* to better understand any exception during image generation.
	- See this article's sections *Incomplete classpath* and *Delayed class initialization*: https://medium.com/graalvm/instant-netty-startup-using-graalvm-native-image-generation-ed6f14ff7692. Option is *--allow-incomplete-classpath*.
	- See this article which solves lot of common problems: https://royvanrijn.com/blog/2018/09/part-2-native-microservice-in-graalvm/
	- To avoid the error *Class XXX cannot be instantiated reflectively . It does not have a nullary constructor* you can disable the ServiceLoaderFeature with -H:-UseServiceLoaderFeature. That's where this is triggered from. You can also use -H:+TraceServiceLoaderFeature to see all the classes processed by this feature.
	- 

Running with Avian JVM
----------------------
I'm trying to improve the performance of code execution using other JVM implementations.
Currently I'm taking a look to Avian JVM, under Windows environment.

Visit page http://oss.readytalk.com/avian/ to know what Avian is all about.

- Install cygwin following the steps mentioned in https://github.com/ReadyTalk/avian/ (README.md file).
	- you will need to add some packages that aren't set as default (the instructions are there)
	- also need to add curl
	- also need ncurses (for clear command, or use ctrl+l)
- Set JAVA_HOME environment variable in your .bashrc file
	- export JAVA_HOME=/cygdrive/c/java/jdk1.8
- Once cygwin is installed you need to clone avian, win32, and win64 repos.
	- open cygwin terminal
	- create a folder named avian_jvm wherever you want (eg: in /home/<user>/avian_jvm)
	- open the cygwin terminal and locate into created dir
	- git clone git://github.com/ReadyTalk/avian
	- git clone git://github.com/ReadyTalk/win64
	- git clone git://github.com/ReadyTalk/win32
- Locate in avian dir and build it using make command
- Run hello world test program (which is an already compiled class file):
	- build/windows-x86_64/avian -cp build/windows-x86_64/test Hello
- You can try a swt example:
	- download the exe from http://oss.readytalk.com/avian/examples.html
	- or build it and execute it locally. Example for win64:
		- export platform=windows-x86_64
		- export swt_zip=swt-4.3-win32-win32-x86_64.zip
		- mkdir swt-example
		- cd swt-example
		- curl -Of http://oss.readytalk.com/avian-web/proguard4.11.tar.gz
		- tar xzf proguard4.11.tar.gz
		- curl -Of http://oss.readytalk.com/avian-web/lzma920.tar.bz2
		- mkdir -p lzma-920
		- cd lzma-920
		- tar xjf ../lzma920.tar.bz2
		- cd ..
		- curl -Of http://oss.readytalk.com/avian-web/${swt_zip}
		- mkdir -p swt/${platform}
		- unzip -d swt/${platform} ${swt_zip}
		- curl -Of http://oss.readytalk.com/avian-web/avian-swt-examples-1.0.tar.bz2
		- tar xjf avian-swt-examples-1.0.tar.bz2
		- we need to copy lzma-920/C folder to avian_jvm/avian/src/ folder because it won't compile otherwise
			- cp -ar lzma-920/C ../avian/src/ 
		- cd avian-swt-examples
		- once in avian-swt-examples folder edit files:
			- makefile: at line 53 add /.. to current cd .. command since avian folder is one more level up.
			- app.mk: locate root variable and change other variables that use root just adding ../ because avian, win32, and win64 folders are 1 additional level up.
		- build example exe file with lzma (compressed exe) or without it
			- make lzma=$(pwd)/../lzma-920 full-platform=${platform} example
			- or
			- make full-platform=${platform} example
		- exe file are created at avian-swt-examples/build/windows-x86_64-lzma/ and in avian-swt-examples/build/windows-x86_64/ respectively.
		- You can omit example target to let other targets be built: example, graphics, and paint


