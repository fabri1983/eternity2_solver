eternity2_solver
================
| Linux | Windows |
| ----- | ------- |
| ![Travis](https://travis-ci.org/fabri1983/eternity2_solver.svg?branch=master) | [![Appveyor](https://ci.appveyor.com/api/projects/status/38ua6hnrh6xtyi8j/branch/master?svg=true)](https://ci.appveyor.com/project/fabri1983/eternity2-solver/branch/master) |

![eternity solver mpje 8 threads image](/misc/eternity_solver_mpje_x8.jpg?raw=true "eternity solver mpje 8 threads")

Java implementation of a backtracker solver for the Eternity II board game released in August 2007.
Game finished in 2010 without anyone claiming the solution. Prize for any valid solution was 2 million usd.

This project is managed with Maven 3.x.

The backtracker uses smart prunes, data structures for quickly accessing information, and micro optimizations.
There are two versions of the same solver: one using fork-join and other using MPI (for distributed execution).

The project is under continuous development, mostly on spare time. Every time I come up with an idea, improvement, or code re-factor is for performance purpose.

Some stats:

- Environment Windows 7 Intel Core i7-2630QM 2.6GHz DDR3 Dual Channel. Results:
Currently placing approx **54 million pieces per second** in a fork-join pool with 8 threads. 
And placing approx **80 million pieces per second** using MPJ Express framework as multi-core execution with 8 solver instances. 

- Environment Ubuntu 14.04 Intel core i5 DDR3 Dual Channel OpenJDK 1.7. Results:
Currently placing **38 million pieces per second** in a fork-join pool with 4 threads. 
And placing around **90 million pieces per second** using MPJ Express framework with 4 instances of the solver. 

In the past, experiments showed that execution was faster using the JRockit JVM from Oracle. I saw a 25% of speed up. 
However new JVMs since 1.7 brought a gain in performance which made me leave the JRockit execution as historical and no more JVM parameters tuning.


Papers where I took some ideas from
-----------------------------------

- How many edges can be shared by N square tiles on a board?
http://tbenoist.pagesperso-orange.fr/papers/HowManyEdges.pdf
Thierry Benoist
e-lab Research Report - April 2008

- Fast Global Filtering for Eternity II
http://cs.brown.edu/people/pvh/CPL/Papers/v3/eternity.pdf
Thierry Benoist, e-lab - Bouygues SA, Paris
Eric Bourreau, LIRMM, Montpellier<br/>

- Jigsaw Puzzles, Edge Matching, and Polyomino Packing: Connections and Complexity
http://erikdemaine.org/papers/Jigsaw_GC/paper.pdf
Erik D. Demaine, Martin L. Demaine
MIT Computer Science and Artificial Intelligence Laboratory


Third party APIs
----------------
**MPJ Express**. http://mpj-express.org/.
It is included in the project as a system dependency

**jsr166**. https://www.jcp.org/en/jsr/detail?id=166
Is the java concurrent api for Java 1.6 target builds.
I use this to run the program on the Oracle JRockit VM.

**ProGuard**. http://proguard.sourceforge.net/
Tool for shrink, obfuscate, and optimize code.
With this tool I could **decrease jar file size by 20%**.
**Code execution is 50% faster** on Windows box using MPJe. Although, on Linux box with an OpenJDK it seems to be slower.
I'm still playing with the program parameters.
Helpful links:
	http://www.alexeyshmalko.com/2014/proguard-real-world-example/
	http://proguard.sourceforge.net/manual/usage.html
	http://proguard.sourceforge.net/manual/examples.html



Packaging
---------
```sh
mvn clean package
```
It generates the jar file with default profile and copy the external dependencies under target folder.
Also by default it uses ProGuard code processing. Add -Dskip.proguard=true to generate simple java jar.

**Profiles (use -P)**
- java7, java8: for execution with either JVM.
- jrockit: intended for running on Oracle's JRockit JVM (the one that is java 1.6 version).
- mpje: intended for running in cluster/multi-core environment using MPJExpress api.
- java8native: only intended for Graal SubstrateVM native image generation.


Execution
---------
First create the package (previous section).
Go under tools folder and use one of the runXXX commands. 
E.g.:
	./run.sh

The app loads by default the next properties (may change between forkjoin and mpje). You can pass only those you want to change:

	max.ciclos.save_status=2147483647
	min.pos.save.partial=211
	exploration.limit=-1
	max.partial.files=2
	target.rollback.pos=-1
	ui.show=true
	ui.per.proc=false
	ui.cell.size=28
	ui.refresh.millis=100
	experimental.gif.fair=false
	experimental.borde.left.explorado=false
	task.distribution.pos=99

E.g.:
	./run.sh -Dmin.pos.save.partial=215 -Dui.show=false
	./run_mpje_multicore.sh -Dmin.pos.save.partial=215 -Dui.show=true
	 
Use run.bat or run.sh for running the e2solver.jar package generated with profiles java7 (default) or java8.
Use run_jrockit.bat or run_jrockit.sh for running the e2solver_jrockit.jar package generated with profile jrockit.
Use run_mpje_xxx.bat or run_mpje_xxx.sh for running the e2solver_mpje.jar package generated with profile mpje.


Known issues
------------
*Note for JRE 8:*
I'm having an exception when using the jpanel:
java.lang.ClassCastException: sun.awt.image.BufImgSurfaceData cannot be cast to sun.java2d.xr.XRSurfaceData
It seems to be a known issue: https://netbeans.org/bugzilla/show_bug.cgi?id=248774


Usage of Graal Compiler on Windows
----------------------------------
We are going to build a graal compiler for Windows platform.
- Download Oracle JDK 11 from http://jdk.java.net/11/ (build 20 or later). This build has support for JVMCI (JVM Compiler Interface) which Graal depends on. 
Environment variables will be set later with specific scripts.
- Install a Labs JDK 1.8 (currently jvmci-0.54) with support for JVMCI: https://www.oracle.com/technetwork/oracle-labs/program-languages/downloads/index.html
Environment variables will be set later with specific scripts.
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
- Building Graal:
	- create a graal directory (outside the mx directory previously created) and locate into it:
	```sh
	mkdir graal
	cd graal
	```
	- clone graal project:
	```sh
	git clone https://github.com/oracle/graal.git .
	```
	- you will need python2.7 to be in your PATH:
	```sh
	SET JAVA_HOME=c:\java\jdk-11.0.1
	echo %JAVA_HOME%
	SET EXTRA_JAVA_HOMES=c:\java\labsjdk1.8.0_192-jvmci-0.54
	echo %EXTRA_JAVA_HOMES%
	cd compiler
	mx build
	mx vm -version
	```
- Using the Graal compiler with your JVMCI enabled JVM:
Now we’re going to use the Graal that we just built as our JIT-compiler in our Java 11 JVM. We need to add some more complicated flags here.
    
    --module-path=... and --upgrade-module-path=... add Graal to the module path. 
    Remember that the module path is new in Java 9 as part of the Jigsaw module system, and you can think of it as being like the classpath for our purposes here.
    
    We need -XX:+UnlockExperimentalVMOptions because JVMCI (the interface that Graal uses) is just experimental at this stage.
    
    We then use -XX:+EnableJVMCI to say that we want to use JVMCI, and -XX:+UseJVMCICompiler to say that we actually want to use it and to install a new JIT compiler.
    By default, Graal is only used for hosted compilation (i.e., the VM still uses C2 for compilation). 
    To make the VM use Graal as the top tier JIT compiler, add the -XX:+UseJVMCICompiler option to the command line. To disable use of Graal altogether, use -XX:-EnableJVMCI.
    	
    We use -XX:-TieredCompilation to disable tiered compilation to keep things simpler and to just have the one JVMCI compiler, rather than using C1 and then the JVMCI compiler in tiered compilation.


Build a native image using Graal's SubstrateVM on Windows
---------------------------------------------------------
- See https://github.com/oracle/graal/issues/946#issuecomment-459330069
- Install a Labs JDK 1.8 (currently jvmci-0.54) with support for JVMCI: https://www.oracle.com/technetwork/oracle-labs/program-languages/downloads/index.html
- You will need Python 2.7 (https://www.python.org/downloads/release/python-2715/) and Windows SDK for Windows 7 (https://www.microsoft.com/en-us/download/details.aspx?id=8442).
This will help you to decide which iso you need to download:
	- GRMSDK_EN_DVD.iso is a version for x86 environment.
	- GRMSDKIAI_EN_DVD.iso is a version for Itanium environment.
	- GRMSDKX_EN_DVD.iso is a version for x64 environment. (This one is for an AMD64 architecture)
- Install Windows SDK for Windows 7 from the download ISO.
See this link for troubleshooting installation issues: https://stackoverflow.com/questions/32091593/cannot-install-windows-sdk-7-1-on-windows-10
- Download and setup mx tool, and add it to your PATH environment variable. Also you can create MX_HOME env variable and add append it to PATH. See previous section *Usage of Graal Compiler on Windows*.
- Download Graal project and build the Substrate VM and build a simple Hello World example:
	```sh
	Open a console:
		open the Windows SDK 7.1 Command Prompt going to Start -> Programs -> Microsoft Windows SDK v7.1
		or
		open a cmd console and run "C:\Program Files\Microsoft SDKs\Windows\v7.1\Bin\SetEnv.cmd"
	SET JAVA_HOME=c:\java\labsjdk1.8.0_192-jvmci-0.54
	cd substratevm
	mx build
	echo public class HelloWorld { public static void main(String[] args) { System.out.println("Hello World"); } } > HelloWorld.java
	%JAVA_HOME%/bin/javac HelloWorld.java
	mx native-image --verbose HelloWorld
	HelloWorld
	```
- Troubleshooting: 
	- ninja: build stopped: subcommand failed.
		```sh
		fatal error C1041: cannot open program database 'C:\java\graal\substratevm\mxbuild\windows-amd64\src\com.oracle.svm.native.libchelper\vc140.pdb'; if multiple CL.EXE write to the same .PDB file, please use /FS
		```
		This error happens because the C compiler CC.exe seems to be executed as release-multithreaded fashion and then when accessing that pdb file a restriction is thrown as error.
		Solution:
			- run the *mx build* command again.
	- In case you need to rebuild the substratevm:
		```sh
		mx clean
		manually delete folders *mxbuild* and *svmbuild*
		mx build
		mx native-image --help
		```
	- If you receive error message *Error: Could not find or load main class com.oracle.svm.hosted.NativeImageGeneratorRunner* then you need to:
		- Run again your native-image command with --verbose option
		- Copy the execution command and replace *:* separator by *;*
		- Optional: add option for temp directory: -H:TempDirectory=C:\java\graal\substratevm\temp
		- Getting this similar command:
			C:\java\labsjdk1.8.0_192-jvmci-0.54\bin\java.exe -Xbootclasspath/a:C:\java\graal\substratevm\svmbuild\native-image-root\lib\boot\graal-sdk.jar -cp C:\java\graal\substratevm\svmbuild\native-image-root\lib\svm\builder\objectfile.jar;C:\java\graal\substratevm\svmbuild\native-image-root\lib\svm\builder\pointsto.jar;C:\java\graal\substratevm\svmbuild\native-image-root\lib\svm\builder\svm.jar;C:\java\graal\substratevm\svmbuild\native-image-root\lib\jvmci\graal.jar -server -d64 -noverify -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:-UseJVMCICompiler -XX:-UseJVMCIClassLoader -Dgraal.EagerSnippets=true -Xss10m -Xms1g -Xmx13658770632 -Duser.country=US -Duser.language=en -Dgraalvm.version=1.0.0-rc10-SNAPSHOT -Dorg.graalvm.version=1.0.0-rc10-SNAPSHOT -Dcom.oracle.graalvm.isaot=true -Djvmci.class.path.append=C:\java\graal\substratevm\svmbuild\native-image-root\lib\jvmci\graal.jar com.oracle.svm.hosted.NativeImageGeneratorRunner -imagecp C:\java\graal\substratevm\svmbuild\native-image-root\lib\boot\graal-sdk.jar;C:\java\graal\substratevm\svmbuild\native-image-root\lib\svm\builder\objectfile.jar;C:\java\graal\substratevm\svmbuild\native-image-root\lib\svm\builder\pointsto.jar;C:\java\graal\substratevm\svmbuild\native-image-root\lib\svm\builder\svm.jar;C:\java\graal\substratevm\svmbuild\native-image-root\lib\jvmci\graal.jar;C:\java\graal\substratevm\svmbuild\native-image-root\lib\svm\library-support.jar;C:\java\graal\substratevm -H:Path=C:\java\graal\substratevm -H:CLibraryPath=C:\java\graal\substratevm\svmbuild\native-image-root\lib\svm\clibraries\windows-amd64 -H:Class=HelloWorld -H:Name=helloworld
- Building a native image for eternity 2 solver:
	complete this. 
	Use *--report-unsupported-elements-at-runtime* to see which elements are not visible ahead of time for Graal since they are not explicitely declared in the classpath.
	Is good for prototyping because it allows you to build native executables without worrying about many issues at first. But we discourage using it in production.
	See this article's sections *Incomplete classpath* and *Delayed class initialization*: https://medium.com/graalvm/instant-netty-startup-using-graalvm-native-image-generation-ed6f14ff7692.
	```sh
	mx native-image --static --report-unsupported-elements-at-runtime -H:IncludeResources="(.*/application.properties$)|(.*/e2pieces.txt$)" --delay-class-initialization-to-runtime=SolverFaster,Contorno,Pieza,FilaPiezas,MapaKeys -J-Xms900m -J-Xmx900m -jar e2solver.jar
	```


Running with Avian JVM
----------------------
I'm trying to improve the performance of code execution using a free JVM implementation.
Currently I'm taking a look to Avian JVM, under a Windows environment.

Visit page http://oss.readytalk.com/avian/ to know what Avian is all about.

- Install cygwin following the steps mentioned in https://github.com/ReadyTalk/avian/ (README.md file).
	- you will need to add some packages that aren't set as default (the instructions are there)
	- also need to add curl
	- also need ncurses (for clear command, or use ctrl+l)
- Set JAVA_HOME environment variable in your .bashrc file
	- export JAVA_HOME=/cygdrive/c/java/jdk1.7
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
