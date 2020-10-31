#!/bin/sh

# Source fapi_wlan_wave_in.conf
. ${IN_CONF}

param_name=$1
object_index=$2
interface_name=$3

value=""

case "$param_name" in
	"SecurityMode")
		eval mode_value=\${ModeEnabled_${object_index}}
		value=$(printf "%b" "$mode_value")
		;;
	"WEPKey")
		eval wep_key_value=\${WEPKey_${object_index}}
		value=$(printf "%b" "$wep_key_value")
		;;
	"KeyPassphrase")
		eval passphrase_value=\${KeyPassphrase_${object_index}}
		value=$(printf "%b" "$passphrase_value")
		;;
	"RekeyingInterval")
		eval rekey_value=\${RekeyingInterval_${object_index}}
		value=$(printf "%b" "$rekey_value")
		;;
	"RadiusServerIPAddr")
		eval radio_ip_value=\${RadiusServerIPAddr_${object_index}}
		value=$(printf "%b" "$radio_ip_value")
		;;
	"RadiusServerPort")
		eval radius_port_value=\${RadiusServerPort_${object_index}}
		value=$(printf "%b" "$radius_port_value")
		;;
	"RadiusSecret")
		eval radius_secret_value=\${RadiusSecret_${object_index}}
		value=$(printf "%b" "$radius_secret_value")
		;;
	"WepEncryption")
		eval mode_value=\${ModeEnabled_${object_index}}
		mode_value=$(printf "%b" "$mode_value")
		if [ "$mode_value" = "WEP-64" ] || [ "$mode_value" = "WEP-128" ]; then
			value=1
		fi
		;;
	"WepKeys_DefaultKey0")
		eval wep_key_value=\${WEPKey_${object_index}}
		value=$(printf "%b" "$wep_key_value")
		;;
	"WepKeys_DefaultKey1")
		eval wep_key_value=\${WEPKey_${object_index}}
		value=$(printf "%b" "$wep_key_value")
		;;
	"WepKeys_DefaultKey2")
		eval wep_key_value=\${WEPKey_${object_index}}
		value=$(printf "%b" "$wep_key_value")
		;;
	"WepKeys_DefaultKey3")
		eval wep_key_value=\${WEPKey_${object_index}}
		value=$(printf "%b" "$wep_key_value")
		;;
	"WepTxKeyIdx")
		eval mode_value=\${ModeEnabled_${object_index}}
		mode_value=$(printf "%b" "$mode_value")
		if [ "$mode_value" = "WEP-64" ] || [ "$mode_value" = "WEP-128" ]; then
			value=0
		fi
		;;
esac

echo "$value"
