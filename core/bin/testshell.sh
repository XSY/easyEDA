echo "this is testing for shell exec from Java" >echo.log
cat echo.log
echo "my pid is $$"
arg1="testarg"
echo ${arg1}
arg2=$1
echo ${arg2:-null}
java -version



#!/bin/sh  

#SERVER=/home/java/server  
#cd $SERVER  

#case "$1" in  

#start)  
#nohup java -Xmx128m -jar server.jar > $SERVER/server.log 2>&1 &  
#echo $! > $SERVER/server.pid  
#;;  

#stop)  
#kill `cat $SERVER/server.pid`  
#rm -rf $SERVER/server.pid  
#;;  

#restart)  
#$0 stop  
#sleep 1  
#$0 start  
#;;  

#*)  
#echo "Usage: run.sh {start|stop|restart}"  
#;;  

#esac  

#exit 0  
