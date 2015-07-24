eternity2_solver
================

Java implementation of a backtracker solver for the Eternity II board game released in August 2007.
Game finished in 2010 without anyone claiming the solution. Prize for any valid solution was 2 million usd.

This project is managed with Maven 3.x.

The backtracker uses smart prunes, data structures for quickly accessing information, and micro optimizations.

Environment: Windows 7 Intel core i7 2.6GHz DDR3 Dual Channel. Results:
Currently placing around 54 million pieces per second in a fork/join pool with 8 threads. 
And placing around 80 million pieces per second using MPJ Express framework with 8 instances of the solver. 

Environment: Ubuntu 14.04 Intel core i5 DDR3 Dual Channel OpenJDK 1.7. Results:
Currently placing 38 million pieces per second in a fork/join pool with 4 threads. 
And placing around 90 million pieces per second using MPJ Express framework with 4 instances of the solver. 

The project is under continuous development, mostly on spare time. Every time I come up with an idea, improvement, 
or code re-factor is for performance purpose.
In the past experiments said that execution is faster using the JRockit JVM from Oracle. I saw a 25% of speed up. 
However new JVMs since 1.7 brought a gain in performance.


Papers where I took some ideas
------------------------------

- How many edges can be shared by N square tiles on a board? 
Thierry Benoist
e-lab Research Report - April 2008

- Fast Global Filtering for Eternity II
Thierry Benoist, e-lab - Bouygues SA, Paris
Eric Bourreau, LIRMM, Montpellier

- Jigsaw Puzzles, Edge Matching, and Polyomino Packing: Connections and Complexity
Erik D. Demaine, Martin L. Demaine
MIT Computer Science and Artificial Intelligence Laboratory


Third party APIs
----------------
MPJ Express. http://mpj-express.org/.
It is included in the project as a system dependency

jsr166. https://www.jcp.org/en/jsr/detail?id=166
Is the java concurrent api for Java 1.6 target builds.
I use this to run the program on the Oracle JRockit VM.

ProGuard. http://proguard.sourceforge.net/
Tool for shrinking, obfuscating, and optimizing code.
With this tool I could decrease jar file size by 20%.
Also code execution is barely faster, although I'm still playing with the options.
Helpful links:
	http://www.alexeyshmalko.com/2014/proguard-real-world-example/
	http://proguard.sourceforge.net/manual/usage.html
	http://proguard.sourceforge.net/manual/examples.html


Packaging
---------
mvn clean package
It generates the jar file with default profile and copy the external dependencies under target folder.
Also by default it uses ProGuard code processing. Add -Dskip.proguard=true to generate simple java jar.
Profiles (use -Pname):
	java7, java8: for executing with either JVM.
	jrockit: intended for running on Oracle's JRockit JVM.
	mpje: intended for running in cluster/multicore environment using MPJExpress.


Execution
---------
Go under tools folder and use one of the runXXX commands.
Use run.bat or run.sh for running the e2solver.jar package generated with profiles java7 (default) or java8.
Use run_jrockit.bat or run_jrockit.sh for running the e2solver_jrockit.jar package generated with profile jrockit.
Use run_mpje_xxx.bat or run_mpje_xxx.sh for running the e2solver_mpje.jar package generated with profile mpje.


Running with Avian jvm
----------------------
I'm trying to improve the execution of code using another free JVM implementations.
Currently I'm taking a look to Avian JVM, under a Windows environment.

Visit page http://oss.readytalk.com/avian/ to know what is Avian.

- Install cygwin following the steps mentioned in https://github.com/ReadyTalk/avian/ (README.md file).
	- you will need to add some packages that aren't set as default (the instructions are there)
	- also need to add curl
	- also need ncurses (for clear command, or use ctrl+l)
- Set JAVA_HOME environment variable in your .bashrc file
	- export JAVA_HOME=/cygdrive/c/java/jdk1.7
- Once cygwin is installed you need to clone avian, win32, and win64 repos.
	- open cygwin terminal
	- create a folder avian_jvm whenever you want (eg: in /home/Admi/avian_jvm)
	- open the cygwin terminal and locate into created dir
	- git clone git://github.com/ReadyTalk/avian
	- git clone git://github.com/ReadyTalk/win64
	- git clone git://github.com/ReadyTalk/win32
- Locate in avian dir and build it using make command
- Run hello world test program (which is a already compiled class file):
	- build/windows-x86_64/avian -cp build/windows-x86_64/test Hello
- You can try a swt example:
	- downloaded the exe from http://oss.readytalk.com/avian/examples.html
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
		- we need to copy lzma-920/C folder to avian_jvm/avian/src/ folder because it won't compile without that folder
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
