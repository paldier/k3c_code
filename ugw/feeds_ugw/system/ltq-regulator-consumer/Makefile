#
# Copyright (C) 2008 OpenWrt.org
# Copyright (C) 2014 lantiq.com 
#
# This is free software, licensed under the GNU General Public License v2.
# See /LICENSE for more information.
#

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=ltq-regulator-consumer
PKG_VERSION:=1.0.1
PKG_BASE_NAME:=ltq_regulator_consumer
PKG_SOURCE:=$(PKG_BASE_NAME)-$(PKG_VERSION).tar.gz
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_BASE_NAME)-$(PKG_VERSION)
PKG_RELEASE:=1
PKG_SOURCE_URL:=http://localhost
PKG_BIN_DIR=lib/modules/$(LINUX_VERSION)/
PKG_BUILD_DEPENDS:=
PKG_INSTALL:=1
PKG_BUILD_PARALLEL:=1 

PKG_FIXUP:=autoreconf
PKG_START_SCRIPT:=ltq_regulator_consumer.sh
PKG_MD5SUM:=2f52173592b76a8ecb706b03207124fd

include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/package.mk

define Package/$(PKG_NAME)
  SECTION:=power
  CATEGORY:=Lantiq
  TITLE:=DCDC Power Regulator Consumer
  URL:=http://www.lantiq.com/
  MAINTAINER:=Lantiq Deutschland GmbH
  DEPENDS:=+ltq-regulator
  MENU:=1 
  SUBMENU:= 
endef

define Package/$(PKG_NAME)/description
	DCDC Power Regulator Consumer.
	Provide Power Regulator Consumer Interface
endef

define Package/$(PKG_NAME)/config 
	menu "configuration" 
	depends on PACKAGE_$(PKG_NAME) 
		source "$(SOURCE)/config/Config.in" 
	endmenu 
	$(call Package/$(PKG_NAME)/override_version) 
	$(call Package/$(PKG_NAME)/override_source_path) 
endef 
 
CONFIGURE_ARGS += \
	--enable-kernelbuild="$(LINUX_DIR)" ARCH=$(LINUX_KARCH)

ifeq ($(CONFIG_LTQ_REGULATOR_CONSUMER_DEBUG),y)
CONFIGURE_ARGS += --enable-debug
endif
	
define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/{lib,include} 
	$(CP) $(PKG_BUILD_DIR)/src/include/ltq_regulator_consumer_interface.h $(1)/usr/include/
endef 

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_BIN) ./files/$(PKG_START_SCRIPT) $(1)/etc/init.d/
	$(INSTALL_DIR) $(1)/$(PKG_BIN_DIR)
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/src/ltq_regulator_consumer.ko $(1)/$(PKG_BIN_DIR)
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
