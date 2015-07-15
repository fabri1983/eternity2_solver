@echo off
echo Compilando...

rem lib para envio de emails. Java 1.6 ya tiene una. Usar esa.
rem -cp ./extern_libs/jaf-1.1/activation.jar

javac -source 1.8 -target 1.8 -O -g:none -cp ./extern_libs/javamail-1.4.5/mail.jar -sourcepath ./eternity_faster_pkg eternity_faster_pkg/*.java eternity_faster_pkg_arrays/*.java graphic/EternityCanvas.java graphic/EternityCellRenderer.java graphic/EternityII.java graphic/EternityModel.java graphic/EternityTable.java graphic/ViewEternity.java

jar cvfe E2Faster.jar eternity_faster_pkg.MainFaster eternity_faster_pkg/*.class eternity_faster_pkg_arrays/*.class graphic/*.class graphic/image/*.png

pause