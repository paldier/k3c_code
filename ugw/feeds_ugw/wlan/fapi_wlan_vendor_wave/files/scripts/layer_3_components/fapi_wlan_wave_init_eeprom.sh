#!/bin/sh
return_status=true

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

command=$1

eeprom_partition=wlanconfig
eeprom_tar=eeprom.tar.gz

start_eeprom() {
	print2log DBG "fapi_wlan_wave_init_eeprom.sh: Start"
	
	# Check partition exists
	if [ `cat /proc/mtd | grep -E "\<$eeprom_partition\>|calibration" -c` -gt 0 ]; then
		vol_mgmt read_calibration $eeprom_partition /tmp/$eeprom_tar
		tar xzf /tmp/$eeprom_tar -C /tmp/
		if [ $? != 0 ]; then
			return_status=false
			print2log DBG "fapi_wlan_wave_init_eeprom.sh: failed to extract $eeprom_tar"
		fi
	else
		return_status=false
		print2log DBG "fapi_wlan_wave_init_eeprom.sh: the partition $eeprom_partition doesn't exist"
	fi
	print2log DBG "fapi_wlan_wave_init_eeprom.sh: Done start"
}

stop_eeprom() {
	return
}

eeprom_should_run() {
	print2log DBG "fapi_wlan_wave_init_eeprom.sh: should_run"
	return_status=`check_failure wlan0`
}

case $command in
	start)
		start_eeprom
		;;
	stop)
		stop_eeprom
		;;
	should_run)
		eeprom_should_run
		;;
	*)
		print2log WARNING "fapi_wlan_wave_init_eeprom.sh: Unknown command=$command"
		;;
esac
$return_status
