#!/bin/sh
echo Compilando...

# lib para envio de emails. Java ya tiene una. Usar esa.
# -cp ./extern_libs/jaf-1.1/activation.jar

export MPJ_HOME="./extern_libs/mpj-v0_44"

javac -O -g:none -cp ./extern_libs/javamail-1.4.5/mail.jar:$MPJ_HOME/lib/mpj.jar -sourcepath ./eternity_faster_pkg_mpj eternity_faster_pkg/*.java eternity_faster_pkg_mpj/*.java eternity_faster_pkg_arrays/*.java eternity_ui/*.java

jar cvfe e2solver_mpje.jar eternity_faster_pkg_mpj.MainFasterMPJE eternity_faster_pkg/*.class eternity_faster_pkg_mpj/*.class eternity_faster_pkg_arrays/*.class eternity_ui/*.class imgs/*.png
