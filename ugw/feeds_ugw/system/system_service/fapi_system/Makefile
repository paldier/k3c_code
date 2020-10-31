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
#include $(INCLUDE_DIR)/package.mk

#### Package Info ###############################
PKG_NAME:=system_fapi
PKG_VERSION:=1.1.0.1
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
  DEPENDS:= +libscapi +PACKAGE_switch_cli:switch_cli
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	FAPI for system configuration
endef

#### Exports ####################################
export LINUX_KARCH TARGET_CONFIGURE_OPTS LINUX_DIR PKG_BUILD_DIR

### Menuconfig ##################################
define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_version)
$(call Package/$(PKG_NAME)/override_source_path)
$(call Package/$(PKG_NAME)/config_debug_level)
endef

TARGET_CFLAGS += -I$(STAGING_DIR)/usr/include/ppa_api/ \
		-I$(STAGING_DIR)/usr/include/ltq_switch_api/ \
		-I$(STAGING_DIR)/usr/include/ \
		-DCONFIG_LTQ_PPA_IPv6_ENABLE \
		-DCONFIG_LTQ_PPA_QOS \
		-DCONFIG_LTQ_PPA_QOS_WFQ \
		-DCONFIG_LTQ_PPA_QOS_RATE_SHAPING \
		-DCONFIG_LTQ_PPA_HAL_SELECTOR \
		-DCONFIG_LTQ_PPA_API_SW_FASTPATH

ifeq ($(CONFIG_PACKAGE_kmod-lantiq-swaccel),y)
  TARGET_CFLAGS += -DCONFIG_LTQ_PPA_API_SW_FASTPATH
endif

ifeq ($(CONFIG_PACKAGE_kmod-toe_drv),y)
  TARGET_CFLAGS += -DCONFIG_INTEL_TRAFFIC_OFFLOAD_ENGINE
endif

ifeq ($(CONFIG_PACKAGE_switch_cli),y)
  TARGET_CFLAGS +=-DCONFIG_LANTIQ_SWITCH
endif

ifeq ($(CONFIG_TARGET_lantiq_xrx200),y)
   TARGET_CFLAGS +=-DPLATFORM_XRX200
   export PLATFORM_XRX200=1 
endif

ifeq ($(CONFIG_TARGET_lantiq_xrx330),y)
        TARGET_CFLAGS +=-DPLATFORM_XRX330
endif
ifeq ($(CONFIG_TARGET_lantiq_xrx300),y)
        TARGET_CFLAGS +=-DPLATFORM_XRX300
endif

ifeq ($(strip $(CONFIG_TARGET_lantiq_xrx200_EASY220W2)),y)
        TARGET_CFLAGS +=-DPLATFORM_XRX200_EASY220W2
endif

ifeq ($(CONFIG_TARGET_lantiq_xrx500),y)
  TARGET_CFLAGS +=-DCONFIG_LTQ_PPA_HAL_SELECTOR -DCONFIG_LTQ_PPA_GRX500 -DCONFIG_LTQ_TARGET_GRX500 -DPLATFORM_XRX500
   export PLATFORM_XRX500=1 
endif
		
ifeq ($(CONFIG_TARGET_x86_puma),y)
  TARGET_CFLAGS += -DPLATFORM_XRX750
  export PLATFORM_XRX750=1
endif

ifeq ($(CONFIG_PACKAGE_kmod-lan_port_separation),y)
TARGET_CFLAGS +=-DENABLE_LAN_PORT_SEPARATION
endif

ifeq ($(CONFIG_PACKAGE_kmod-gphy_event),y)
TARGET_CFLAGS +=-DENABLE_LAN_PORT_LINK_EVENT
endif

ifeq ($(CONFIG_PACKAGE_ltq_vrx320_datapath_drv),y)
  TARGET_CFLAGS +=-DCONFIG_VRX320_SUPPORT
endif
	
ifeq ($(CONFIG_VRX320_PTM_VECTORING),y)
  TARGET_CFLAGS +=-DCONFIG_VRX320_PTM_VECTORING_SUPPORT
endif
	
ifeq ($(CONFIG_FEATURE_SESSION_LIMIT),y) 
  TARGET_CFLAGS +=-DCONFIG_LTQ_PPA_HANDLE_CONNTRACK_SESSIONS 
endif

ifeq ($(CONFIG_PACKAGE_kmod-pp_crypto_drv),y)
	EXTRA_CFLAGS += -DCONFIG_IPSEC_SUPPORT
endif

TARGET_LDFLAGS += -L$(STAGING_DIR)/usr/lib/ppa_api/ \
		-L$(STAGING_DIR)/usr/lib/switch_api/ \
		-L$(STAGING_DIR)/usr/lib/libscapi.so

#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	$(if $(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),,$(CP) -L ./src/* $(PKG_BUILD_DIR)/)
endef

# To Staging Dir
define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/include/
	$(INSTALL_DIR) $(1)/usr/include/net/
	$(INSTALL_DIR) $(1)/usr/lib/
	$(INSTALL_DIR) $(1)/usr/docs/
ifeq ($(CONFIG_TARGET_lantiq_xrx200),y)
endif
ifeq ($(CONFIG_TARGET_lantiq_xrx500),y)
endif
	$(CP) $(PKG_BUILD_DIR)/*.so $(1)/usr/lib/
endef

# To ipkg / Rootfs
define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/opt/lantiq/usr/lib
ifeq ($(CONFIG_TARGET_lantiq_xrx200),y)
	$(INSTALL_DIR) $(1)/etc/init.d/
endif
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/libsysfapi.so $(1)/opt/lantiq/usr/lib/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/libethfapi.so $(1)/opt/lantiq/usr/lib/
ifeq ($(CONFIG_TARGET_lantiq_xrx200),y)
	$(CP) ./files/* $(1)/etc/init.d/
endif
endef


define Build/Clean
	if [ -d $(PKG_BUILD_DIR) ]; then $(MAKE) -C $(PKG_BUILD_DIR) clean; \
	$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD); fi
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
