all: TJBuilder.jar

touch = java -jar TJBuilder-bootstrap.jar touch

clean:
	rm -rf bin TJBuilder.jar .java-src.lst

.PHONY: _dir_

src: _dir_
	${touch} -latest-within "$@" "$@"

.java-src.lst: src
	find "$<" -name '*.java' >"$@"

bin: src .java-src.lst
	mkdir -p bin
	javac -d bin @.java-src.lst
	${touch} bin

TJBuilder.jar: bin
	jar cfe "$@" togos.tjbuilder.TJBuilder -C bin .
