#!/bin/bash
cd "$(dirname "$0")"

java_exec="java"
DEFAULT_MIN_MEM=128m
DEFAULT_MAX_MEM=4096m
java_home_check="/opt/jboss/java"
java_location=$JAVA_HOME
NEW_RELIC_DIR="/opt/cms/batch/newrelic"

usage() {
        launcher_package=`echo $job_runner_class | sed -e 's/\./\//g'`
        launcher_classname=`$launcher_package`
        echo -e "\n
        Runs a batch job by starting a Spring Batch Framework's 
        $launcher_classname
        which runs all steps of a user defined batchjob.\n\n
        Usage: $0 <jar file name> <job configuration file> <job name> <source:ffm|hub>\n"         
}

if [ $# -lt 3 ]; 
 then
  usage
 exit 1
fi

if [ "$JAVA_HOME" == ""  ];
 then
 java_location=$java_home_check
  
fi

MIN_MEM=${USER_MIN_MEM:-$DEFAULT_MIN_MEM}
MAX_MEM=${USER_MAX_MEM:-$DEFAULT_MAX_MEM}
JAVA_OPTIONS="-Xms${MIN_MEM} -Xmx${MAX_MEM}"

if [ -e $NEW_RELIC_DIR/newrelic.jar ]
then
LOG_DIR="$NEW_RELIC_DIR/logs"
JAVA_OPTIONS="$JAVA_OPTIONS -Dnewrelic.environment=ffm$5 -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:HeapDumpPath=$LOG_DIR -javaagent:$NEW_RELIC_DIR/newrelic.jar -Xloggc:$LOG_DIR/gc-ffm$4$5-$(date +%Y%m%d-%H%M%S).log"
fi


$java_location/bin/$java_exec $JAVA_OPTIONS -cp "../../../secure/:../config/env/:../config/app/:../lib/*:./*" org.springframework.batch.core.launch.support.CommandLineJobRunner $1 $2 source=$3 jobType=$4 timestamp="$(date +'%m%d%Y-%k:%M:%S')"