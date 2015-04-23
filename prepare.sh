#!/bin/bash

mkdir -p release/prev
cp -r src/ release/prev/src/
find release/prev/ -name "*.class" -type f -delete
cd release

if [ -f 63120217-$1.zip ]; then
	rm 63120217-$1.zip
fi

zip -r 63120217-$1.zip prev/
rm -r prev/
