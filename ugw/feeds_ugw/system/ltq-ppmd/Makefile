#
# Copyright (C) 2008 OpenWrt.org
# Copyright (C) 2014 lantiq.com 
#
# This is free software, licensed under the GNU General Public License v2.
# See /LICENSE for more information.
#

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=ltq-ppmd
PKG_VERSION:=1.2.2
PKG_BASE_NAME:=ltq_ppmd
PKG_SOURCE:=$(PKG_BASE_NAME)-$(PKG_VERSION).tar.gz
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_BASE_NAME)-$(PKG_VERSION)
PKG_RELEASE:=1
PKG_SOURCE_URL:=http://localhost
PKG_BIN_DIR=opt/lantiq/bin
PKG_BUILD_DEPENDS:=+ltq-pm-util
PKG_INSTALL:=1
PKG_BUILD_PARALLEL:=1 

PKG_FIXUP:=autoreconf

PKG_START_SCRIPT:=ppmd.sh
PKG_MD5SUM:=b60c1e98d10aad72e3f20e49de6776db

include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/package.mk

define Package/$(PKG_NAME)
  SECTION:=power
  CATEGORY:=Lantiq
  TITLE:=Power Policy Management Daemon
  URL:=http://www.lantiq.com/
  MAINTAINER:=Lantiq Deutschland GmbH
  DEPENDS:=+ltq-pmcu +ltq-pm-util
  MENU:=1
  SUBMENU:= 
endef

define Package/$(PKG_NAME)/description
   The PPMD (Power Policy Management Daemon) takes care about thermal limits
   based on the temperature sensor values and trigger actions to bring the
   system back into a normal temperature range.
   Example: ppmd -h    "print help menu"
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
ifneq ($(CONFIG_LTQ_PPMD_USER_FLAGS),"")
PPMD_USER_CFLAGS=$(strip $(subst ",, $(CONFIG_LTQ_PPMD_USER_FLAGS)))
CONFIGURE_ARGS += $(PPMD_USER_CFLAGS)
endif 
 
CONFIGURE_ARGS += \
	--enable-kernelbuild="$(LINUX_DIR)" ARCH=$(LINUX_KARCH) \
	--enable-pmutil-library="-L$(STAGING_DIR)/usr/lib" \
	--enable-pmutil-include="-L$(STAGING_DIR)/usr/include"

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_BIN) ./files/$(PKG_START_SCRIPT) $(1)/etc/init.d/
	$(INSTALL_DIR) $(1)/$(PKG_BIN_DIR)
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/src/ppmd $(1)/opt/lantiq/bin/
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
