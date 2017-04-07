#!/bin/bash
set -x
export PATH=$PATH:/usr/local/go/bin/
export GOPATH=`pwd`
export ANDROID_HOME=$HOME'/Android/Sdk'
/usr/local/go/bin/go get golang.org/x/mobile/cmd/gomobile
./bin/gomobile bind -v dwl
#./bin/gomobile bind -target=android/arm -v -o dwl_arm.aar dwl &&
#./bin/gomobile bind -target=android/arm64 -v -o dwl_arm64.aar dwl &&
#./bin/gomobile bind -target=android/386 -v -o dwl_386.aar dwl &&
#./bin/gomobile bind -target=android/amd64 -v -o dwl_amd64.aar dwl
