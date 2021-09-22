@ECHO OFF
ECHO.
ECHO ####################################
ECHO Cleaning all projects with mx clean
ECHO ####################################
ECHO.

SET currentDir=%CD%

start /B /wait cmd.exe /K "cd /D %currentDir%\compiler\    && ECHO Cleaning compiler && mx clean && exit"
start /B /wait cmd.exe /K "cd /D %currentDir%\regex\       && ECHO Cleaning regex && mx clean && exit"
start /B /wait cmd.exe /K "cd /D %currentDir%\sdk\         && ECHO Cleaning sdk && mx clean && exit"
start /B /wait cmd.exe /K "cd /D %currentDir%\substratevm\ && ECHO Cleaning substratevm && mx clean && exit"
start /B /wait cmd.exe /K "cd /D %currentDir%\tools\       && ECHO Cleaning tools && mx clean && exit"
start /B /wait cmd.exe /K "cd /D %currentDir%\truffle\     && ECHO Cleaning truffle && mx clean && exit"
start /B /wait cmd.exe /K "cd /D %currentDir%\vm\          && ECHO Cleaning vm && mx clean && exit"
start /B /wait cmd.exe /K "cd /D %currentDir%\wasm\        && ECHO Cleaning wasm && mx clean && exit"
:: This one fails so I put it at the end
start /B /wait cmd.exe /K "cd /D %currentDir%\sulong\      && ECHO Cleaning sulong && mx clean && exit"

ECHO.
ECHO Finished!
ECHO.