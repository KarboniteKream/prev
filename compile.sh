#!/bin/bash
javac -cp src src/compiler/Main.java && java -cp src compiler.Main --phase=$1 --dump=$1 test/$2.prev && cat test/$2.log
