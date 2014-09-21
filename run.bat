@echo off
:: El entero mas grande de 32 bits es 2147483648
:: A efectos de java el maximo int de 32 bits posible a usar es 2147483647
:: para 2 colums tengo count maximo 29 con 34926 filas calculadas
:: para 3 colums tengo count maximo 91 con 1556132 filas calculadas
:: para 4 colums tengo count maximo 297 con 68981072 filas calculadas
:: para 5 colums tengo count maximo ¿? con 3042060164 filas calculadas

:: paramaters:
:: maxCiclos limiteParcialMax minLimiteExploracion maxParciales destinoARetroceder InterfaceGrafica
:: CellPixelesLado CanvasRefreshMillis PodaFairExperiment PodaColorBordeLeftExplorado PosicionInicioMultiThreading

set java_hotspot=java -XX:+AggressiveOpts
set java_rockit=C:/Java/jrockit-jdk1.6.0_45-R28.2.7-4.1.0/bin/java -Xbootclasspath/p:./extern_libs/jsr166.jar
:: NOTE: it seems to be that using server has a good impact in performance. However with MPJe it doesn't
%java_rockit% -server -Xms1024m -Xmx1024m -jar E2Faster.jar 2147483647 211 -1 2 -1 true 28 100 false false 99

pause