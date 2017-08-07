#!/usr/bin/env sh
./gradlew clean
./gradlew shadowJar
echo Copying our shadow JAR file
cp build/libs/ICFP2017-1.0-SNAPSHOT-all.jar submission/*
echo Pulling sources from github
curl -L https://github.com/kercheval/ICFP2017/tarball/master > submission/src/ICFP2017-src.tar.gz
echo Creating tarball
tar -czvf icfp-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.tar.gz submission
echo COMPLETE
echo .
echo .
echo Remember to rename the tarball using our team ID before submission
echo .
echo MD5 Checksum
md5 icfp-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.tar.gz
