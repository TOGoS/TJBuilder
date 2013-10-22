touch = java -jar TJBuilder-bootstrap.jar touch

all: TJBuilder.jar

clean:
	rm -rf bin TJBuilder.jar .java-src.lst

.PHONY: all clean .FORCE

src: .FORCE
	${touch} --latest-within="$@" "$@"

.java-src.lst: src
	find "$<" -name '*.java' >"$@"

bin: src .java-src.lst
	mkdir -p bin
	javac -d bin @.java-src.lst
	${touch} bin

TJBuilder.jar: bin
	jar cfe "$@" togos.tjbuilder.TJBuilder -C bin .
