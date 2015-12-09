#!/bin/bash

javac -cp src src/compiler/Main.java && java -cp src compiler.Main --phase=$1 --dump=$1 tests/$2.c

if [[ $1 == "build" ]]; then
	cat tests/$2.mms
else
	cat tests/$2.log
fi
