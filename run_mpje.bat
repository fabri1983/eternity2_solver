@echo off
rem El entero mas grande de 32 bits es 2147483648
rem A efectos de java el maximo int de 32 bits posible a usar es 2147483647
rem para 2 colums tengo count maximo 29 con 34926 filas calculadas
rem para 3 colums tengo count maximo 91 con 1556132 filas calculadas
rem para 4 colums tengo count maximo 297 con 68981072 filas calculadas
rem para 5 colums tengo count maximo ¿? con 3042060164 filas calculadas

rem use %NUMBER_OF_PROCESSORS% instead
rem edit mpjrun.bat to select the desired JVM

mpjrun.bat -np 4 -Xms200m -Xmx200m E2MPJE.jar 2147483647 211 -1 2 -1 false 28 100 false false 99

pause