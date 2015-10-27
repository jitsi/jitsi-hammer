#!/bin/bash

if [ $# -lt 2 ] ; then #[ "$1" == "--help" ] || [ "$1" == "-help" ] || [ "$1" == "-h" ] || [ $# -lt 1 ] ; then
    set -- "-help"
fi

kernel="$(uname -s)"
if [ $kernel == "Darwin" ] ; then
	SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

	architecture="mac"
else
	SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

	if [ $kernel == "Linux" ] ; then
		architecture="linux"
	elif [ $kernel == "FreeBSD" ] ; then
		architecture="freebsb"
	else
		echo "Unknown system : "$kernel
		echo "Must be mac, linux, freebsb or windows"
		exit 1
	fi

	machine="$(uname -m)"
	if [ "$machine" == "x86_64" ] || [ "$machine" == "amd64" ] ; then
		architecture=$architecture"-64"
	fi
fi

mainClass="org.jitsi.hammer.Main"
classpath=$(JARS=($SCRIPT_DIR/jitsi-hammer.jar $SCRIPT_DIR/lib/*.jar); IFS=:; echo "${JARS[*]}")
libs="$SCRIPT_DIR/lib/native/$architecture"
logging_config="$SCRIPT_DIR/lib/logging.properties"

LD_LIBRARY_PATH=$libs:$LD_LIBRARY_PATH java -Djava.library.path=$libs \
  -Djava.util.logging.config.file=$logging_config \
  -Dnet.java.sip.communicator.SC_HOME_DIR_LOCATION=$SCRIPT_DIR \
  -Dnet.java.sip.communicator.SC_HOME_DIR_NAME=.jitsi-hammer \
  -cp $classpath $mainClass $@ -logfile >(cat >&2) 2>&1
