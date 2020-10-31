#!/bin/bash
#

CUR_PATH=`readlink -f $(dirname $0)`
echo $CUR_PATH
TOP_DIR=$CUR_PATH/../../

error_print()
{
	echo "ERROR: $@"
	exit 1;
}

pkg_3rdparty="tuxera-ntfs tuxera_xRx500-ntfs eip97 eip123"
thirdparty()
{
	[ "$1" = "install" ] && {
		./scripts/feeds update thirdparty_sw
		for tpkg in $pkg_3rdparty
		do
			./scripts/feeds $1 -f $tpkg
		done 
	} || {
		rm -rf $CUR_PATH/package/feeds/thirdparty_sw
		rm -rf $CUR_PATH/feeds/thirdparty_sw*
	}
}

copy_dbtool_jar_file()
{
	echo $1
	#root path
	cd $1/ugw/feeds_ugw/framework/DBTool/src/dist/lib > /dev/null 2>&1
	wget http://central.maven.org/maven2/javax/xml/jsr173/1.0/jsr173-1.0.jar >/dev/null 2>&1
	wget http://www.mmbase.org/maven2/com/bea/xml/jsr173-ri/1.0/jsr173-ri-1.0.jar >/dev/null 2>&1 
#	wget http://central.maven.org/maven2/com/bea/xml/jsr173-ri/1.0/jsr173-ri-1.0.jari >/dev/null 2>&1
	wget http://central.maven.org/maven2/org/apache/logging/log4j/log4j-api/2.2/log4j-api-2.2.jar >/dev/null 2>&1
	wget http://central.maven.org/maven2/org/apache/logging/log4j/log4j-core/2.2/log4j-core-2.2.jar >/dev/null 2>&1
	wget http://central.maven.org/maven2/org/python/jython-standalone/2.7-b4/jython-standalone-2.7-b4.jar >/dev/null 2>&1
	wget http://central.maven.org/maven2/net/java/dev/stax-utils/stax-utils/20070216/stax-utils-20070216.jar >/dev/null 2>&1
	wget http://central.maven.org/maven2/org/swinglabs/swingx/swingx-all/1.6.5-1/swingx-all-1.6.5-1.jar >/dev/null 2>&1
	wget http://central.maven.org/maven2/xmlunit/xmlunit/1.5/xmlunit-1.5.jar >/dev/null 2>&1
	
	cd ../../frontend/lib >/dev/null
	cp ../../dist/lib/* . >/dev/null
	
	cd $1 >/dev/null
}

copy_system_anlyzer_jar_file()
{
	cd $1/ugw/feeds_ugw/debug/system-analyzer/src/systemanalyzer/lib/ > /dev/null 2>&1 
	wget http://central.maven.org/maven2/commons-codec/commons-codec/1.7/commons-codec-1.7.jar  >/dev/null 2>&1
	wget http://central.maven.org/maven2/net/lingala/zip4j/zip4j/1.3.2/zip4j-1.3.2.jar  >/dev/null 2>&1
	wget http://central.maven.org/maven2/junit/junit/4.10/junit-4.10.jar  >/dev/null 2>&1
	wget http://central.maven.org/maven2/org/controlsfx/controlsfx/8.20.8/controlsfx-8.20.8.jar  >/dev/null 2>&1
	wget http://central.maven.org/maven2/org/controlsfx/fxsampler/1.0.8/fxsampler-1.0.8.jar  >/dev/null 2>&1
	wget http://central.maven.org/maven2/org/controlsfx/openjfx-dialogs/1.0.2/openjfx-dialogs-1.0.2.jar  >/dev/null 2>&1
	wget http://central.maven.org/maven2/org/reactfx/reactfx/1.4.1/reactfx-1.4.1.jar  >/dev/null 2>&1
	cd $1 >/dev/null

}

environment_prepare()
{
	cd $TOP_DIR/
	[ -d ugw ] && exit

	[ -d target/ltq-sdk ] || {
		#setup patches link
		patches_path=$CUR_PATH/ugw/build/buildsystem_patches/core

		[ -L patches ] || {
			ln -s $patches_path patches
		}

		sed -e '/.*#+[btsfl].*/d' -e '/.*#+ugw-.*/d' -e '/.*ugw-temperory*/d' -e '/.*hgignore-*/d' -i patches/series


		quilt --version 2>/dev/null || error_print "Error: Missing 'quilt' tool. Please install 'quilt' in your PC to continue."
		quilt push -a
	}

	rsync -uavhi --inplace  $CUR_PATH/ugw $TOP_DIR > /dev/null
	rsync -uavhi --inplace  ugw/dl $TOP_DIR > /dev/null

	rm -rf target/linux/lantiq/
	cd target/linux/
	ln -sf ../../ugw/target/linux/lantiq
	#cp -aLr  ../../ugw/target/linux/x86/* x86/
	cd - > /dev/null 2>&1

	rm -f feeds.conf
	ln -sf ugw/feeds.conf feeds.conf
	echo "./ugw/config" > other_config_path
	ln -sf ugw/ugw_version ugw_version

	[ -d target/ltq-sdk ] || {
		rm -rf config
		mkdir config
		cp ugw/config/Config-* config/ -rf
	}

	#Download required jar files for DBTool re-compilation.
	#	copy_dbtool_jar_file $CUR_PATH

	#	copy_system_anlyzer_jar_file $CUR_PATH

	chmod +x ./scripts/*

	./scripts/feeds update -a
	./scripts/feeds install -a 2>/dev/null

	rm -rf ./package/feeds/thirdparty_sw/
	./scripts/feeds install -f eip97 2>/dev/null
	./scripts/feeds install -f eip123 2>/dev/null
	>./scripts/localmirrors.default

	echo -e "Environment Setup done...!"
	echo -e "Select model and compile ...!"
	#./scripts/ltq_change_environment.sh switch
}

[ "$1" = "-i" ] && {
	rm -rf tmp feeds package/feeds
        ./scripts/feeds update -a
        ./scripts/feeds install -a 2>/dev/null
	rm -rf ./package/feeds/thirdparty_sw/
	./scripts/feeds install -f eip97
	./scripts/feeds install -f eip123
        echo "Now please do './scripts/ltq_change_environment.sh switch' to select a model for build."
	exit
} || [ "$1" = "-h" ] && {
	echo -e "$0 -i <To re-install packages>"
	echo -e "$0 install/uninstall  <package name> <to install or uninstall packages>"
	echo -e "$0 thirdparty install/uninstall  <package name> <to install or uninstall packages>"
	exit
} || [ "$1" = "download" ] && {
	make package/download
	make tools/download
	echo -e "\n@@@ now start the compilation with  make -j24  @@@\n"
	exit
} || [ "$1" = "install" ] && {
	[ -n "$2" ] && {
		./scripts/feeds update thirdparty_sw
		./scripts/feeds $1 -f $2
		exit $?
	} || error_print "provide feed name to install"
} || [ "$1" = "uninstall" ] && {
	[ -n "$2" ] && {
		./scripts/feeds $1 $2
		exit $?
	} || error_print "provide feed name to uninstall"
} || [ "$1" = "thirdparty" ] && {
	[ "$2" = "install" ] && {
		thirdparty $2
	} || {
		thirdparty $2 
	}
	exit $?
} || [ -n "$1" ] && {
	error_print "invalid option execute -h option"
}

echo -e "@@@@@@@@ Setting up enviroment for compilation @@@@@@@@"  
environment_prepare 

