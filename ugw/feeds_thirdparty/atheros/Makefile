#
# Copyright (C) 2007 OpenWrt.org
#
# This is free software, licensed under the GNU General Public License v2.
# See /LICENSE for more information.
#
# $Id: Makefile 13904 2009-01-06 21:56:22Z jow $

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk
UGW_VER:=$(shell cat $(TOPDIR)/ugw_version)
PKG_NAME:=atheros
PKG_VERSION:=2.0
PKG_SOURCE_NAME:=lantiq-atheros
ifeq ($(CONFIG_TARGET_lantiq_xrx500),y)
ifeq ($(CONFIG_EVA_2GB),y)
  DRV_DIR:=grx500
else
  DRV_DIR:=grx500legacy
endif
endif
ifeq ($(CONFIG_TARGET_lantiq_xrx500),y)
   PKG_SOURCE_VERSION:=10.04.00.00.46
   DRV_PKG_NAME:=00000340
   BOARD_TYPE=lantiq-platform
   TMP_FILES := qca-wifi  
else
   ifeq ($(CONFIG_TARGET_lantiq_xrx330),y)
     PKG_SOURCE_VERSION:=10.04.242.00.40
     DRV_PKG_NAME:=00000320
     BOARD_TYPE=lantiq-platform
     TMP_FILES := qca-wifi  
   else
     PKG_SOURCE_VERSION:=10.02.85.00.3c
     DRV_PKG_NAME:=02150310
     BOARD_TYPE=lq-platform
     TMP_FILES := Makefile build drivers 
   endif
endif
DRV_PKG_VERSION:=00
KERNEL_PKG_NAME:=lantiq-wlan-atheros
TAR =tar 
RM  =rm

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk

ifdef CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR
  PKG_FULL_DIR:=$(CONFIG_atheros_CUSTOM_SOURCE_DIR)
endif

define Package/$(PKG_NAME)
	SECTION:=utils
	CATEGORY:=Lantiq
	TITLE:= Atheros scripts
	DEPENDS:= +kmod-$(KERNEL_PKG_NAME) \
		@+FEATURE_WIRELESS \
		@+FEATURE_WIRELESS_3P_ATH \
		+libpthread +librt
	URL:=http://www.lantiq.com
	MAINTAINER:=Lantiq Deutschland GMBH
	MENU:=1
endef

define Package/$(PKG_NAME)/description
 This package contains atheros scripts
endef


### Menuconfig #############################
define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_source_path)

  config PACKAGE_$(PKG_NAME)
	select PACKAGE_kmod-ltqcpe_dma_descr_atheros

  config FEATURE_ATHEROS_WLAN_TYPE_USB
	depends on PACKAGE_$(PKG_NAME)
	bool 'Atheros wlan type USB'
	help
	  'This feature loads Atheros wlan usb driver instead of pci(default) driver'
endef

define KernelPackage/$(KERNEL_PKG_NAME)
 SUBMENU:=Lantiq
 TITLE:=IFX Supoort for Atheros WLAN 
 KCONFIG:= \
         CONFIG_IFX_PCI=y \
         CONFIG_IFX_PCI_HW_SWAP=y \
         CONFIG_FW_LOADER=y \
         CONFIG_HOTPLUG=y \
         CONFIG_KMALLOC_HIGH_ATHEROS=y
endef

define KernelPackage/$(KERNEL_PKG_NAME)/description
  Kernel Support for Atheros WLAN 
endef

RSTRIP:=:

define Build/Prepare
	$(if $(PKG_FULL_DIR), 
	sh -c "cd $(BUILD_DIR) && if test -h "$(PKG_NAME)-$(PKG_VERSION)"; then rm -f "$(PKG_NAME)-$(PKG_VERSION)";fi"
	sh -c "cd $(BUILD_DIR) && ln -s $(PKG_FULL_DIR) $(PKG_NAME)-$(PKG_VERSION)" )
endef

define Build/Compile
	$(if $(PKG_FULL_DIR), 
	sh -c "cd $(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION) && $(RM) -rf $(TMP_FILES) && $(TAR) xjvf $(PKG_SOURCE_NAME)-$(PKG_SOURCE_VERSION)-$(DRV_PKG_VERSION)-$(DRV_PKG_NAME).bz2 && $(MAKE) BUILD_LANTIQ_TARGET=$(CONFIG_BUILD_SUFFIX) BUILD_DIR=$(BUILD_DIR) LINUX_DIR=$(LINUX_DIR) LINUX_VERSION=$(LINUX_VERSION) NEWFRAMEWORK=$(CONFIG_NEW_FRAMEWORK) TOOLCHAIN_DIR=$(TOOLCHAIN_DIR) TARGET_DIR=$(TARGET_DIR) KERNEL_BUILD_DIR=$(KERNEL_BUILD_DIR)" )
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/etc/rc.d
	$(INSTALL_DIR) $(1)/lib/modules/$(LINUX_VERSION)/net
	$(INSTALL_DIR) $(1)/sbin
	$(CP) ./files/ath_scripts/* $(1)/etc/rc.d/
ifndef CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR
	$(if $(CONFIG_TARGET_lantiq_xrx500), \
		$(CP) ./files/$(DRV_DIR)/* $(1); \
	)
endif
	$(if $(PKG_FULL_DIR), \
		$(RM) -rf $(PKG_FULL_DIR)/rootfs-$(BOARD_TYPE).optbuild/var; \
		$(CP) $(PKG_FULL_DIR)/rootfs-$(BOARD_TYPE).optbuild/* $(1)/; \
	)
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
$(eval $(call KernelPackage,$(KERNEL_PKG_NAME)))
