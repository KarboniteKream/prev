#!/bin/bash

REGISTERS=${3:-8}

javac -cp src src/compiler/Main.java && java -cp src compiler.Main --phase=$1 --dump=$1 --registers=$REGISTERS tests/$2.prev

if [[ $1 == "build" ]]; then
	cat tests/$2.mms
else
	cat tests/$2.log
fi
