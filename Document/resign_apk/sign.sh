#!/bin/sh
java -jar apksigner.jar sign --ks xxx.keystore --ks-key-alias xxxAlias --ks-pass pass:xxxxxx --key-pass pass:xxxxxx --out $1 $2
