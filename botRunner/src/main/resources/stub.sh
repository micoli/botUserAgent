#!/bin/sh
#
# cat src/main/resources/stub.sh target/botUserAgent-0.5.1-SNAPSHOT-jar-with-dependencies.jar > botUserAgent.run && chmod +x botUserAgent.run
#
MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"
java=java
if test -n "$JAVA_HOME"; then
	java="$JAVA_HOME/bin/java"
fi
exec "$java" $java_args -jar $MYSELF "$@"
exit 1
