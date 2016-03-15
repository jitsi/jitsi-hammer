#!/bin/bash -e

# NOTE bash is required.

KEYSTORE_FILE=keystore.ks
KEYSTORE_PWD=123456

if [ ! -f "$KEYSTORE_FILE" ]
then
    while getopts ":u:" o; do
      case "${o}" in
        u) BOSH_URI=${OPTARG}
	  ;;
      esac
    done

    OLDIFS="$IFS"
    IFS='/' read -r -a array <<< "$BOSH_URI"
    IFS="$OLDIFS"
    if [ ${array[0]} = "https:" ]
    then
      CERT=$(mktemp /tmp/temporary-file.XXXXXXXX)
      openssl s_client -showcerts -connect ${array[2]}:443 </dev/null 2>/dev/null|openssl x509 -outform PEM > $CERT
      keytool -import -alias ${array[2]} -file $CERT -keystore "$KEYSTORE_FILE" -storepass "$KEYSTORE_PWD"
    fi
fi

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
  -Djavax.net.ssl.keyStore=$KEYSTORE_FILE -Djavax.net.ssl.keyStorePassword=$KEYSTORE_PWD \
  -Djavax.net.ssl.trustStore=$KEYSTORE_FILE -Djavax.net.ssl.trustStorePassword=$KEYSTORE_PWD \
  -Djava.util.logging.config.file=$SCRIPT_DIR/lib/logging.properties \
  -Dnet.java.sip.communicator.SC_HOME_DIR_LOCATION=$SC_HOME_DIR_LOCATION \
  -Dnet.java.sip.communicator.SC_HOME_DIR_NAME=$SC_HOME_DIR_NAME 2>&1 | tee $LOG_HOME/output.log
