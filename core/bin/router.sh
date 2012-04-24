#bin/router
cmd=$1
baseDir=$EDA_HOME

case $cmd in

start)
./startRouter.sh
;;
stop)
./stopRouter.sh
;;
version)
cat $baseDir/version
;;




*)
echo "Usage: router.sh {start|stop|version}"
;;

esac

exit 0







#srcfile=$1


#targetdir=$2
#mv ${srcfile} ${targetdir}


