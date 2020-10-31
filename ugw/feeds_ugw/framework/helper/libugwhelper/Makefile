#
# Openwrt Makefile for libugwhelper
#
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ############################### 
PKG_NAME:=libugwhelper
PKG_VERSION:=1.4.0.2
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)
PKG_DEFAULT_CUSTOM_SOURCE_DIR:=$(shell pwd)/src

#PKG_CONFIG_DEPENDS:=\
#	CONFIG_FULL_PACKAGE

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/ugw-loglevel.mk

define Package/$(PKG_NAME)
  SECTION:=utils
  CATEGORY:=Lantiq
  SUBMENU:=UGW Software
  TITLE:=Lantiq UGW SDK Helper library
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
  DEPENDS:=+libc +librt +libpthread +libjson +libubox +libubus +libblobmsg-json +libscapi +libhelper 
  MENU:=1
endef


define Package/$(PKG_NAME)/description
	Lantiq UGW SDK Helper Library to define function,prototypes,structs,enums which are
	used across the system. 
endef

### Menuconfig ##################################
define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_version)
$(call Package/$(PKG_NAME)/override_source_path)
$(call Package/$(PKG_NAME)/config_debug_level)
$(call config_add_global_debug_level)
	menu "$(PKG_NAME) features"
	depends on PACKAGE_$(PKG_NAME)
        source "$(SOURCE)/config/Config.in"
        endmenu
endef

TARGET_CFLAGS += $(if $(CONFIG_libugwhelper_MEM_DEBUG),-DMEM_DEBUG)

#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,$(CP) -L ./src/* $(PKG_BUILD_DIR)/)
endef

define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/lib
	$(INSTALL_DIR) $(1)/usr/sbin
	$(INSTALL_DIR) $(1)/usr/include
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/libugwhelper.so $(1)/usr/lib/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/helper_test $(1)/usr/sbin/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/*.h $(1)/usr/include/
	$(INSTALL_DIR) $(1)/usr/docs
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/ugw_common.h $(1)/usr/docs/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/ugw_debug.h $(1)/usr/docs/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/ugw_defs.h $(1)/usr/docs/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/ugw_enums.h $(1)/usr/docs/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/ugw_proto.h $(1)/usr/docs/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/include/ugw_structs.h $(1)/usr/docs/
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/usr/lib
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/usr/sbin
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/etc/
	$(INSTALL_DIR) $(1)/etc/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/libugwhelper.so $(1)/$(VENDOR_PATH)/usr/lib/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/helper_test $(1)/$(VENDOR_PATH)/usr/sbin/
	$(INSTALL_BIN) ./files/syslog-ng.conf $(1)/$(VENDOR_PATH)/etc/
	./files/gen_notify_defs.sh $(PKG_BUILD_DIR)/include/ugw_notify_defs.h $(1)/$(VENDOR_PATH)/etc/ugw_notify_defs.sh
	./files/gen_notify_defs.sh $(PKG_BUILD_DIR)/include/ugw_notify_defs.h $(1)/etc/ugw_notify_defs.sh
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
