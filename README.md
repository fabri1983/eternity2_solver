eternity2_solver
==
Java implementation of a backtracker solver for the Eternity II puzzle game released in August 2007.  
Game finished in 2010 with no single person claiming the solution. Prize for any valid solution was 2 million usd.  

| Linux | Windows |
| ----- | ------- |
| [![Travis](https://app.travis-ci.com/fabri1983/eternity2_solver.svg?branch=master)](https://app.travis-ci.com/fabri1983/eternity2_solver?branch=master) | [![Appveyor](https://ci.appveyor.com/api/projects/status/38ua6hnrh6xtyi8j/branch/master?svg=true)](https://ci.appveyor.com/project/fabri1983/eternity2-solver/branch/master) |

![eternity solver mpje 8 threads image](misc/eternity_solver_mpje_x8.png?raw=true "eternity2 solver mpje 8 processes with UI enabled")  

- The project is managed with **Maven 3.6.x**. If you don't want to download and install Maven then use local `mvnw` alternative.  
- It provides several jar artifacts for **Java 8, 11, 17**, and a benchmark artifact with **JMH** *(Java Microbenchmark Harness)*.  
- Additionally, there are other maven profiles and scripting instructions to compile to a **native image using Graal's SubstrateVM**.  

The backtracker efficiency is backed by:
- smart prunes: parity check and patterns of placed tiles which don't allow to generate the same pattern again.
- clever data structures for quickly accessing data: matrix of neighbour tiles and mask matrices.
- primitive arrays whenever possible to reduce memory usage and better local data access.
- *bitwise operations* and *micro optimizations*.
- *minimal perfect hash function* to quickly access neighbour tiles.
- *sparse bit set* from [Brett Wooldridge](https://github.com/brettwooldridge/SparseBitSet) as a faster test for neighbour existence.
- lot of *JVM flag tweaks* to reduce thread pressure, GC pressure, use of JIT compiler parameters, etc.

There are two versions of the same solver: 
- one using a **thread pool** to spawn as many tasks as logic cores the runtime platform provides (is configurable).
- another using **MPI** *(for distributed execution)*.

The placement of tiles follows a *row-scan schema* from *top-left* to *bottom-right*.  

The project is under continuous development, mostly on spare time. 
Every time I come up with an idea, improvement, or code refactor is for performance gain purpose. 
I'm focused on 2 main strategies:  
- *Speed of tiles placed by second after all filtering has taken place*. A piece is consider placed in the board 
after it passes a series of filters. Note that only pre calculated candidates are eligible for filtering. Here is 
where micro/macro optimizations and new clever filtering algorithms come into action.
- *Time needed to reach a given board position (eg 211) with a fixed configuration (eg 8 threads)*. Given the fact 
that board positions, tiles, and filtering structures are visited always in the same order, this gives us a frame 
in which CPU processing capabiliy is decoupled from game logic. Here is where micro/macro optimizations come into action.


Some stats
----------
- Counting correct tiles per second. A correct tile is such one that passed all filtering:
  - Environment: Windows 10 Home, Intel Core i7-2630QM (2.9 GHz max per core), DDR3 666MHz. OpenkJDK 1.8.0_242-b06 (compiled and executed). Results:
    - Approx **84.27 million correct tiles per second** running with a pool of **8 threads**.
    - *(outdated)* Approx **85.89 million correct tiles per second** using MPJ Express framework as multi-core mode **with 8 solver instances**.
  - Environment: Windows 10 Home, Intel Core i7-2630QM (2.9 GHz max per core), DDR3 666MHz. OpenkJDK 11.0.7-10 (compiled and executed). Results:
    - Approx **84.27 million correct tiles per second** running with a pool of **8 threads**.
    - *(outdated)* Approx **85.89 million correct tiles per second** using MPJ Express framework as multi-core mode **with 8 solver instances**.
  - Native images stats:
    - *(outdated)* Approx **61.32 million correct tiles per second** running the native image generated with **GraalVM 20.1.0 Java8 EE**, **with 8 threads**.
    - *(outdated)* Approx **62.52 million correct tiles per second** running the native image generated with **GraalVM 20.1.0 Java11 EE**, **with 8 threads**.

I still need to solve some miss cache issues by shrinking data size and change access patterns, thus maximizing data temporal and space locality.  


Papers and lectures that have influenced algorithms and hacks used in the solver
--------------------------------------------------------------------------------

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
Jurgen J. Vinju - Centrum Wiskunde & Informatica, The Netherlands.

- An Optimal Algorithm For Generating Minimal Perfect Hashing Functions. [link](http://http://cmph.sourceforge.net/papers/chm92.pdf)  
George Havas and Bohdan S. Majewski.  
Key Centre for Software Technology Department of Computer Science.  
University of Queensland, Australia.

- RecSplit [link](https://github.com/thomasmueller/minperf/blob/master/src/test/java/org/minperf/simple/recsplit.md)  
A faster algorithm for Minimal Perfect Hash Function.  

- Minimal Perfect Hashing by Bob Jenkins [link](https://burtleburtle.net/bob/hash/perfect.html)  
Minimal Perfect Hashing tool for C code generation.  
Updated version compatible with MSVC compilers [here](https://github.com/driedfruit/jenkins-minimal-perfect-hash).  

- Quasi-Succinct Indices or *The Revenge of Elias and Fano* [link](https://shonan.nii.ac.jp/archives/seminar/029/wp-content/uploads/sites/12/2013/07/Sebastiano_Shonan.pdf)  
Sebastiano Vigna. 2013. Quasi-succinct indices.  
In Proceedings of the sixth ACM international conference on Web search and data mining (WSDM '13).  
ACM, New York, NY, USA, 83-92.  
Simple Java implementation: https://github.com/catenamatteo/eliasfano  

- Compressed BitMap alternatives other than java BitSet.  
Java EWAH [link](https://github.com/lemire/javaewah)  
Raoring Bitmap [link](https://github.com/RoaringBitmap/RoaringBitmap)  

- Hacker’s Delight (2nd Edition) - 2013 [link](https://en.wikipedia.org/wiki/Hacker%27s_Delight)  
Henry S. Warren, Jr.  

- VM Options Explorer
https://chriswhocodes.com  
This is just a website which points to JVM options and differences between the many JVMs.


Third party APIs
----------------
**MPJ Express**. http://mpj-express.org/.  
It is included in the project as a system dependency  

**ProGuard**. http://proguard.sourceforge.net/.  
Tool for shrink, obfuscate, and optimize code.  
With this tool I could **decrease jar file size by 20%**. **Code execution is 50% faster** on Windows using MPJe.  
I'm still playing with the program options.  
Helpful links:  
 - http://www.alexeyshmalko.com/2014/proguard-real-world-example/
 - http://proguard.sourceforge.net/manual/usage.html
 - http://proguard.sourceforge.net/manual/examples.html


Generate an Artifact
--------------------
*Note: if you don't have local Maven installation then use provided* `mvnw`.  
*Note: if you are using a JVM version 8 or smaller then you need to apply next changes in* `proguard.conf`: *uncomment* `rt.jar` *and* `jsse.jar`, *comment* `jmods`.    

Generate the jar artifact:  
```sh
mvn clean package -P java8,proguard
```
It creates a jar file with profiles **java8** and **proguard**, and copies the external dependencies under target folder.  
To disable the Proguard processing then just do not use profile Add `proguard`.  

**Profiles (use -P <name>)**
- `java8`, `java11`, `java17`: for execution with either JVM. Creates `e2solver.jar`.
- `mpje8`, `mpje11`, `mpje17`: intended for running in cluster/multi-core environment using MPJExpress api. Creates `e2solver_mpje.jar`.
- `java8native`, `java11native`, `java17native`: only intended for Graal SubstrateVM native image generation. Creates `e2solver.jar`.
- `proguard`: activates the processing of claases by Proguard to produce an optimized jar.
- `docker`, `docker-native-llvm`, `docker-native-agent`, `docker-native-llvm-build`: provide different executions on Docker. See each Docker file to know more.
- `benchmark`: generate an artifact containing JMH (Java Microbenchmarking Harness) api to benchmarking the core algorithm. Creates `e2solver_benchmark.jar`. **WIP**.


Execution
---------
First generate the artifact (previous section).  
Go under tools folder and use one of the runXXX commands.  
Eg:
```sh
  cd tools
  ./run.sh
```

The app loads by default the next properties (may vary between `threads.properties` and `mpje.properties`). 
You can pass those you want to change:
```sh
  max.cycles.print.stats=21474836470    <-- Java's Integer.MAX_VALUE * 10
  on.max.reached.save.status=true
  min.pos.save.partial=211
  exploration.limit=-1
  target.rollback.pos=-1
  ui.show=true            <-- this has no effect on native builds
  ui.per.proc=false       <-- this has no effect on native builds, and is only valid for MPJE builds
  ui.cell.size=28         <-- this has no effect on native builds
  ui.refresh.millis=100   <-- this has no effect on native builds
  num.tasks=Runtime availableProcessors()     <-- this option has no effect on MPJE builds
```
Eg:
```sh
  ./run.sh -Dmin.pos.save.partial=215 -Dnum.tasks=4
	
  ./run_mpje_multicore.sh -Dmin.pos.save.partial=215
    it uses environment variable %NUMBER_OF_PROCESSORS% or $(nproc)
```

**NOTE**: if running on a Linux terminal with no X11 server then use `-Djava.awt.headless=true`.  

Use `run.[bat|sh]` for running the `e2solver.jar` package generated with profiles *java8*, *java11*, and *java17*.  
Use `run_mpje_[multicore|cluster].[bat|sh]` for running the `e2solver_mpje.jar` package generated with profiles *mpje8*, *mpje11* and *mpje17*.  
Use `run_benchmark.[bat|sh]` for running the `e2solver_benchmark.jar` package generated with profile *benchmark*.  


Known issues
------------
*Affects some JVM 8 builds:*  
I'm having an exception when using the jpanel:  
  `java.lang.ClassCastException: sun.awt.image.BufImgSurfaceData cannot be cast to sun.java2d.xr.XRSurfaceData`  
It seems to be a known issue: https://netbeans.org/bugzilla/show_bug.cgi?id=248774  
Work around for this issue if you are using OpenJDK 8 version prior to 112: `-Dsun.java2d.xrender=false`.  
Aditionally you can use `-Djava.awt.headless=true` to ignore graphics libraries.


Using jdeps on generated jar to build custom JRE (Java 9+)
----------------------------------------------------------
Use `jdeps` to know which java modules the final application needs to run.  
Note that we are using `--multi-release=11`.  
Then you can build a *custom and smaller JRE* which will have a smaller memory footprint.  

- *NOTE*: depending on the maven profile you use to generate the artifact the name may be one of: `e2solver.jar`, `e2solver_mpje.jar`, `e2solver_benchmark.jar`. 

- Windows:
```sh
jdeps --add-modules=ALL-MODULE-PATH --multi-release=11 --print-module-deps ^
  -cp "target\libs\mpj-v0_44\lib\*" target\e2solver.jar
```
- Linux:
```sh
jdeps --add-modules=ALL-MODULE-PATH --multi-release=11 --print-module-deps \
  -cp "target/libs/mpj-v0_44/lib/*" target/e2solver.jar
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

The custom JRE is now located at `%JAVA_HOME%/customjre` folder.  
In order to use it you have to update both `JAVA_HOME`and `PATH` environment variables.


CMPH - C Minimal Perfect Hashing Library
----------------------------------------
http://cmph.sourceforge.net/  
*Used when you have large set of keys.*    
Java implementation: https://github.com/thomasmueller/minperf.  

**Test CMPH with Docker**  
```sh
docker run -it --rm alpine:latest /bin/ash
apk update
apk upgrade
apk add --no-cache cmph wget
rm -rf /tmp/*.apk /var/cache/apk/*
cmph -h
wget https://raw.githubusercontent.com/fabri1983/eternity2_solver/master/misc/super_matriz_decimal.txt -O keys_file
cmph -v -g keys_file
  it generates a file named keys_file.mph
cmph -v -m keys_file.mph keys_file
108 -> 0
119 -> 1
292 -> 2
...
777941 -> 6860
777942 -> 6861

exit
```


perfect - Minimal Perfect Hashing tool
--------------------------------------
Athor: Bob Jenkins  
https://burtleburtle.net/bob/hash/perfect.html  
A newer version is here https://github.com/driedfruit/jenkins-minimal-perfect-hash.  
It produces C files with the final hash function. **This is the solution I'm using actually**.  
This tool generates a **Minimal Perfect Hash** function for the 6862 used entries (in base 10) of the `super_matriz[24][24][24][24]`, 
which is a structure to rapidly access candidate pieces, with a total size of 331776 indexes.  
Using the **Minimal Perfect Hash function** produced by the algorithm there is a **save up to 43.3% of space** keeping 
the lookup time O(k) but still slower than a direct array access.

**Let's use MinGW in Windows to compile the project and produce the C files**
- Download MinGW from http://www.mingw.org/.
- Run setup program and when the installation manager appears select from Baisc Setup:
  - mingw-developer-toolkit-bin
  - mingw32-base-bin
  - mingw32-gcc-g++-bin
  - msys-base-bin
- Then open a cmd console:
```bat
git clone https://github.com/driedfruit/jenkins-minimal-perfect-hash perfect-jenkins
cd perfect-jenkins
edit Makefile: add CC=gcc
curl -LJ https://raw.githubusercontent.com/fabri1983/eternity2_solver/master/misc/super_matriz_decimal.txt -o keys_file
set PATH=C:\mingw\bin;C:\mingw\msys\1.0\bin;%PATH%
make
perfect -nm < samperf.txt
test -nm < samperf.txt
perfect -dpf < keys_file
  options: d = decimal keys, p = perfect hash, f = fast method
  option f produces bigger tab[] but slighly faster phash function
  option s (slow, in contrast with f) produces smaller tab[] but slower phash function 
test -dpf < keys_file
cat perf_hash.h
cat perf_hash.c
```

**Let's build a Docker image to compile the project and produce the C files**  
- Create a file named `Dockerfile` with next content:
```sh
FROM alpine:3.10.3
WORKDIR perfect
RUN apk update && apk add wget git gcc make build-base \
    && rm -rf /var/cache/apk/* \
    && git clone https://github.com/driedfruit/jenkins-minimal-perfect-hash . \
    && wget https://raw.githubusercontent.com/fabri1983/eternity2_solver/master/misc/super_matriz_decimal.txt -O keys_file
RUN make \
    && ./perfect -nm < samperf.txt \
    && ./test -nm < samperf.txt \
    && ./perfect -dpf < keys_file
    && ./test -dpf < keys_file
CMD /bin/sh
```
- Create the image:
```sh
docker image build -t perfect .
```
- Fire a container and interact with it:
```sh
docker container run --rm -it perfect
  it will prompt you a sh terminal located at /perfect folder
cat perf_hash.h
cat perf_hash.c
```
- Once you exit the console the container is removed due to `--rm` flag.


gperf - GNU perf
----------------
https://www.gnu.org/software/gperf/  
Generates a perfect hash function from a string keys set. Produces C and C++ files.  
*Used when you have small set of keys.*  
See:
- https://linux.die.net/man/1/gperf
- https://developer.ibm.com/tutorials/l-gperf/
- https://www.lrde.epita.fr/~tiger/doc/gnuprog2/Simple-Uses-of-Gperf.html

**Test gperf with Docker**
```sh
docker container run -it --rm alpine:3.10.3 /bin/ash
apk update
apk upgrade
apk add --no-cache gperf wget
rm -rf /tmp/*.apk /var/cache/apk/*
... gperf ONLY READS KEYWORDS AS STRINGS :( ...
exit
```


Build a GraalVM on Windows and run your jar
-------------------------------------------
We are going to build Graal VM for Windows platform from source: **GraalVM 21.3**.
- Download Open JDK 11: https://adoptopenjdk.net/releases.html?variant=openjdk11#x64_win.
- Or you can download Oracle JDK 11 from http://jdk.java.net/11/ (build 20 or later). This build has support for JVMCI (JVM Compiler Interface) which Graal depends on. 
- Environment variables will be set later with specific scripts.
- Install an Open JDK 1.8/11 (which already has support for JVMCI) or Windows GraalVM EE Early Adopter based on JDK 1.8/11 (with support for JVMCI):
	- https://github.com/graalvm/openjdk8-jvmci-builder/releases
	- https://github.com/graalvm/labs-openjdk-11/releases
	- https://www.oracle.com/technetwork/graalvm/downloads/index.html   <-- (choose either java8 or java11 version, both EE)
- Install Python 2.7:
	- https://www.python.org/download/releases/2.7/
	- DO NOT select Add To System Path. You will manually add it later on.
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
	- add mx binary along with Python 2.7 to PATH:
	```sh
	SET PATH=%PATH%;%cd%
	SET PATH=c:\python27;%PATH%
	mx version
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
	or if you want a specific branch:
	git clone --single-branch --branch release/graal-vm/21.3 https://github.com/oracle/graal.git .
	```
	- you will need python2.7 to be in your PATH if already didn't:
	```sh
	SET PATH=c:\python27;%PATH%
	```
	- build the Graal VM
	```sh
	SET JAVA_HOME=c:\java\openjdk-1.8.0_302-jvmci-21.3-b04
	or
	SET JAVA_HOME=c:\java\labsjdk-ce-11.0.13-7-jvmci-21.3-b04
	cd vm
	git config core.protectNTFS false
	mx --no-sources --disable-polyglot --disable-libpolyglot --skip-libraries=true --disable-installables=true ^
	   --exclude-components=gvm,gu,lg,mjdksl,nju,nic,nil,polynative,tflm ^
	   --dynamicimport /substratevm build
	cd ..\substratevm
	mx graalvm-home
	mx vm -version
	```
	Helpful options:
	```sh
	use --max-cpus 1 when you get CL.exe errors due to simultaneous access to pdb file.
	use graalvm-show and graalvm-home instead of build to see what will be built and where.
	--skip-libraries=true  <-- skips libnative-image-agent and libjvmcicompiler and maybe others
	--disable-installables=true  <-- ?? skips installables and launchers (gu, native-image, native-image-configure)?
	--exclude-components=gvm,lg,mjdksl,nju,nic,nil,polynative,tflm
	Components:
	 - Truffle ('tfl', /truffle)
	 - Component installer ('gu', /installer)
	 - SubstrateVM ('svm', /svm)
	 - Native Image licence files ('nil', /svm)
	 - Native Image ('ni', /svm)
	 - SubstrateVM LLVM ('svml', /svm)
	 - Polyglot Native API ('polynative', /polyglot)
	 - LLVM.org toolchain ('llp', /llvm)
	 - GraalVM license files ('gvm', /.)
	 - Truffle NFI ('nfi', /nfi)
	 - Truffle Macro ('tflm', /truffle)
	 - JDK11 static libraries compiled with muslc ('mjdksl', /False)
	 - LibGraal ('lg', /False)
	 - Native Image JUnit ('nju', /junit)
	 - Native Image Configure Tool ('nic', /svm)
	 - Graal SDK ('sdk', /graalvm)
	 - GraalVM compiler ('cmp', /graal)
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
    To disable use of Graal all together, use -XX:-EnableJVMCI.  
    	
    We use -XX:-TieredCompilation to disable tiered compilation to keep things simpler and to just have the one JVMCI compiler, 
    rather than using C1 and then the JVMCI compiler in tiered compilation.  

    In order to use the AOT compiled (native) GraalVM JIT compiler when loading the JVM Hotspot, use the following options:  
	-XX:+UseJVMCICompiler -XX:+UseJVMCINativeLibrary

- See also https://github.com/neomatrix369/awesome-graal/tree/master/build/x86_64/linux_macos


Build a native image using GraalVM's Native Image on Windows
------------------------------------------------------------
We are going to generate a native image to run our solver. No UI supported by the moment. **Only upto GraalVM EE 21.3 so far**.
- Install an Open JDK 1.8/11 (which already has support for JVMCI) or Windows GraalVM Early Adopter based on JDK 1.8/11 (with support for JVMCI):
	- https://github.com/graalvm/openjdk8-jvmci-builder/releases
	- https://github.com/graalvm/labs-openjdk-11/releases
- Install GraalVM EE either Java8 or java 11 version:
	- https://www.oracle.com/downloads/graalvm-downloads.html   <-- (choose either java8 or java11 versions, both EE)
	- Also download the Oracle GraalVM Enterprise Edition Native Image Early Adopter:
		- native-image-installable-svm-svmee-java8-windows-amd64-21.3.0.jar
		or
		- native-image-installable-svm-svmee-java11-windows-amd64-21.3.0.jar
- Install Python 2.7:
	- https://www.python.org/download/releases/2.7/
	- DO NOT select Add To System Path. You will manually add it later on.
- You need Windows SDK 7.1 (https://www.microsoft.com/en-us/download/details.aspx?id=8442) for building against GraalVM Java8.
This will help you to decide which iso you need to download:
	- GRMSDK_EN_DVD.iso is a version for x86 environment.
	- GRMSDKIAI_EN_DVD.iso is a version for Itanium environment.
	- GRMSDKX_EN_DVD.iso is a version for x64 environment. (This one is for an AMD64 architecture)
- Install Windows SDK 7.1 from the downloaded ISO.
	- If setup shows a warning message saying missing NET 4.x Framework tools then ignore it, and once installed install this:
		https://www.microsoft.com/en-us/download/details.aspx?id=4422
	- See this link for troubleshooting installation issues:
		https://stackoverflow.com/questions/32091593/cannot-install-windows-sdk-7-1-on-windows-10
	- You need to put files ammintrin.h, emmintrin.h, mmintrin.h, pmmintrin.h, xmmintrin.h:
		- from: C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\include
		- to: C:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\include
- You need Build Tools for Visual Studio 2017 (https://my.visualstudio.com/Downloads?q=visual%20studio%202017&wt.mc_id=o~msft~vscom~older-downloads) for building against GraalVM Java11.
- Install Build Tools for Visual Studio 2017.
- Download and setup mx tool, and add it to your PATH environment variable. See previous section *Build a GraalVM on Windows and run your jar*.
- Set Python 2.7 to path. See previous section *Build a GraalVM on Windows and run your jar*.
- Download Graal project and build the Substrate VM and build a simple Hello World example:
	```sh
	Open a console:
	  For Java8 targets
		open the Windows SDK 7.1 Command Prompt going to Start -> Programs -> Microsoft Windows SDK v7.1
		(or open a cmd console and run: call "C:\Program Files\Microsoft SDKs\Windows\v7.1\Bin\SetEnv.cmd")
	  For Java11 targets:
		open the x64 Native Tools Command Prompt for VS 2017 going to Start -> Programs -> Visual Studio 2017 -> Visual Studio Tools -> VC.
		(or open a cmd console and run: call "C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\VC\Auxiliary\Build\vcvarsall" x64)
	SET JAVA_HOME=C:\java\graalvm-ee-java8-21.3.0  or C:\java\graalvm-ee-java11-21.3.0
	cd substratevm
	git config core.protectNTFS false
	mx --disable-polyglot --disable-libpolyglot --disable-installables=true --skip-libraries=true build /substratevm
	echo public class HelloWorld { public static void main(String[] args) { System.out.println("Hello World"); } } > HelloWorld.java
	%JAVA_HOME%/bin/javac HelloWorld.java
	mx native-image --verbose HelloWorld
	HelloWorld
	```
- Building a native image for eternity 2 solver:
	- Build the project with the native profile (select the one according your installed version of GraalVM):
	```sh
	mvn clean package -P java8native,proguard
	or
	mvn clean package -P java11native,proguard
	```
	- Build the static image
	```
	cd target
	mx native-image --verbose -jar e2solver.jar
		(using --verbose we can see if it picked up our META-INF/native-image/org.fabri1983.eternity2/native-image.properties file)
		(might be usefull: mx native-image --server-shutdown)
	e2solver.exe -Dnum.tasks=8 -Dmin.pos.save.partial=211
	```
	- If not using `mx`, then you have to install the previously downloaded `native-image` tool manually:
	```sh
	SET GRAALVM_HOME=c:\java\graalvm-ee-java8-21.3.0  or  c:\java\graalvm-ee-java11-21.3.0
	%GRAALVM_HOME%\lib\installer\bin\gu -L install native-image-installable-svm-svmee-java8-windows-amd64-21.3.0.jar  or  native-image-installable-svm-svmee-java11-windows-amd64-21.3.0.jar
	```
	- There is also a possible optimization feature named Profile Guided Optimization:
	```sh
	mx native-image --pgo-instrument <same params than above>
	execute the executable for some seconds:
		e2solver.exe -Dnum.tasks=8 -Dmin.pos.save.partial=211
	mx native-image --pgo=default.iprof <same params than above>
	execute again and see if there is an improvement in execution speed
	```
- Tips:
  - Use *-H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy$BySpaceAndTime* for long lived processes.
  - For EE only you can use the low latency G1 GC: *-H:+UseLowLatencyGC*  (since v20.x)
  - Use *--report-unsupported-elements-at-runtime* to see which elements are not visible ahead of time for Graal since they are not explicitely declared in the classpath.
  - Use *-H:+ReportExceptionStackTraces* to better understand any exception during image generation.
  - See this article's sections *Incomplete classpath* and *Delayed class initialization*: https://medium.com/graalvm/instant-netty-startup-using-graalvm-native-image-generation-ed6f14ff7692. Option is *--allow-incomplete-classpath*.
  - See this article which solves lot of common problems: https://royvanrijn.com/blog/2018/09/part-2-native-microservice-in-graalvm/
  - To avoid the error *Class XXX cannot be instantiated reflectively . It does not have a nullary constructor* you can disable the ServiceLoaderFeature with *-H:-UseServiceLoaderFeature*. That's where this is triggered from. You can also use *-H:+TraceServiceLoaderFeature* to see all the classes processed by this feature.
  - To avoid resources registration been automatically added by ServiceLoaderFeature use *-H:-UseServiceLoaderFeature*
  - Reference manual: https://docs.oracle.com/en/graalvm/enterprise/20/guide/toc.htm
  - *-H:+PrintAnalysisCallTree* or *-H:+PrintImageObjectTree* options are meant to help answer questions about why a certain method or object are getting into an image.
  - If facing `Caused by: java.nio.charset.UnsupportedCharsetException: <charset-name-here>` then use *-H:+AddAllCharsets*.
  - If using *-Dio.netty.noUnsafe=true* but still getting: `DEBUG io.grpc.netty.shaded.io.netty.util.internal.PlatformDependent0 - -Dio.netty.noUnsafe: false` then use *-Dio.grpc.netty.shaded.io.netty.noUnsafe=true*
  - Use *-H:+RemoveSaturatedTypeFlows* to reduce the analysis phase execution time and memory usage. However the number of methods/types included in the image can increase.
  - Use *--install-exit-handlers* to improve user experience when running a native image in a Docker container as the init process.
  - Use *-H:Log=registerResource:verbose* to show which resource is effectively used in the binary generation. Leading "/" isn't allowed in native builds.
  - Print all flags: *-XX:+JVMCIPrintProperties*.
  - How to work with substitutions in GraalVM: https://blog.frankel.ch/solving-substitution-graalvm-issue/.


Using GraalVM's Agent Lib to get native image resources and configurations
--------------------------------------------------------------------------
**This process produces configurations already set in native-image.properties**
```sh
SET GRAALVM_HOME=c:\java\graalvm-ee-java8-21.3.0 or  c:\java\graalvm-ee-java11-21.3.0
%GRAALVM_HOME%\lib\installer\bin\gu -L install native-image-installable-svm-svmee-java8-windows-amd64-21.3.0.jar  or  native-image-installable-svm-svmee-java11-windows-amd64-21.3.0.jar
set JAVA_HOME=<any JDK 8 or 11 with JVMCI support, except the GraalVM one>
	Eg: set JAVA_HOME=c:\java\openjdk-1.8.0_302-jvmci-21.3-b04
	Eg: set JAVA_HOME=c:\java\labsjdk-ce-11.0.13-7-jvmci-21.3-b04
Update PATH env variable with %JAVA_HOME%\bin
```
**WIP**: need to setup a flag to finish the program after few seconds, so the agent writes down the output files. Otherwise I'm adding `System.exit(0)` when max cycles is reached. 
Generate jar artifact with -Pjava8native or -Pjava11native
```sh
mvn clean package -P java8native,proguard
```
Then add at the beginning of your java command and run your program for few seconds:
```sh
-agentpath:%GRAALVM_HOME%\jre\bin\native-image-agent.dll=config-output-dir=native-config
```
Run the solver for few seconds
```sh
cd tools
run.bat -Dui.show=false -Dnum.tasks=1
```
Then move produced files from `target\native-config` to `src\main\resources\META-INF\native-image\org.fabri1983.eternity2`.
That way when jar is generated and then processed by `native-image` tool it will grab those configurations.


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

