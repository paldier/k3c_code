#!/bin/sh

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

command=$1

drvhlpr_dut="drvhlpr_dut"

start_dut_helper() {
	print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: Start DUT Helper"
	echo "f_saver = $ETC_PATH/fapi_wlan_wave_dut_file_saver.sh" > $WAVE_TMP/$drvhlpr_dut.config
	cp -s $BINDIR/drvhlpr $WAVE_TMP/$drvhlpr_dut
	$WAVE_TMP/$drvhlpr_dut --dut -p $WAVE_TMP/$drvhlpr_dut.config &
	print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: Start DUT Helper Done"
}

stop_dut_helper() {
	print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: Stop DUT Helper"
	killall $drvhlpr_dut
	print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: Stop DUT Helper Done"
}

start_dut_drvctrl() {
	print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: Start"
	(. $ETC_PATH/fapi_wlan_wave_init_driver.sh start dut)
	start_dut_helper
	touch $WAVE_TMP/dut_mode_on
	print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: Start done"
}

safe_driver_stop() {
	# rmmod other drivers
	l2f_count=`lsmod | grep wave_wifi_l2f`
	[ -n "$l2f_count" ] && $ETC_PATH/wave_wifi_l2f_ctrl.sh stop
	
	parp_count=`lsmod | grep wave_wifi_parp`
	[ -n "$parp_count" ] && $ETC_PATH/wave_wifi_parp_ctrl.sh stop

	wmd_count=`ps | grep wmd | grep -vc grep`
	[ "$wmd_count" -gt "0" ] && $ETC_PATH/wmdctrl.sh stop

    $ETC_PATH/fapi_wlan_wave_init_driver.sh stop
}

first_stop_dut_drvctrl() {
    print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: First Stop"
    # We have to stop all the wave AP/VAPs
	ifconfig | grep $interface_name > ${WAVE_TMP}/ifconfig_res
	vaps_list=`get_interfaces_from_ifconfig ${WAVE_TMP}/ifconfig_res`
	for current_vap in $vaps_list; do
		print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: Stoping Wave interface $current_vap"
		$ETC_PATH/fapi_wlan_wave_down $current_vap
	done
	rm -f ${WAVE_TMP}/ifconfig_res
	
    # rmmod driver
    safe_driver_stop
    print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: First Stop done"
}

stop_dut_drvctrl() {
	print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: Stop"
	rm -f $WAVE_TMP/dut_mode_on
	# rmmod driver
	safe_driver_stop
	stop_dut_helper
	print2log DBG "fapi_wlan_wave_dut_drvctrl.sh: Stop done"
}

case $command in
	start)
		start_dut_drvctrl
		;;
	stop)
		if [ -e /tmp/dut_mode_on ]; then
			stop_dut_drvctrl
		else
			first_stop_dut_drvctrl
		fi
		;;
	start_helper)
		start_dut_helper
		;;
	stop_helper)
		stop_dut_helper
		;;
	*)
		print2log WARNING "fapi_wlan_wave_dut_drvctrl.sh: Unknown command=$command"
		;;
esac
