#!/bin/sh

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

if [ -e ${WAVE_TMP}/wls_link_stat ]; then
	LAST_STATUS=`awk -F "=" '/^WLSLinksStatus/ {str = $2; gsub(/ /, "", str); sub(/\r/, "", str); print str}' ${WAVE_TMP}/wls_link_stat`
else
	LAST_STATUS=0
fi

if [ "$1" = "w1" ]; then
	echo fapi_wlan_wave_linkstat_event.sh:Wireless Link is UP >/dev/console
	echo "WLSLinksStatus = 1" > ${WAVE_TMP}/wls_link_stat
	[ "$LAST_STATUS" = "1" ] && echo fapi_wlan_wave_linkstat_event.sh:Already up >/dev/console					
	exit
fi

if [ "$1" = "w0" ]; then
	echo fapi_wlan_wave_linkstat_event.sh:Wireless Link is DOWN >/dev/console
	echo "WLSLinksStatus = 0" > ${WAVE_TMP}/wls_link_stat
	exit
fi

echo fapi_wlan_wave_linkstat_event.sh:Unknown event $1 >/dev/console
exit