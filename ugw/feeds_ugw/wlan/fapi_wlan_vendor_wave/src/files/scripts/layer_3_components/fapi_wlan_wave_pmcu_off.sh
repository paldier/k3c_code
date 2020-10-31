#!/bin/sh
# Disable PMCU feature

interface_name=$1

if [ -z "$interface_name" ]; then
	echo "ERROR: please provide wlan interface, exit"
	exit 1
fi

iwpriv $interface_name sPCoCPower 0

cpufreq-set -g userspace
echo off > /sys/devices/system/cpu/cpu0/cpufreq/ltq_stats/ltq_control
