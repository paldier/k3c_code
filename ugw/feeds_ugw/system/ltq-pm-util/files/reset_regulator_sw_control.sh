#!/bin/sh /etc/rc.common

STOP=01

PMUTILROOT="/opt/lantiq/bin"
bin_dir=/opt/lantiq/bin 

# disable software control of the DVS
stop ()
{
	if [ -n "$CONFIG_PACKAGE_KMOD_LANTIQ_DVS" ]; then
		if [ -f $PMUTILROOT/pm_util ]; then
			${bin_dir}/pm_util -xoff&
		fi
	fi
}

