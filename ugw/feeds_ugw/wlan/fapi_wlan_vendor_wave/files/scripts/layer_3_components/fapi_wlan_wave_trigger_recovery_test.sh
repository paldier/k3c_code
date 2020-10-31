#!/bin/sh

# Source library script
[ ! "$FAPI_WLAN_WAVE_INIT_PLATFORM" ] && . /tmp/wlan_wave/fapi_wlan_wave_init_platform.sh

interface_name=$1
time=$2
iteration=$3
assert_type=$4
# increment iteration by 1 in order to get into the loop
[ "$iteration" = "0" ] && iteration=$((iteration+1))

# assert_type may be missing in script call:
[ -z $assert_type ] && assert_type=0

print2log DBG "fapi_wlan_wave_trigger_recovery_test.sh: interface=$interface_name, assert_type=$assert_type"

while [ "$iteration" -gt "0" ]; do
   sleep $time
   iwpriv $interface_name sDoFwDebug 1 $assert_type
   iteration=$((iteration-1))
done 
