tjb = java -jar TJBuilder-bootstrap.jar
touch = ${tjb} touch

all: TJBuilder.jar TJBuilder.jar.urn

clean:
	rm -rf bin TJBuilder.jar .java-src.lst

.DELETE_ON_ERROR:

.PHONY: all clean .FORCE

src: .FORCE
	${touch} --latest-within="$@" "$@"

.java-src.lst: src
	find "$<" -name '*.java' >"$@"

bin: src .java-src.lst
	mkdir -p bin
	javac -d bin -source 1.6 -target 1.6 @.java-src.lst
	${touch} bin

TJBuilder.jar: bin
	jar cfe "$@" togos.tjbuilder.TJBuilder -C bin .

TJBuilder.jar.urn: TJBuilder.jar
	${tjb} id "$<" >"$@"
