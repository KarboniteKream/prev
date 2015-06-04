### Compiling

```sh
$ javac -cp src src/compiler/Main.java
```

### Running

```sh
$ java -cp src compiler.Main --phase=PHASE --dump=DUMP [--registers=8] FILE.prev
```
or
```sh
$ ./compile.sh PHASE PHASE/FILENAME [REGISTERS]
```

```
PHASE/DUMP = lexan|synan|ast|seman|frames|imcode|lincode|asmcode|tmpan|regalloc
```
