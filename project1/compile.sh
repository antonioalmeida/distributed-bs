#!/usr/bin/env bash
rm -rf bin
mkdir -p bin
javac -d bin -sourcepath . test/TestApp.java server/Peer.java -Xlint:unchecked

