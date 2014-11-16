eternity2_solver
================

Java implementation of a backtracker for the Eternity II board game released in August 2007.
Game finished in 2010 without no one claiming the solution. Prize for any valid solution was 2 million usd.

This backtracker uses smart prunes, data structures for quickly accessing information, and micro optimizations.

Currently placing 70 million pieces per second in a 8 thread execution instances using a fork/join pool. And placing 80 million pieces per second using MPJ Express framework with 4 instances of the solver.

The project is under continuous development on spare time. Every time I come back and apply some change or code re-factor is for performance purpose.
Experiments say that execution is faster using the JRockit JVM from Oracle. I see a 25% of speed up.


Papers where I took some ideas from
-----------------------------------

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
MPJ Express. http://mpj-express.org/
Once downloaded and uncompressed, you need to edit the bat files and the Eclipse project for correctly classpath.


Running with Avian jvm
----------------------
I'm trying to improve the execution of code using another free JVM implementations.
Currently I'm taking a look to Avian JVM.

Visit page http://oss.readytalk.com/avian/ to know what is Avian.

Install cygwin following the steps mentioned in https://github.com/ReadyTalk/avian/ (README.md file).
	you will need to add some packages that aren't set as default (the instructions are there)
	also need to add curl
	also need ncurses (for clear command, or use ctrl+l)
Set JAVA_HOME environment variable in your .bashrc file
	export JAVA_HOME=/cygdrive/c/java/jdk1.7
Once cygwin is installed you need to clone avian, win32, and win64 repos.
	- open cygwin terminal
	- create a folder avian_jvm whenever you want (eg: in /home/Admi/avian_jvm)
	- open the cygwin terminal and locate into created dir
	- git clone git://github.com/ReadyTalk/avian
	- git clone git://github.com/ReadyTalk/win64
	- git clone git://github.com/ReadyTalk/win32
Locate in avian dir and build it:
	make
Run hello world test program (which is a already compiled class file):
	build/windows-x86_64/avian -cp build/windows-x86_64/test Hello
You can try a swt example:
	downloaded the exe from http://oss.readytalk.com/avian/examples.html
	or build it and execute it locally. Example for win64:
		export platform=windows-x86_64
		export swt_zip=swt-4.3-win32-win32-x86_64.zip
		mkdir swt-example
		cd swt-example
		curl -Of http://oss.readytalk.com/avian-web/proguard4.11.tar.gz
		tar xzf proguard4.11.tar.gz
		curl -Of http://oss.readytalk.com/avian-web/lzma920.tar.bz2
		mkdir -p lzma-920
		cd lzma-920
		tar xjf ../lzma920.tar.bz2
		cd ..
		curl -Of http://oss.readytalk.com/avian-web/${swt_zip}
		mkdir -p swt/${platform}
		unzip -d swt/${platform} ${swt_zip}
		curl -Of http://oss.readytalk.com/avian-web/avian-swt-examples-1.0.tar.bz2
		tar xjf avian-swt-examples-1.0.tar.bz2
		we need to copy lzma-920/C folder to avian_jvm/avian/src/ folder because it won't compile without that folder
			cp -ar lzma-920/C ../avian/src/ 
		cd avian-swt-examples
		once in avian-swt-examples folder edit files:
			makefile: at line 53 add /.. to current cd .. command since avian folder is one more level up.
			app.mk: locate root variable and change other variables that use root just adding ../ because avian, win32, and win64 folders are 1 additional level up.
		build example exe file with lzma (compressed exe) or without it
			make lzma=$(pwd)/../lzma-920 full-platform=${platform} example
			or
			make full-platform=${platform} example
		exe file are created at avian-swt-examples/build/windows-x86_64-lzma/ and in avian-swt-examples/build/windows-x86_64/ respectively.
		You can omit example target to let other targets be built: example, graphics, and paint
