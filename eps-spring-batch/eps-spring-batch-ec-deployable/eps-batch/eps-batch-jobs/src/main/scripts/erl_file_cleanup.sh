EPS_PROPERTIES=../config/env/eps-env.properties

PROCESSED_DIR=`cat $EPS_PROPERTIES | grep "eps.processedfolder.erl" | cut -d':' -f2`
INVALID_DIR=`cat $EPS_PROPERTIES | grep "eps.invalidfolder.erl" | cut -d':' -f2`
DAYS_OLDER=`cat $EPS_PROPERTIES | grep "erl.file.days.older" | cut -d'=' -f2`

OldProcFiles=`find $PROCESSED_DIR -mtime +$DAYS_OLDER | wc -l`
OldInvalidDirectories=`find $INVALID_DIR -type d -mtime +$DAYS_OLDER | wc -l`
OldInvalidFiles=`find $INVALID_DIR -type f -mtime +$DAYS_OLDER | wc -l`

echo 'Old Processed files to be removed :' $OldProcFiles
echo 'Old Invalid files to be removed : '  $OldInvalidFiles
echo 'Old Invalid Directories to be removed : '  $OldInvalidDirectories

exec `find $PROCESSED_DIR -mtime +$DAYS_OLDER -print0 | xargs -0 rm -rf \;`
exec `find $INVALID_DIR -mindepth 1 -maxdepth 1 -type d -mtime +$DAYS_OLDER -print0 | xargs -0 rm -rf \;`
exec `find $INVALID_DIR -type f -mtime +$DAYS_OLDER -print0 | xargs -0 rm -rf \;`

echo 'Old files and folders from processed and invalid directories have been removed.'
