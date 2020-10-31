#
# Copyright (C) 2008 OpenWrt.org
#
# This is free software, licensed under the GNU General Public License v2.
# See /LICENSE for more information.
# 

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=switch_cli
ifneq "$(strip $(CONFIG_TARGET_lantiq_xrx500) $(CONFIG_TARGET_x86_puma))" ""
PKG_VERSION:=2.1.1
else
PKG_VERSION:=1.3.8
endif

XRX500_PKG_VERSION:=2.1.1
XRX3XX_PKG_VERSION:=1.3.8

PKG_RELEASE:=1
PKG_SOURCE:=$(PKG_NAME)-$(PKG_VERSION).tar.gz
PKG_SOURCE_URL:=http://localhost
PKG_CAT:=zcat
PKG_SOURCE_DIR:=$(BUILD_DIR)/$(PKG_NAME)-$(PKG_VERSION)
PKG_BUILD_DIR:=$(PKG_SOURCE_DIR)
#PKG_UNPACK += cp -f $(PKG_SOURCE_DIR)/include/ifx_ethsw.h $(LINUX_DIR)/include/asm/ifx/
IFX_BIN_DIR=opt/ifx/bin
#PKG_START_SCRIPT:=ifx_load_switch.sh

include $(INCLUDE_DIR)/package-version-override.mk
include $(INCLUDE_DIR)/package.mk

# Download alternate package also. if xrx500 is selected, then download non-xrx500 tarball else vice-versa
define Download/switch_cli$(if $(pkgName),,500)
  FILE:=$(if $(pkgName),$(subst $(PKG_VERSION).tar.gz,$(XRX3XX_PKG_VERSION).tar.gz,$(PKG_SOURCE)),$(subst $(PKG_VERSION).tar.gz,$(XRX500_PKG_VERSION).tar.gz,$(PKG_SOURCE)))
  URL:=$(PKG_SOURCE_URL)
endef
$(eval $(call Download,switch_cli$(if $(pkgName),,500)))


define Package/$(PKG_NAME)
  SECTION:=driver
  CATEGORY:=Lantiq
  TITLE:=Ethernet Switch New Command Line Utility
  URL:=http://www.lantiq.com/
  MAINTAINER:=Lantiq GmbH
  MENU:=1
endef

define Package/$(PKG_NAME)/description
	Advanced Ulitity for Ethernet Switch driver with ioctl support
endef

### Menuconfig ##################################
define Package/$(PKG_NAME)/config
        menu "$(PKG_NAME) features"
        depends on PACKAGE_$(PKG_NAME)
        source "$(SOURCE)/config/Config.in"
        endmenu
endef
CONFIG_SWITCH_CLI_SYSTEM:=
### Configurations depends on Menuconfig ########
ifeq ($(CONFIG_LTQ_ETHSW_API),y)
	CONFIG_SWITCH_CLI_SYSTEM += --enable-gswip
endif
ifeq ($(CONFIG_VR9),y)
	CONFIG_SWITCH_CLI_SYSTEM += --enable-gswip
endif
ifeq ($(CONFIG_AR10),y)
	CONFIG_SWITCH_CLI_SYSTEM += --enable-gswip
endif

ifeq ($(CONFIG_AR9),y)
	CONFIG_SWITCH_CLI_SYSTEM += --enable-tantos
endif

ifeq ($(CONFIG_DANUBE),y)
	CONFIG_SWITCH_CLI_SYSTEM += --enable-tantos
endif

ifeq ($(CONFIG_AMAZON_SE),y)
	CONFIG_SWITCH_CLI_SYSTEM += --enable-tantos
endif

ifeq ($(CONFIG_LTQ_ETHSW_CLI_HELP_PRINTOUT),y)
	CONFIG_SWITCH_CLI_SYSTEM += --enable-printhelp
endif

ifeq ($(CONFIG_TARGET_x86_puma),y)
	SWITCH_HEADERS_PATH=include/net/switch-api/
else
	SWITCH_HEADERS_PATH=arch/mips/include/asm/mach-lantiq/xway/switch-api/
endif

CONFIGURE_ARGS += --enable-kernelincl="$(LINUX_DIR)/include" \
	--with-cflags="-I$(LINUX_DIR)/$(SWITCH_HEADERS_PATH)" \
	$(CONFIG_SWITCH_CLI_SYSTEM)

define Build/InstallDev
	$(INSTALL_DIR) $(1)/usr/include/ltq_switch_api
	$(CP) $(LINUX_DIR)/$(SWITCH_HEADERS_PATH)/*.h $(1)/usr/include/ltq_switch_api/
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/usr/bin
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/src/switch_cli $(1)/usr/bin/
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
