#!/bin/sh
# This is the script set wls links.
return_status=true

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

print2log DBG "fapi_wlan_wave_init_links.sh.sh: args: $*"

command=$1
object_index=$2

start_wls_links()
{
	print2log DBG "fapi_wlan_wave_init_links.sh.sh: start"
	cp -s $DRIVER_PATH/mtlk.ko /tmp/mtlk.ko
	cp -s $IMAGES_PATH/* /tmp/
	
	# Check if need to work with FW debug bins.
	# if so, create symlinks from /tmp/ to debug bins.
	fw_debug_enable=`get_conf_param FWDebugEnabled $object_index wlan0`
	if [ "$fw_debug_enable" = "$YES" ]; then
		print2log ATTENTION "#################################################"
		print2log ATTENTION "########## YOU ARE USING FW DEBUG BINS ##########"
		print2log ATTENTION "#################################################"
		rm /tmp/ap_upper* /tmp/sta_upper_* /tmp/contr_lm*
		cd $IMAGES_PATH
		for bin in `ls ap_upper_*debug.bin sta_upper_*debug.bin contr_lm*debug.bin`; do
			bin_short=`basename $bin _debug.bin`
			bin_name=$bin_short.bin
			cp -s $IMAGES_PATH/$bin /tmp/$bin_name
		done
		cd - > /dev/null
	fi
	print2log DBG "fapi_wlan_wave_init_links.sh.sh: Done Start"
}

stop_wls_links() {
	print2log DBG "fapi_wlan_wave_init_links.sh.sh: stop"

	rm -f /tmp/ap_upper* /tmp/ProgModel_* /tmp/mtlk.ko /tmp/contr_lm.bin
	#if [ -e /tmp/sta_upper* ]; then rm /tmp/sta_upper*; fi
	print2log DBG "fapi_wlan_wave_init_links.sh.sh: Done stop"
}

create_config_wls_links() {
	return
}

should_run_wls_links() {
	return_status=`check_failure wlan0`
}

case $command in
	start)
		start_wls_links
		;;
	stop)
		stop_wls_links
		;;
	create_config)
		create_config_wls_links
		;;
	should_run)
		should_run_wls_links
		;;
esac

$return_status
