#!/bin/sh
echo Compilando...

# lib para envio de emails. Java ya tiene una. Usar esa.
# -cp ./extern_libs/jaf-1.1/activation.jar

javac -source 1.8 -target 1.8 -O -g:none -cp ./extern_libs/javamail-1.4.5/mail.jar -sourcepath ./eternity_faster_pkg eternity_faster_pkg/*.java eternity_faster_pkg_arrays/*.java eternity_ui/EternityCanvas.java eternity_ui/EternityCellRenderer.java eternity_ui/EternityII.java eternity_ui/EternityModel.java eternity_ui/EternityTable.java eternity_ui/ViewEternity.java

jar cvfe e2solver.jar eternity_faster_pkg.MainFaster eternity_faster_pkg/*.class eternity_faster_pkg_arrays/*.class eternity_ui/*.class imgs/*.png
