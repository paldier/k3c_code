#******************************************************************************
#
#                         Copyright (c) 2015
#                        Lantiq Beteiligungs-GmbH & Co. KG
#
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#******************************************************************************

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ###############################
PKG_NAME:=libsysservices
PKG_VERSION:=1.0.1.3
PKG_RELEASE:=1
PKG_BUILD_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)
PKG_DEFAULT_CUSTOM_SOURCE_DIR:=$(shell pwd)/src

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/ugw-loglevel.mk

define Package/$(PKG_NAME)
  SECTION:=utils
  CATEGORY:=Lantiq
  SUBMENU:=UGW Software
  TITLE:=System Configuration FAPI
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
  DEPENDS:= +libugwhelper +libcal +csd +servd +libscapi +system_fapi
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	Service Library for system configuration
endef

### Menuconfig ##################################
define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_version)
$(call Package/$(PKG_NAME)/override_source_path)
$(call Package/$(PKG_NAME)/config_debug_level)
endef

ifeq ($(CONFIG_TARGET_lantiq_xrx200),y)
TARGET_CFLAGS +=-DPLATFORM_XRX200
endif

TARGET_CFLAGS += -I$(STAGING_DIR)/usr/include/ppa_api/ \
        -I$(STAGING_DIR)/usr/include/ltq_switch_api/ \
        -I$(STAGING_DIR)/usr/include/

ifeq ($(CONFIG_TARGET_lantiq_xrx500),y)
  TARGET_CFLAGS +=-DCONFIG_LTQ_PPA_HAL_SELECTOR -DCONFIG_LTQ_PPA_GRX500 -DCONFIG_LTQ_TARGET_GRX500 -DPLATFORM_XRX500
endif

#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,$(CP) -L ./src/* $(PKG_BUILD_DIR)/)
endef

# To Staging Dir
define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/lib/
	$(CP) $(PKG_BUILD_DIR)/*.so $(1)/usr/lib/
	$(if $(CONFIG_TARGET_lantiq_xrx200), \
		$(INSTALL_DIR) $(1)/xml; \
		$(INSTALL_DATA) ./files/db_xml/*.xml $(1)/xml/; \
	)
	$(if $(CONFIG_TARGET_lantiq_xrx200_EASY220W2), \
		$(INSTALL_DIR) $(1)/xml; \
		$(INSTALL_DATA) ./files/grx220/db_xml/*.xml $(1)/xml/; \
	)
endef

# To ipkg / Rootfs
define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/opt/lantiq/servd/lib
	$(INSTALL_DIR) $(1)/$(VENDOR_PATH)/www
	$(INSTALL_DIR) $(1)/opt/lantiq/xml
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/libsyssl.so $(1)/opt/lantiq/servd/lib/
	$(INSTALL_DATA) ./files/db_xml/*.xml $(1)/opt/lantiq/xml/
	$(if $(CONFIG_TARGET_lantiq_xrx200), \
		$(INSTALL_DATA) ./files/view_xml/*.xml $(1)/$(VENDOR_PATH)/www/; \
		$(INSTALL_DATA) ./files/db_xml/*.xml $(1)/opt/lantiq/xml/; \
	)
	$(if $(CONFIG_TARGET_lantiq_xrx200_EASY220W2), \
		$(INSTALL_DATA) ./files/grx220/view_xml/*.xml $(1)/$(VENDOR_PATH)/www/; \
		$(INSTALL_DATA) ./files/grx220/db_xml/*.xml $(1)/opt/lantiq/xml/; \
	)
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
