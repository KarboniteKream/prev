### Compilation

```sh
$ javac -cp src src/compiler/Main.java
```

### Usage

```sh
$ java -cp src compiler.Main --phase=lexan|synan --dump=lexan|synan|ast FILE.prev
```
OR
```sh
$ ./compile.sh lexan|synan|ast PHASE/TEST_FILE
```
