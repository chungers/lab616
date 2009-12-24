#!/bin/bash
export QHOME=~/q
export RLWRAP="DYLD_LIBRARY_PATH=~/q/mac/rlwrap ~/q/mac/rlwrap/rlwrap -i -c -r -f ~/q/mac/qcmds.txt"

alias q="${RLWRAP} ~/q/m32/q $*"
alias qcon="${RLWRAP} ~/q/mac/qcon$*"

