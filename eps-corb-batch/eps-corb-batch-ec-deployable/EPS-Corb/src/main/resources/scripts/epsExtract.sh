erl_properties=../config/env/erl.properties
source $erl_properties

# source highwatermark out of the properties file
highwatermarkFilename=$CORB_PROP_DIR/highwatermark.properties
source $highwatermarkFilename

# The propertiesFilename template and file that will be generated
loggingFilename="$LOGGING_DIR/$jobId-`date +%Y%m%dT%H%M%S`.txt"
propertiesFilename=$CORB_PROP_DIR/corb.properties
templateFilename=../config/env/corb.properties.template
manifestFilename=$CORB_MANIFEST_DIR/Manifest-$jobId.txt
outputBerPath=$OUTPUTPATH_DIR/$jobId
invalidDirectory=$INVALID_DIR
numInBatch=`cat $templateFilename | grep "URIS-MODULE.numInBatch" | cut -d'=' -f2`
timeflag=`date +%s%N`
# Get a set of properties from the common properties file
#TODO

# Build the CORB properties file with the highwatermark value
eval "cat $templateFilename > $propertiesFilename"
eval "echo URIS-MODULE.manifestFilename=$manifestFilename >> $propertiesFilename"
eval "echo URIS-MODULE.highwatermarkFilename=$highwatermarkFilename >> $propertiesFilename"

eval "echo URIS-MODULE.highwaterMark=$highwaterMark >> $propertiesFilename"
eval "echo XQUERY-MODULE.highwaterMark=$highwaterMark >> $propertiesFilename"

eval "echo URIS-MODULE.jobId=$jobId >> $propertiesFilename"
eval "echo XQUERY-MODULE.jobId=$jobId >> $propertiesFilename"

eval "echo URIS-MODULE.PAET=$PAET >> $propertiesFilename"
eval "echo URIS-MODULE.PAETCompletion=$PAETCompletion >> $propertiesFilename"
eval "echo XQUERY-MODULE.outputBerPath=$outputBerPath >> $propertiesFilename"
eval "echo EXPORT-FILE-DIR=$outputBerPath >> $propertiesFilename"


# Create directory for BER output
mkdir -p $outputBerPath

# Put values in the manifest file
eval "echo JobId=$jobId > $manifestFilename"
eval "echo BeginHighWaterMark=$highwaterMark >> $manifestFilename"
eval "echo JobStartTime=`date +%Y-%m-%dT%H:%M:%S-05:00` >> $manifestFilename"

# Execute the CORB job
export PATH=/sbin:/bin:/usr/sbin:/usr/bin:$JAVA_HOME/bin
java -cp "../../../secure:../lib/marklogic-corb-2.3.1.jar:../lib/marklogic-xcc-8.0.5.jar:../lib/jasypt-1.9.2.jar" -DOPTIONS-FILE=$propertiesFilename com.marklogic.developer.corb.Manager 2>&1 | tee $loggingFilename

eval "echo JobStatusCode = $?"
# Get ExtractionJobEndTime
eval "echo JobEndTime=`date +%Y-%m-%dT%H:%M:%S-05:00` >> $manifestFilename"

# Cleanup filenames of output files (to remove ">" sign, and add .T)
for file in $outputBerPath/*
do
	mv "$file" "${file/>/}"
done

# Read newHighwaterMark-highwatermark* file into an Array
lines=($(cat $outputBerPath/newHighwaterMark-highwatermark*)) # array

# Determine the RecordCount from the output files (and add to manifest file)
RecordCount=`cat $outputBerPath/*.T.* | grep -o '<bem:BenefitEnrollmentMaintenance>' | wc -l`
eval "echo RecordCount=$RecordCount >> $manifestFilename"


# Reattach part files to main files.

mainFiles=()
partfiles=($outputBerPath/*.part2)
# Check if part files exists
if [ -e "${partfiles[0]}" ]; then
# Get main file names from part2 files
for file in $outputBerPath/*.part2
do
    parentfile=`echo $file |  cut -d . -f 1-6`
    mainFiles+=("$parentfile")
done
# Loop though main files and remove the ending BER tag. Append part files and then attach the BER tag again. Delete the part file after appending.
for mainFile in "${mainFiles[@]}"
    do
        echo 'Attaching part files to '$mainFile
        eval `sed -i -- 's/<\/bem:BenefitEnrollmentRequest>//' $mainFile`
        for file in $mainFile.part*
            do
			eval `sed -i -- 's/bem:BenefitEnrollmentMaintenance xmlns:bem="http:\/\/bem.dsh.cms.gov"/bem:BenefitEnrollmentMaintenance/' $file`
            cat $file >> $mainFile
            rm $file
            done
        eval `echo '<\/bem:BenefitEnrollmentRequest>' >>  $mainFile`
    done
fi


# Check Output directory for errors, save jobstatus
if grep -i "CorbException" $loggingFilename; then
  eval "echo JobStatus=FAILED >> $manifestFilename"
  JobStatus=1
elif grep -i "completed all tasks" $loggingFilename; then
  eval "echo JobStatus=SUCCESS >> $manifestFilename"
  JobStatus=0
elif grep -i "nothing to process" $loggingFilename; then
  eval "echo JobStatus=SUCCESS >> $manifestFilename"
  JobStatus=0 
else
  eval "echo JobStatus=FAILED >> $manifestFilename"
  JobStatus=2
fi

eval "echo JobStatus=$JobStatus"

if [[ $JobStatus -eq 0 ]]; then 
	
	# Overwrite the newly generated highwatermark.properties
	eval "cat $outputBerPath/newHighwaterMark-highwatermark* > $highwatermarkFilename"
	eval "rm $outputBerPath/newHighwaterMark-highwatermark*"

	# Append the end highwatermark
	eval "cat $outputBerPath/newHighwaterMark* >> $manifestFilename"
	eval "rm $outputBerPath/newHighwaterMark*";
else
    eval "echo Job Failed. Moving manifest and files to invalid directory"
    mkdir $invalidDirectory/$jobId-invalid-$timeflag
    mv $manifestFilename $invalidDirectory/$jobId-invalid-$timeflag/.
    mv $outputBerPath/* $invalidDirectory/$jobId-invalid-$timeflag/.
    rm -rf $outputBerPath
fi

# Log the pendingExtractCount variable from the newHighwaterMark file.
array=(${lines[9]//=/ })
totalIPPCountTobeExtracted=${array[1]}
array=(${lines[11]//=/ })
pendingCount=${array[1]}

eval "echo totalIPPCountTobeExtracted=$totalIPPCountTobeExtracted";
eval "echo pendingCount=$pendingCount";
eval "echo numInBatch=$numInBatch";

if [[ $JobStatus -eq 0 && $totalIPPCountTobeExtracted -eq 0 ]]; then
   eval "echo Tobe Extacted is less than threshhold"
   JobStatus=99
fi

eval "echo JobStatus=$JobStatus"
exit $JobStatus

