#
# Copyright (C) 2008 OpenWrt.org
# Copyright (C) 2014 lantiq.com 
#
# This is free software, licensed under the GNU General Public License v2.
# See /LICENSE for more information.
#

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=ltq-pm-util
PKG_VERSION:=1.4.4
PKG_BASE_NAME:=ltq_pm_util
PKG_SOURCE:=$(PKG_BASE_NAME)-$(PKG_VERSION).tar.gz
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_BASE_NAME)-$(PKG_VERSION)
PKG_RELEASE:=1
PKG_SOURCE_URL:=http://localhost
PKG_BIN_DIR=opt/lantiq/bin
PKG_BUILD_DEPENDS:=
PKG_INSTALL:=1
PKG_BUILD_PARALLEL:=1 

PKG_FIXUP:=autoreconf

PKG_START_SCRIPT:=pm_util.sh
PKG_STOP_SCRIPT:=reset_regulator_sw_control.sh
PKG_MD5SUM:=e559c67281625997d6db5038c2c1e598

include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/package.mk

define Package/$(PKG_NAME)
  SECTION:=power
  CATEGORY:=Lantiq
  TITLE:=Power Management Utility
  URL:=http://www.lantiq.com/
  MAINTAINER:=Lantiq Deutschland GmbH
  DEPENDS:=+ltq-pmcu
  MENU:=1
  SUBMENU:= 
endef

define Package/$(PKG_NAME)/description
   Power Management Utility for XRX200 and XRX300 platforms.
   This application offers access to system power info's.
   Features: 
      - Display current power state of all registered modules
      - Get module specific power state
      - Change/Set power state of a specific module
   Example: pm_util -h    "print help menu"
endef

define Package/$(PKG_NAME)/config 
	menu "configuration" 
	depends on PACKAGE_$(PKG_NAME) 
		source "$(SOURCE)/config/Config.in" 
	endmenu 
	$(call Package/$(PKG_NAME)/override_version) 
	$(call Package/$(PKG_NAME)/override_source_path) 
endef 
 
# Add user specified CFLAGS and configure options
ifneq ($(CONFIG_LTQ_PM_UTIL_USER_FLAGS),"")
PM_UTIL_USER_CFLAGS=$(strip $(subst ",, $(CONFIG_LTQ_PM_UTIL_USER_FLAGS)))
CONFIGURE_ARGS += --with-cflags="$(PM_UTIL_USER_CFLAGS)"
endif 
 
CONFIGURE_ARGS += \
	--enable-kernelbuild="$(LINUX_DIR)" ARCH=$(LINUX_KARCH)

define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/{lib,include} 
	$(CP) $(PKG_BUILD_DIR)/src/include/ltq_pmutil_interface.h $(1)/usr/include/
	$(INSTALL_DIR) $(1)/usr/{lib,include} 
	$(CP) $(PKG_BUILD_DIR)/src/libpmutil.a $(1)/usr/lib/
endef 
 
 
define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_BIN) ./files/$(PKG_START_SCRIPT) $(1)/etc/init.d/
	$(INSTALL_BIN) ./files/$(PKG_STOP_SCRIPT) $(1)/etc/init.d/
	$(INSTALL_DIR) $(1)/etc/profile.d
	$(INSTALL_BIN) ./files/ltq_pm.sh $(1)/etc/profile.d/
	$(INSTALL_DIR) $(1)/$(PKG_BIN_DIR)
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/src/pm_util $(1)/opt/lantiq/bin/
endef


define Build/Configure
	# remove "configured" stamps of other configs
	@- rm $(PKG_BUILD_DIR)/.configured*
	$(call Build/Configure/Default)
endef

define Build/Clean
	@- rm -Rf $(PKG_BUILD_DIR)/ipkg*
	@- [ -e $(PKG_BUILD_DIR)/Makefile ] && $(MAKE) -C $(PKG_BUILD_DIR) clean
	$(call Build/Clean/Default)
endef

define Build/lint
    make -C $(PKG_BUILD_DIR) lint
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
