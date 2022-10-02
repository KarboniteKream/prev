#### Compiling
```sh
$ javac -cp src -d build src/compiler/Main.java
$ java -cp build compiler.Main --phase=PHASE --dump=DUMP [--registers=8] FILE.prev
```
or
```sh
$ ./compile.sh PHASE PHASE/FILENAME [REGISTERS]
```

#### Running
```sh
$ ./run.sh PHASE/FILENAME
```

#### Options
```
PHASE/DUMP = lexan|synan|ast|seman|frames|imcode|lincode|asmcode|tmpan|regalloc|build
```
