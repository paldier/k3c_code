#!/bin/sh

# Source fapi_wlan_wave_in.conf
. ${IN_CONF}

param_name=$1
object_index=$2
interface_name=$3

value=""

case "$param_name" in
	"interfaceName")
		eval interface_name_value=\${Name_${object_index}}
		value=$(printf "%b" "$interface_name_value")
		;;
	"BSSID")
		eval bssid_value=\${MACAddress_${object_index}}
		value=$(printf "%b" "$bssid_value")
		;;
	"ESSID")
		eval essid_value=\${SSID_${object_index}}
		value=$(printf "%b" "$essid_value")
		;;
esac

echo "$value"
