#!/bin/sh

# Source fapi_wlan_wave_in.conf
. ${IN_CONF}

param_name=$1
object_index=$2
interface_name=$3

value=""

eval access_category_value=\${AccessCategory_${object_index}}
AccessCategory=$(printf "%b" "$access_category_value")

case "$param_name" in
	"AC_BK_CWmin")
		[ "$AccessCategory" = "BK" ] && eval cwmin_value=\${ECWMin_${object_index}} && value=$(printf "%b" "$cwmin_value")
		;;
	"AC_BK_CWmax")
		[ "$AccessCategory" = "BK" ] && eval cwmax_value=\${ECWMax_${object_index}} && value=$(printf "%b" "$cwmax_value")
		;;
	"AC_BK_AIFSN")
		[ "$AccessCategory" = "BK" ] && eval aifsn_value=\${AIFSN_${object_index}} && value=$(printf "%b" "$aifsn_value")
		;;
	"AC_BK_TXOP")
		[ "$AccessCategory" = "BK" ] && eval txop_value=\${TxOpMax_${object_index}} && value=$(printf "%b" "$txop_value")
		;;
	"AC_BE_CWmin")
		[ "$AccessCategory" = "BE" ] && eval cwmin_value=\${ECWMin_${object_index}} && value=$(printf "%b" "$cwmin_value")
		;;
	"AC_BE_CWmax")
		[ "$AccessCategory" = "BE" ] && eval cwmax_value=\${ECWMax_${object_index}} && value=$(printf "%b" "$cwmax_value")
		;;
	"AC_BE_AIFSN")
		[ "$AccessCategory" = "BE" ] && eval aifsn_value=\${AIFSN_${object_index}} && value=$(printf "%b" "$aifsn_value")
		;;
	"AC_BE_TXOP")
		[ "$AccessCategory" = "BE" ] && eval txop_value=\${TxOpMax_${object_index}} && value=$(printf "%b" "$txop_value")
		;;
	"AC_VI_CWmin")
		[ "$AccessCategory" = "VI" ] && eval cwmin_value=\${ECWMin_${object_index}} && value=$(printf "%b" "$cwmin_value")
		;;
	"AC_VI_CWmax")
		[ "$AccessCategory" = "VI" ] && eval cwmax_value=\${ECWMax_${object_index}} && value=$(printf "%b" "$cwmax_value")
		;;
	"AC_VI_AIFSN")
		[ "$AccessCategory" = "VI" ] && eval aifsn_value=\${AIFSN_${object_index}} && value=$(printf "%b" "$aifsn_value")
		;;
	"AC_VI_TXOP")
		[ "$AccessCategory" = "VI" ] && eval txop_value=\${TxOpMax_${object_index}} && value=$(printf "%b" "$txop_value")
		;;
	"AC_VO_CWmin")
		[ "$AccessCategory" = "VO" ] && eval cwmin_value=\${ECWMin_${object_index}} && value=$(printf "%b" "$cwmin_value")
		;;
	"AC_VO_CWmax")
		[ "$AccessCategory" = "VO" ] && eval cwmax_value=\${ECWMax_${object_index}} && value=$(printf "%b" "$cwmax_value")
		;;
	"AC_VO_AIFSN")
		[ "$AccessCategory" = "VO" ] && eval aifsn_value=\${AIFSN_${object_index}} && value=$(printf "%b" "$aifsn_value")
		;;
	"AC_VO_TXOP")
		[ "$AccessCategory" = "VO" ] && eval txop_value=\${TxOpMax_${object_index}} && value=$(printf "%b" "$txop_value")
		;;
esac

echo "$value"
