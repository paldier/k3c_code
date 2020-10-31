#!/bin/sh

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

[ -e $LED_VARS_FILE ] && source $LED_VARS_FILE

print2log DBG "fapi_wlan_wave_hostapd_events.sh: Start"

interface_name=$1
event_name=$2
event_param3=$3
event_param4=$4

print2log DBG "fapi_wlan_wave_hostapd_events.sh: args: $*"

conf_via_external() {
	print2log DBG "fapi_wlan_wave_hostapd_events.sh: conf_via_external start"
	(. $ETC_PATH/fapi_wlan_wave_wps.sh wps_external_done $interface_name)
	print2log DBG "fapi_wlan_wave_hostapd_events.sh: conf_via_external done"
}

wps_pin_needed() {
	sta_uuid=$event_param3
	sta_mac=$event_param4
	# Change MAC to lowercase
	sta_mac=`echo $sta_mac | tr '[:upper:]' '[:lower:]'`
	
	print2log DBG "fapi_wlan_wave_hostapd_events.sh: Start wps_pin_needed for MAC=$sta_mac with uuid=$sta_uuid"
	
	# Compare between MAC in requesting sta and MAC listed as allowed in DB (converted to lowercase).
	allowed_mac=`cat $WPS_MAC_TEMP_FILE | tr '[:upper:]' '[:lower:]'`
	print2log DBG "fapi_wlan_wave_hostapd_events.sh: allowed_mac=$allowed_mac"
	# No allowed mac was found
	if [ -z "$allowed_mac" ]; then
		print2log DBG "fapi_wlan_wave_hostapd_events.sh: not expecting WPS-PIN session, no allowed MACs found"
		exit
	fi
	# Requesting STA's MAC is allowed, perform pin connection
	if [ "$allowed_mac" = "$sta_mac" ]; then
		print2log DBG "fapi_wlan_wave_hostapd_events.sh: connecting STA via PIN"
		$ETC_PATH/fapi_wlan_wave.sh conf_via_pin $interface_name 0 0 $sta_mac $sta_uuid
	else
		print2log DBG "fapi_wlan_wave_hostapd_events.sh: STA $sta_mac tried PIN connection but wasn't allowed"
	fi
	print2log DBG "fapi_wlan_wave_hostapd_events.sh: Done wps_pin_needed"
}

wps_in_progress()
{
	# multicolor WPS LED: fast blink amber
	if [ -n "$led_wps_count" -a $led_wps_count -eq 1 -a -n "$wps_green" -a -n "$wps_amber" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_amber 0 400 $wps_amber,200 0,100 )
	# unicolor WPS LED: blink green
	elif [ -n "$led_wps_count" -a -n "$wps_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_blink $led_wps_green 500 500 )
	# no WPS LED, but multicolor WLAN0 LED: fast blink amber
	elif [ -n "$led_wlan0_count" -a $led_wlan0_count -eq 1 -a -n "$wlan0_green" -a -n "$wlan0_amber" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_amber 0 400 $wlan0_amber,200 0,100 )
	# no WPS LED, but unicolor WLAN0 LED: blink green
	elif [ -n "$led_wlan0_count" -a -n "$wlan0_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_blink $led_wlan0_green 500 500 )
	fi
}

wps_auth_started()
{
	# multicolor WPS LED: slow blink green and amber
	if [ -n "$led_wps_count" -a $led_wps_count -eq 1 -a -n "$wps_green" -a -n "$wps_amber" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_green $wps_green -1 $wps_green,1000 $wps_amber,1000 )
	# unicolor WPS LED: slow blink green
	elif [ -n "$led_wps_count" -a -n "$wps_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_blink $led_wps_green 1000 1000 )
	# no WPS LED, but multicolor WLAN0 LED: slow blink green and amber
	elif [ -n "$led_wlan0_count" -a $led_wlan0_count -eq 1 -a -n "$wlan0_green" -a -n "$wlan0_amber" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_green $wlan0_green -1 $wlan0_green,1000 $wlan0_amber,1000 )
	# no WPS LED, but unicolor WLAN0 LED: slow blink green
	elif [ -n "$led_wlan0_count" -a -n "$wlan0_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_blink $led_wlan0_green 1000 1000 )
	fi
}

wps_error()
{
	# multicolor WPS LED: fast blink red for 2 minutes, then stay green
	if [ -n "$led_wps_count" -a $led_wps_count -eq 1 -a -n "$wps_green" -a -n "$wps_red" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_green $wps_green 600 $wps_red,100 0,100 )
	# unicolor WPS LEDs: fast blink red for 2 minutes, then stay green
	elif [ -n "$wps_green" -a -n "$wps_red" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_green $wps_green 1 0,120000 )
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_blink $led_wps_red 100 100 120 )
	# unicolor WPS LED (green only): fast blink green for 2 minutes, then stay green
	elif [ -n "$wps_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_green $wps_green 600 $wps_green,100 0,100 )
	# no WPS LED, but multicolor WLAN0 LED: fast blink red for 2 minutes, then stay green
	elif [ -n "$led_wlan0_count" -a $led_wlan0_count -eq 1 -a -n "$wlan0_green" -a -n "$wlan0_red" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_green $wlan0_green 600 $wlan0_red,100 0,100 )
	# no WPS LED, but unicolor WLAN0 LEDs: fast blink red for 2 minutes, then stay green
	elif [ -n "$wlan0_green" -a -n "$wlan0_red" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_green $wlan0_green 1 0,120000 )
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_blink $led_wlan0_red 100 100 120 )
	# no WPS LED, but unicolor WLAN0 LED (green only): fast blink green for 2 minutes, then stay green
	elif [ -n "$wlan0_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_green $wlan0_green 600 $wlan0_green,100 0,100 )
	fi
}

wps_session_overlap()
{
	# multicolor WPS LED: blink red for 2 minutes, then stay green
	if [ -n "$led_wps_count" -a $led_wps_count -eq 1 -a -n "$wps_green" -a -n "$wps_red" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_green $wps_green 80 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 0,500 )
	# unicolor WPS LEDs:  blink red for 2 minutes, then stay green
	elif [ -n "$wps_green" -a -n "$wps_red" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_green $wps_green 1 0,120000 )
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_red 0 80 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 $wps_red,100 0,100 0,500 )
	# unicolor WPS LED (green only): fast blink green for 2 minutes, then stay green
	elif [ -n "$wps_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_green $wps_green 600 $wps_green,100 0,100 )
	# no WPS LED, but multicolor WLAN0 LED: fast blink red for 2 minutes, then stay green
	elif [ -n "$led_wlan0_count" -a $led_wlan0_count -eq 1 -a -n "$wlan0_green" -a -n "$wlan0_red" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_green $wlan0_green 80 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 0,500 )
	# no WPS LED, but unicolor WLAN0 LEDs: fast blink red for 2 minutes, then stay green
	elif [ -n "$wlan0_green" -a -n "$wlan0_red" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_green $wlan0_green 1 0,120000 )
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_red 0 80 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 $wlan0_red,100 0,100 0,500 )
	# no WPS LED, but unicolor WLAN0 LED (green only): fast blink green for 2 minutes, then stay green
	elif [ -n "$wlan0_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_green $wlan0_green 600 $wlan0_green,100 0,100 )
	fi
}

wps_success()
{
	# WPS LED: slow blink green 5 times, then stay green
	if [ -n "$led_wps_count" -a -n "$wps_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_green $wps_green 5 $wps_green,1000 0,1000 )
	# no WPS LED, but WLAN0 LED: slow blink green 5 times, then stay green
	elif [ -n "$led_wlan0_count" -a -n "$wlan0_green" ]; then
		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wlan0_green $wlan0_green 5 $wlan0_green,1000 0,1000 )
	fi
}

timestamp=`date -Iseconds`
# Write to file only when CLI WPS session started.
# CLI WPS command create CLI_WPS_IN_PROCESS file when WPS session start, delete it when finish .
# It also mv wave_wlan_hostapd_events.log to wave_wlan_hostapd_events_prev.log for debug history.
if [ -e $CLI_WPS_IN_PROCESS ]
then
	echo "[$timestamp] $*" >> /tmp/wave_wlan_hostapd_events.log
fi

if [ -e /tmp/WPS_STATE ]; then
	source /tmp/WPS_STATE
else
	WPS_STATE=WPS_OFF
fi

# WPS state machine
case $event_name in
	"WPS-NEW-AP-SETTINGS")
		conf_via_external
	;;
	"WPS-PIN-NEEDED")
		wps_pin_needed
	;;
	"WLAN-MODE-INIT")
		print2log DBG "wave_wlan_hostpad_events: TODO: add LED flashing implementation for event WLAN-MODE-INIT"
		# WLAN LED: flashing green for WLAN network activity
		#if [ "$interface_name" = "wlan0" -a -n "$led_wlan0_green" ]; then
		#	( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_netif_activity_trigger $led_wlan0_green $interface_name )
		#elif [ "$interface_name" = "wlan1" -a -n "$led_wlan1_green" ]; then
		#	( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_netif_activity_trigger $led_wlan1_green $interface_name )
		#fi
		# Initialize WPS and APMODE LEDs (only for WLAN-MODE-INIT of wlan0)
		if [ "$interface_name" = "wlan0" ]; then
			# check if WPS is enabled
			wps_on=`get_wps_on $interface_name`
			if [ "$wps_on" = "$YES" ]; then
				WPS_STATE="WPS_IDLE"
 			else
				WPS_STATE="WPS_OFF"
 			fi
			#if [ -n "$led_wps_green" ]; then
			#	if [ "$WPS_STATE" != "WPS_OFF" ]; then
			#		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_sequence $led_wps_green $wps_green 0 )
			#	else
			#		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_off $led_wps_green )
			#	fi
			#fi
			#if [ -n "$led_apmode_green" ]; then
			#	if [ "$event_param3" = "sta" ]; then
			#		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_off $led_apmode_green )
			#	else
			#		( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_on $led_apmode_green )
			#	fi
			#fi
		fi
	;;
	"WPS-SESSION-START")
		if [ "$WPS_STATE" != "WPS_OFF" ]; then
			WPS_STATE="WPS_STARTED"
			print2log DBG "wave_wlan_hostpad_events: TODO: add LED flashing implementation for WPS_STATE=$WPS_STATE"
			#wps_in_progress
		fi
	;;
	"CTRL-EVENT-EAP-STARTED")
		if [ "$WPS_STATE" = "WPS_STARTED" ]; then
			WPS_STATE=$event_param3
			print2log DBG "wave_wlan_hostpad_events: TODO: add LED flashing implementation for WPS_STATE=$WPS_STATE"
			#wps_auth_started
		fi
	;;
	"WPS-REG-SUCCESS")
		WPS_STATE=$event_param3
		print2log DBG "wave_wlan_hostpad_events: TODO: add LED flashing implementation for WPS_STATE=$WPS_STATE"
		#wps_success
	;;
	"AP-STA-CONNECTED" | "CONNECTED")
		if [ "$WPS_STATE" = "$event_param3" ]; then
			WPS_STATE="WPS_IDLE"
		fi
		print2log DBG "wave_wlan_hostpad_events: TODO: add LED flashing implementation for WPS_STATE=$WPS_STATE"
		# WLAN0 LED: flashing green for WLAN0 network activity
		#if [ -n "$led_wlan0_count" -a -n "$wlan0_green" ]; then
		#	( . $ETC_PATH/fapi_wlan_wave_led_control.sh led_netif_activity_trigger $led_wlan0_green wlan0 )
		#fi
	;;
	"WPS-TIMEOUT")
		WPS_STATE="WPS_IDLE"
		print2log DBG "wave_wlan_hostpad_events: TODO: add LED flashing implementation for WPS_STATE=$WPS_STATE"
		#wps_error
	;;
	"AP-STA-DISCONNECTED")
		if [ "$WPS_STATE" = "$event_param3" ]; then
			WPS_STATE="WPS_IDLE"
			print2log DBG "wave_wlan_hostpad_events: TODO: add LED flashing implementation for WPS_STATE=$WPS_STATE"
			#wps_error
		fi
	;;
	"WPS-OVERLAP-DETECTED")
		WPS_STATE="WPS_IDLE"
		print2log DBG "wave_wlan_hostpad_events: TODO: add LED flashing implementation for WPS_STATE=$WPS_STATE"
		#wps_session_overlap
	;;
	*)
		echo "wave_wlan_hostapd_events: $event_name"
	;;
esac
echo "WPS_STATE=$WPS_STATE" > /tmp/WPS_STATE
print2log DBG "wave_wlan_hostpad_events: Done"
