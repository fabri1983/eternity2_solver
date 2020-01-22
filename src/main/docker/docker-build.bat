@ECHO OFF

IF "%1"=="" (
  GOTO NO_ARGS
)
IF "%2"=="" (
  GOTO NO_ARGS
)
IF "%3"=="" (
  GOTO NO_ARGS
)
IF NOT "%4"=="" (
  GOTO WRONG_ARGS
)

GOTO ACTION

:NO_ARGS
ECHO No arguments supplied. You need to specify artifact final name (without extension), extension (war or jar), and tag name
GOTO FAILED

:WRONG_ARGS
ECHO Wrong number of arguments. You need to specify artifact final name (without extension), extension (war or jar), and tag name
GOTO FAILED

:ACTION

ECHO --------------------
ECHO Copying tools folder
ECHO --------------------
XCOPY /Q /Y /I tools target\tools

ECHO -------------------------------------
ECHO Building Docker image for %1.%2
ECHO -------------------------------------

SET tagName=fabri1983dockerid/%1:%3

:: create Docker image
docker image build ^
  --build-arg E2_JAR=%1.%2 ^
  -f target/Dockerfile -t %tagName% ./target

if %ERRORLEVEL% == 0 (
	ECHO ----------------------------------------------------------
	ECHO Finished! Docker Image tagged: %tagName%
	ECHO ----------------------------------------------------------
	GOTO SUCCESS
) ELSE (
	ECHO -----------------------------
	ECHO There was a problem!
	ECHO -----------------------------
	GOTO FAILED
)

:FAILED
EXIT /b 1

:SUCCESS
EXIT /b 0