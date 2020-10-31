#
# Openwrt Makefile for Ralink WLAN
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ###############################
PKG_NAME:=ralink
PKG_VERSION:=v2715_DPA_20121207
PKG_RELEASE:=1
#PKG_MD5SUM:=f8874369f74b5352ba9f4a708485644b
PKG_BASE_NAME:=RT5592AP_wifi
PKG_SOURCE:=$(PKG_BASE_NAME)_$(PKG_VERSION).tar.bz2
PKG_BUILD_DIR:=$(KERNEL_BUILD_DIR)/$(PKG_BASE_NAME)_$(PKG_VERSION)
PKG_SOURCE_URL:=http://localhost

include $(INCLUDE_DIR)/package.mk

define Package/$(PKG_NAME)
  SECTION:=net
  CATEGORY:=Lantiq
  TITLE:=Ralink Wireless Module RT5592AP
  URL:=http://www.ralink.com
  MAINTAINER:=Lantiq Technologies AG
endef

define Package/$(PKG_NAME)/description
	Ralink Wireless Module RT5592AP
endef

MAKE_OPTS:= \
	ARCH="$(LINUX_KARCH)" \
	CROSS_COMPILE="$(TARGET_CROSS)" \
	$(EXTRA_KCONFIG) \
	GCC_HONOUR_COPTS=s

RALINK_PKG_OPTS:= \
	TARGET=LINUX \
	CHIPSET="5592 5390 3593 3090" \
	LINUX_SRC="$(LINUX_DIR)" \
	LINUX_SRC_MODULE="$(LINUX_DIR)/drivers/net/wireless/" \
	MODULE="5592" \
	OSABL="NO" \
	PLATFORM="INF_VR9" \
	RELEASE="DPA" \
	RT28xx_DIR=$(PKG_BUILD_DIR) \
	RT28xx_MODE="AP" \
	RTMP_SRC_DIR="$(PKG_BUILD_DIR)/RT5592"

#### Target Rules ###############################
define Build/Prepare
$(call Build/Prepare/Default)
	$(CP) $(PKG_BUILD_DIR)/os/linux/Makefile.6 $(PKG_BUILD_DIR)/os/linux/Makefile
	sed -i 's/findstring 2.4/findstring 24_bug/g' $(PKG_BUILD_DIR)/os/linux/config.mk
endef

define Build/Compile
	$(MAKE) -C $(LINUX_DIR) SUBDIRS=$(PKG_BUILD_DIR)/os/linux modules \
		$(MAKE_OPTS) \
		$(RALINK_PKG_OPTS)
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/lib/modules/$(LINUX_VERSION)
	$(INSTALL_DIR) $(1)/etc/rc.d/
	$(CP) $(PKG_BUILD_DIR)/os/linux/rt5592ap.ko $(1)/lib/modules/$(LINUX_VERSION)/
	$(INSTALL_DIR) $(1)/etc/Wireless/RT2860AP/
	$(INSTALL_DIR) $(1)/ramdisk/etc/Wireless/RT2860AP/
	$(CP) $(PKG_BUILD_DIR)/RT2860AP.dat $(1)/etc/Wireless/RT2860AP/
	$(CP) $(PKG_BUILD_DIR)/RT2860AP.dat $(1)/ramdisk/etc/Wireless/RT2860AP/
	$(CP) ./files/scripts/* $(1)/etc/rc.d/
endef


$(eval $(call BuildPackage,$(PKG_NAME)))
