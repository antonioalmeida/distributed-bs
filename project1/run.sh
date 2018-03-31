#!/usr/bin/env bash
java -classpath bin -Djava.net.preferIPv4Stack=true -Djava.rgit server.Peer "$1" "$2" "$3" 232.1.1.0 4465 232.1.1.1 4466 232.1.1.2 4467 &