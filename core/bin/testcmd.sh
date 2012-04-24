case $1 in

start)
echo "start cmd"
;;

stop)
echo "stop cmd"
;;

*)
echo "Usage: testcmd.sh {start|stop}"
;;
esac

exit 0

