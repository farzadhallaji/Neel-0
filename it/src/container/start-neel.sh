#!/bin/bash

trap 'kill -TERM $PID' TERM INT
echo Options: $NEEL_OPTS
java $NEEL_OPTS -jar /opt/neel/neel.jar /opt/neel/template.conf &
PID=$!
wait $PID
trap - TERM INT
wait $PID
EXIT_STATUS=$?
