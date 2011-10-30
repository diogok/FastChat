#!/bin/sh
lein clean, deps
java -cp "src:test:classes:lib/*:lib/dev/*" lazytest.watch src test
