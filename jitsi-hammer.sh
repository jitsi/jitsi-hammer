#!/bin/sh -e

if [ $# -lt 1 ] ; then #[ "$1" == "--help" ] || [ "$1" == "-help" ] || [ "$1" == "-h" ] || [ $# -lt 1 ] ; then
    set -- "-help"
fi


KERN="$(uname -s)"
if [ "$KERN" = "Darwin" ] ; then
	SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
else
	SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"
fi

# we can make this a script parameter at some point.
REBUILD=true
SC_HOME_DIR_LOCATION=$SCRIPT_DIR
SC_HOME_DIR_NAME=.jitsi-hammer
LOG_HOME=$SCRIPT_DIR/$SC_HOME_DIR_NAME/log

if $REBUILD ; then
  mvn clean compile
fi

exec mvn exec:java -Dexec.args="$*" \
  -Djava.util.logging.config.file=$SCRIPT_DIR/lib/logging.properties \
  -Dnet.java.sip.communicator.SC_HOME_DIR_LOCATION=$SC_HOME_DIR_LOCATION \
  -Dnet.java.sip.communicator.SC_HOME_DIR_NAME=$SC_HOME_DIR_NAME 2>&1 | tee $LOG_HOME/output.log
