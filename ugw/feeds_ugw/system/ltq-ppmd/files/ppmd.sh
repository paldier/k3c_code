#!/bin/sh /etc/rc.common

START=79

bin_dir=/opt/lantiq/bin 
freq_dir="/sys/devices/system/cpu/cpu0/cpufreq"
ppmd_bin=ppmd 
i=0
s4=0

start() {
        #check how many scaling frequencies we have and set the thresholds accordingly
        work=$( cat $freq_dir/scaling_available_frequencies )
        f0=`echo $work | awk '{printf "%s", $1}'`
        f1=`echo $work | awk '{printf "%s", $2}'`
        f2=`echo $work | awk '{printf "%s", $3}'`
        f3=`echo $work | awk '{printf "%s", $4}'`
        if [ "$f1" -ne "$f0" ]; then
            i=`expr $i + 1`
        fi 
        if [ "$f0" -ne "$f2" ] && [ "$f1" -ne "$f2" ]; then
            i=`expr $i + 1`
        fi 
        if [ "$f0" -ne "$f3" ] && [ "$f2" -ne "$f3" ] && [ "$f1" -ne "$f3" ]; then
            i=`expr $i + 1`
        fi 


        if [ A"$CONFIG_LTQ_TEMP_EMER_SHUT_DOWN" = "A1" ]; then 
			s4=125000
			if [ -e ${bin_dir}/${ppmd_bin} ];then
				${bin_dir}/${ppmd_bin} -st0_0_0_$s4
			else
				echo "${bin_dir}/${ppmd_bin} not found"
				exit 1
			fi 
		fi
        if [ A"$CONFIG_LTQ_SCALE_DOWN_ON_HIGH_TEMP" = "A1" ]; then 
			if [ -e ${bin_dir}/${ppmd_bin} ];then
        			if [ "$i" -eq 1 ];then
        				${bin_dir}/${ppmd_bin} -st0_0_115000_$s4
                                elif [ "$i" -eq 2 ];then
        				${bin_dir}/${ppmd_bin} -st0_0_115000_$s4
                                elif [ "$i" -eq 3 ];then
        				${bin_dir}/${ppmd_bin} -st0_0_115000_$s4
        			fi
			else
				echo "${bin_dir}/${ppmd_bin} not found"
				exit 1
			fi 
	fi
}

stop() {
		if [ A"$CONFIG_LTQ_SCALE_DOWN_ON_HIGH_TEMP" = "A1" ] ||
			[ A"$CONFIG_LTQ_TEMP_EMER_SHUT_DOWN" = "A1" ]; then 
			if [ -e ${bin_dir}/${ppmd_bin} ];then
				echo "Thermal control disabled" 
				${bin_dir}/${ppmd_bin} -sp0
			else
				echo "${bin_dir}/${ppmd_bin} not found"
				exit 1
			fi 
		fi
}

