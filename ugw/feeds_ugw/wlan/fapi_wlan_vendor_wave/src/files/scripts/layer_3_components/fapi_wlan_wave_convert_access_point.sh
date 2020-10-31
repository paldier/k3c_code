#!/bin/sh

# Source fapi_wlan_wave_in.conf
. ${IN_CONF}

param_name=$1
object_index=$2
interface_name=$3
vendor_object_index=$4
[ -z "$vendor_object_index" ] && vendor_object_index=$object_index

value=""

case "$param_name" in
	"apEnable")
		eval enabled_value=\${Enable_${object_index}}
		Enable=$(printf "%b" "$enabled_value")
		if [ "$Enable" = "true" ]; then
			value="1"
		elif [ "$Enable" = "false" ]; then
			value="0"
		fi
		;;
	"AdvancedCoding")
		eval ldpc_value=\${LDPC_${vendor_object_index}}
		LDPC=$(printf "%b" "$ldpc_value")
		if [ "$LDPC" = "true" ]; then
			value=1
		elif [ "$LDPC" = "false" ]; then
			value=0
		fi
		;;
	"STBC")
		eval stbc_value=\${STBC_${vendor_object_index}}
		STBC=$(printf "%b" "$stbc_value")
		if [ "$STBC" = "true" ]; then
			value=1
		elif [ "$STBC" = "false" ]; then
			value=0
		fi
		;;
	"AclMode")
		eval acl_value=\${AclMode_${vendor_object_index}}
		mode=$(printf "%b" "$acl_value")
		case "$mode" in
			"Disabled")
				value=0
				;;
			"Allow")
				value=1
				;;
			"Deny")
				value=2
				;;
		esac
		;;
	"ACL")
		eval acl_macs_value=\${AclMACs_${vendor_object_index}}
		macs_list=$(printf "%b" "$acl_macs_value")
		# Replace commas with spaces
		value="${macs_list//,/ }"
		;;
	"UapsdEna")
		eval uapsd_value=\${UAPSDEnable_${object_index}}
		UAPSDEnable=$(printf "%b" "$uapsd_value")
		if [ "$UAPSDEnable" = "true" ]; then
			value="1"
		elif [ "$UAPSDEnable" = "false" ]; then
			value="0"
		fi
		;;
	"HiddenSSID")
		eval hidden_ssid_value=\${SSIDAdvertisementEnabled_${object_index}}
		SSIDAdvertisementEnabled=$(printf "%b" "$hidden_ssid_value")
		if [ "$SSIDAdvertisementEnabled" = "true" ]; then
			value="0"
		elif [ "$SSIDAdvertisementEnabled" = "false" ]; then
			value="1"
		fi
		;;
	"APforwarding")
		eval isolation_value=\${IsolationEnable_${object_index}}
		IsolationEnable=$(printf "%b" "$isolation_value")
		if [ "$IsolationEnable" = "true" ]; then
			value="0"
		elif [ "$IsolationEnable" = "false" ]; then
			value="1"
		fi
		;;
	"ReliableMulticast")
		eval multicast_value=\${ReliableMuticastEnable_${vendor_object_index}}
		ReliableMuticastEnable=$(printf "%b" "$multicast_value")
		if [ "$ReliableMuticastEnable" = "true" ]; then
			value="1"
		elif [ "$ReliableMuticastEnable" = "false" ]; then
			value="0"
		fi
		;;
	"AocsRestrictCh")
		#TODO ADD
		;;
	"NetworkMode")
		eval standards_value=\${OperatingStandards_${vendor_object_index}}
		OperatingStandards=$(printf "%b" "$standards_value")
		# Remove commas
		OperatingStandards=${OperatingStandards//,/}
		case "$OperatingStandards" in
			"a")
				value=10
				;;
			"an"|"na")
				value=14
				;;
			"b")
				value=17
				;;
			"g")
				value=18
				;;
			"bg"|"gb")
				value=19
				;;
			"gn"|"ng")
				value=22
				;;
			"bgn"|"bng"|"gbn"|"gnb"|"nbg"|"ngb")
				value=23
				;;
			"n")
				value=20
				# When 11n is set, read frequency from the driver
				frequency=`iwconfig $interface_name`
				frequency=${frequency##*Frequency=}
				frequency=${frequency%%.*}
				[ "$frequency" = "5" ] && value=12
				;;
		esac
		;;
	"vapStaLimits")
		# Current VAP STA Limit values are saved in a separate conf file
		# Parameters in the conf file are:
		# minSta
		# maxSta
		
		[ -e "${STA_LIMITS_CONF}_${interface_name}" ] && . ${STA_LIMITS_CONF}_${interface_name}
		eval reserved_sta_value=\${ReservedAssociatedDevices_${vendor_object_index}}
		eval max_sta_value=\${MaxAssociatedDevices_${object_index}}
		reserverd_sta=$(printf "%b" "$reserved_sta_value")
		max_sta=$(printf "%b" "$max_sta_value")

		# If a parameter is missing (not modified), use value from conf file
		# If both parameters are missing, return empty value.
		if [ -z "$reserverd_sta" ] && [ -n "$max_sta" ] ; then
			reserverd_sta=$minSta
		elif [ -z "$max_sta" ] && [ -n "$reserverd_sta" ]; then
			max_sta=$maxSta
		fi
		value="$reserverd_sta $max_sta"
		;;
	"PPAenabled")
		eval ppa_value=\${PPAEnable_${vendor_object_index}}
		PPAEnable=$(printf "%b" "$ppa_value")
		if [ "$PPAEnable" = "true" ]; then
			value="1"
		elif [ "$PPAEnable" = "false" ]; then
			value="0"
		fi
		;;
	"maxSta")
		eval max_sta_value=\${MaxAssociatedDevices_${object_index}}
		value=$(printf "%b" "$max_sta_value")
		;;
	"minSta")
		eval reserved_sta_value=\${ReservedAssociatedDevices_${vendor_object_index}}
		value=$(printf "%b" "$reserved_sta_value")
		;;
esac

echo "$value"
