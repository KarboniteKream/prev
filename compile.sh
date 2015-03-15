#!/bin/bash
javac -cp src src/compiler/Main.java && java -cp src compiler.Main --phase=$1 --dump=$1 test/$1/$2.prev && cat test/$1/$2.log
