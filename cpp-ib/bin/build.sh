#!/bin/bash

TARGET=$1
HOME=$(dirname $0)/..

echo -e "Cleaning $TARGET"

find $HOME -name $TARGET -type f | xargs rm -f

cmake .
make install $TARGET
