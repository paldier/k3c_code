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

#### Package Info ###############################
PKG_NAME:=system_service_cli
PKG_VERSION:=1.0.0.1
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
  DEPENDS:= +system_fapi +libscapi
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	CLI for system/eth fapi
endef

### Menuconfig ##################################
define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_version)
$(call Package/$(PKG_NAME)/override_source_path)
$(call Package/$(PKG_NAME)/config_debug_level)
endef

TARGET_CFLAGS += -I$(STAGING_DIR)/usr/include/ppa_api/ \
		-I$(STAGING_DIR)/usr/include/ltq_switch_api/ \
		-I$(STAGING_DIR)/usr/include/ 
		
ifeq ($(CONFIG_TARGET_lantiq_xrx200),y)
   TARGET_CFLAGS +=-DPLATFORM_XRX200
   export PLATFORM_XRX200=1 
endif

ifeq ($(CONFIG_TARGET_lantiq_xrx500),y)
  TARGET_CFLAGS +=-DPLATFORM_XRX500
endif
		
ifeq ($(CONFIG_TARGET_x86_puma_puma7),y)
   TARGET_CFLAGS +=-DPLATFORM_XRX750
   export PLATFORM_XRX750=1 
endif

TARGET_LDFLAGS += -L$(STAGING_DIR)/usr/lib/ppa_api/ \
		-L$(STAGING_DIR)/usr/lib/switch_api/ \
		-L$(STAGING_DIR)/usr/lib/libscapi.so


#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,$(CP) -L ./src/* $(PKG_BUILD_DIR)/)
endef

# To ipkg / Rootfs
define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/opt/lantiq/usr/sbin
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/sys_cli $(1)/opt/lantiq/usr/sbin/
endef

define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
