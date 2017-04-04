#!/bin/bash
set -x
export PATH=$PATH:/usr/local/go/bin/
export GOPATH=`pwd`
export ANDROID_HOME=$HOME'/Android/Sdk'
/usr/local/go/bin/go get golang.org/x/mobile/cmd/gomobile
./bin/gomobile bind -v dwl
