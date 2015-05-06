### Compiling

```sh
$ javac -cp src src/compiler/Main.java
```

### Running

```sh
$ java -cp src compiler.Main --phase=PHASE --dump=DUMP FILE.prev
```
or
```sh
$ ./compile.sh PHASE PHASE/FILENAME
```

```
PHASE/DUMP = lexan|synan|ast|seman|frames|imcode|lincode
```
