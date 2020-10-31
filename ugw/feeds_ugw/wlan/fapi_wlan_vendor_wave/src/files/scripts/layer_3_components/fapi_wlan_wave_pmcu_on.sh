#!/bin/sh
# Enable PMCU feature:

interface_name=$1

if [ -z "$interface_name" ]; then
	echo "ERROR: please provide wlan interface, exit"
	exit 1
fi

echo on > /sys/devices/system/cpu/cpu0/cpufreq/ltq_stats/ltq_control
cpufreq-set -g lantiqgov

iwpriv $interface_name sPCoCPower 1

