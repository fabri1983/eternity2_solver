#!/bin/sh
# If you need to change permissions for execution then do: sudo chmod 775 docker-build.sh

if [[ $# -ne 3 ]] ; then
  echo "No arguments supplied. You need to specify artifact final name (without extension), extension (war or jar), and tag name"
  exit 1
fi

echo --------------------
echo Copying tools folder
echo --------------------
cp -f -r tools target/

echo -----------------------------------
echo Building Docker image for $1.$2
echo -----------------------------------

tagName=fabri1983dockerid/$1:$3

# create Docker image
docker image build \
	--build-arg E2_JAR=$1.$2 \
	-f target/Dockerfile -t $tagName ./target

if [[ $? -eq 0 ]] ; then
	echo ----------------------------------------------------------
	echo Finished! Docker Image tagged: $tagName
	echo ----------------------------------------------------------
	exit 0
else
	echo -----------------------------
	echo There was a problem!
	echo -----------------------------
	exit 1
fi
