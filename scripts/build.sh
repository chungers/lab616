#!/bin/bash

# Builds a set of projects with maven.
config=$1
shift;
tasks=$@

function buildProject {
	project=$1; shift;
	echo "Building $project"
	cmd="pushd ${project}; mvn ${tasks}; popd"
	eval ${cmd} | tee /tmp/build-$project
	result=`grep "BUILD SUCCESSFUL" /tmp/build-$project`
	if [[ "$result" == "" ]]; then
	    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!! Build failed @ $project"
	    exit -1
	fi
}

function buildSet {
    echo "Building $@"
    for p in $@; do
	buildProject $p
    done
}

case "${config}" in
common)
	modules="
lab616-common
"
;;
omnibus)
	modules="
lab616-common
lab616-omnibus
"
;;
trading)
	modules="
lab616-common
lab616-omnibus
lab616-trading
"
;;
tws)
	modules="
lab616-common
lab616-omnibus
lab616-tws
"
;;
scalatrader)
	modules="
lab616-common
lab616-omnibus
lab616-trading
app-scalatrader
"
;;
*)
echo "Unknown: ${config}"
exit -1
;;
esac

echo "Building ${modules}"
buildSet ${modules}



