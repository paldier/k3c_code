#!/bin/sh
return_status=true

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

command=$1
object_index=$2

# Variables
TMPDIR="/tmp"
DEVDIR="$TMPDIR/dev"
device="mtlkroot"
module="mtlk_log"
device_log="mtlk_log"
logserver="logserver"
logdriver="mtlkroot.ko"
fw_scd=fw_scd_file.scd
device_log_minor="0"
mode="660"
user="root"
group="root"

check_logger_status() {		
	if [ ! -e $BINDIR/$logserver ] || [ ! -e $DRIVER_PATH/$logdriver ]; then
		print2log ALERT "fapi_wlan_wave_init_rtlogger: RT Logger didn't start: $BINDIR/$logserver or $DRIVER_PATH/$logdriver is missing"
		echo "fapi_wlan_wave_init_rtlogger: RT Logger didn't start: $BINDIR/$logserver or $DRIVER_PATH/$logdriver is missing" >> $wave_init_failure
		exit 1
	fi
}

insmod_device() {
	# We pass Netlink ID to driver. After reboot we pass 0, else we pass what the kernel allocated for reusing the same ID.
	if [ -e $GENL_FAMILY_ID_FILE ]; then
		mtlk_genl_family_id=`cat $GENL_FAMILY_ID_FILE`
	else
		mtlk_genl_family_id=0
	fi
	print2log DBG "fapi_wlan_wave_init_rtlogger: mtlk_genl_family_id=$mtlk_genl_family_id"

	cd $TMPDIR
	insmod $device.ko mtlk_genl_family_id=$mtlk_genl_family_id
	res=$?	
	# verify that insmod was successful
	count=0
	while [ $res != 0 ]; do
		if [ "$count" -lt "10" ]; then
			insmod $device.ko mtlk_genl_family_id=$mtlk_genl_family_id
			res=$?
			sleep 1
		else
			print2log ALERT "fapi_wlan_wave_init_rtlogger: insmod of $device is failed!!!"
			echo "fapi_wlan_wave_init_rtlogger: rtlogger insmod is failed" >> $wave_init_failure
			exit 1
		fi
		count=$((count+1))
	done

	cd - > /dev/null
}

rmmod_device() {
	rmmod $device.ko
	res=$?
	# verify that rmmod was successful
	count=0
	while [ $res != 0 ]; do
		if [ "$count" -lt "10" ]; then
			rmmod $device.ko
			res=$?
			sleep 1
		else
			print2log ALERT "fapi_wlan_wave_init_rtlogger: rmmod of $device is failed!!!"
			print2log INFO "verify to rmmod mtlk.ko"
			break
		fi
		count=$((count+1))
	done		
}

logger_up() {
	print2log DBG "fapi_wlan_wave_init_rtlogger: Start logger_up"
	check_logger_status
	if [ `lsmod | grep -c $device` -gt 0 ]; then 
		print2log INFO "fapi_wlan_wave_init_rtlogger: RT Logger is already up"
		return
	fi
	cp -s $DRIVER_PATH/$logdriver $TMPDIR/$logdriver
	cp -s $BINDIR/$fw_scd $TMPDIR/$fw_scd
	insmod_device
	mkdir -p ${DEVDIR}
	rm -f ${DEVDIR}/${device}? 
	device_log_major=`cat /proc/devices | awk "\\$2==\"$module\" {print \\$1}"`
	mknod ${DEVDIR}/${device}0 c $device_log_major $device_log_minor
	ln -sf ${device}${device_log_minor} ${DEVDIR}/${device_log}
	# give appropriate group/permissions
	[ "$LOGNAME" != "root" ] && chown $user:$group ${DEVDIR}/${device}[0-0]	
	chmod $mode ${DEVDIR}/${device}[0-0]
	# Start logserver
	logger_enabled=`get_conf_param RTLoggerEnabled $object_index wlan0`
	if [ "$logger_enabled" != "0" ]; then
		cp -s $BINDIR/$logserver $TMPDIR/$logserver
		/tmp/$logserver -f ${DEVDIR}/${device}0 -s  $TMPDIR/$fw_scd &
	fi
	print2log DBG "fapi_wlan_wave_init_rtlogger: End logger_up"
}

logger_down() {
	print2log DBG "fapi_wlan_wave_init_rtlogger: Start logger_down"
	check_logger_status
	if [ `lsmod | grep -c $device` = 0 ]; then 
		print2log INFO "fapi_wlan_wave_init_rtlogger: RT Logger is already down"
		return
	fi
	killall $logserver 2>/dev/null
	killall mtdump 2>/dev/null
	# remove nodes
	mkdir -p ${DEVDIR}
	rm -f ${DEVDIR}/${device}? 
	# unload driver
	rmmod_device
	rm -f $TMPDIR/$logserver
	rm -f $TMPDIR/$logdriver
	rm -f $TMPDIR/$fw_scd
	print2log DBG "fapi_wlan_wave_init_rtlogger: Done logger_down"
}

logger_should_run ()
{
	logger_enabled=`get_conf_param RTLoggerEnabled $object_index wlan0`
	print2log DBG "fapi_wlan_wave_init_rtlogger.sh: RTlogger_enabled=$logger_enabled."
	[ "$logger_enabled" != "1" ] && return_status=false
}

case $command in
	start)
		logger_up
		;;
	stop)
		logger_down
		;;
	should_run)
		logger_should_run
		;;
	*)
		echo "ERROR: Unknown command=$command"
		;;
esac
$return_status
