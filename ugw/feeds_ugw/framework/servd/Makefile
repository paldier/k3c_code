#
# Openwrt Makefile for servd
#
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ############################### 
PKG_NAME:=servd
PKG_VERSION:=1.7.0.1
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)
PKG_DEFAULT_CUSTOM_SOURCE_DIR:=$(shell pwd)/src
PKG_BUILD_DEPENDS:=DBTool/host

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/ugw-loglevel.mk

define Package/$(PKG_NAME)
  SECTION:=utils
  CATEGORY:=Lantiq
  SUBMENU:=UGW Software
  TITLE:=Lantiq Service Daemon
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
  DEPENDS:=+libubus +libubox +libblobmsg-json +libugwhelper +libhelper +csd +libcal +libscapi
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	UGW Service Daemon
endef

### Menuconfig ##################################
define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_version)
$(call Package/$(PKG_NAME)/override_source_path)
$(call Package/$(PKG_NAME)/config_debug_level)
endef

#### Exports for Source Code ####################
#export $(PKG_CONFIG_DEPENDS)
#export TEST_LIBS=1

#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,$(CP) -L ./src/* $(PKG_BUILD_DIR)/)
endef

define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/include
	$(INSTALL_DIR) $(1)/xml
	$(if $(TEST_LIBS), \
		$(INSTALL_DATA) ./files/test_db_xml/*_data.xml $(1)/xml/; \
		$(INSTALL_DATA) ./files/test_db_xml/*_control.xml $(1)/xml/; \
	)
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/usr/sbin
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/servd/lib
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/servd/etc
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/servd $(1)/$(VENDOR_PATH)/usr/sbin/
	$(INSTALL_BIN) ./files/slloglevel $(1)/$(VENDOR_PATH)/usr/sbin/
	$(INSTALL_BIN) ./files/servd $(1)/etc/init.d/
	$(if $(TEST_LIBS), \
		$(INSTALL_BIN) $(PKG_BUILD_DIR)/test_libs/libservdsltest.so $(1)/$(VENDOR_PATH)/servd/lib/
	)
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
