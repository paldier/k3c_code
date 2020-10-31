#
# Openwrt Makefile for altair-LTE
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ###############################
PKG_NAME:=altair-lte
PKG_BASE_NAME:=altair-lte-npd
PKG_VERSION:=1.0.0.8
PKG_RELEASE:=1
PKG_MD5SUM:=d35e8de61e98d2b51e8e80900481e269
PKG_SOURCE:=$(PKG_BASE_NAME)-$(PKG_VERSION).tar.gz
PKG_SOURCE_URL:=http://localhost
PKG_BUILD_DIR:=$(KERNEL_BUILD_DIR)/$(PKG_BASE_NAME)-$(PKG_VERSION)

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk

define KernelPackage/$(PKG_NAME)
  SUBMENU:=Lantiq
  TITLE:=Altair LTE support driver
  KCONFIG:= \
	CONFIG_USB_NET_CDCETHER \
	CONFIG_USB_USBNET
  FILES:=\
	$(PKG_BUILD_DIR)/Host/Linux/Kernel/Usb/*.$(LINUX_KMOD_SUFFIX) \
	$(LINUX_DIR)/drivers/net/usb/cdc_ether.$(LINUX_KMOD_SUFFIX) \
	$(LINUX_DIR)/drivers/net/usb/usbnet.$(LINUX_KMOD_SUFFIX)
endef

define KernelPackage/$(PKG_NAME)/description
  Kernel module to support Altair LTE
endef

define Package/$(PKG_NAME)
  SECTION:=net
  CATEGORY:=Network
  TITLE:=Altair LTE support applications
  DEPENDS:= +kmod-$(PKG_NAME) +socat +libpthread
  MENU:=1
endef

define Package/$(PKG_NAME)/description
  This package contains Altair LTE support applications
endef

### Configurations depends on Menuconfig ########
MAKE_OPTS:= \
	CROSS_COMPILE="$(TARGET_CROSS)" \
	ARCH="$(LINUX_KARCH)" \
	SUBDIRS="$(PKG_BUILD_DIR)/Host/Linux/Kernel/Usb/" \
	EXTRA_CFLAGS="$(BUILDFLAGS)"

APP_PATH:=$(PKG_BUILD_DIR)/Host/Windows/Application/FwLoader
APP_SRC:=$(APP_PATH)/source
APP_INC:=$(APP_PATH)/include
FW_PATH:=$(PKG_BUILD_DIR)/Projects/LinuxEnv/bin

define Build/Compile
	$(MAKE) -C "$(LINUX_DIR)" \
		$(MAKE_OPTS) modules

	$(MAKE) -C $(APP_SRC) \
		$(TARGET_CONFIGURE_OPTS) CROSS_COMPILE=$(TARGET_CROSS) \
		CFLAGS="$(TARGET_CFLAGS) -I$(APP_INC)"
endef

define Package/$(PKG_NAME)/install
	$(INSTALL_DIR)  $(1)/opt/altair/bin
	$(INSTALL_BIN)  $(APP_SRC)/FwLoaderApp $(1)/opt/altair/bin/
	$(INSTALL_DATA) $(FW_PATH)/image.bin $(1)/opt/altair/bin/
	$(INSTALL_DIR)  $(1)/etc/hotplug.d/{usb,ueservice,net}
	$(if $(CONFIG_PACKAGE_libcellwan), \
	, \
		$(INSTALL_BIN)  ./files/ltq-wwan-usb-lte.hotplug $(1)/etc/hotplug.d/usb/; \
		$(INSTALL_BIN)  ./files/ltq-wwan-ueservice-lte.hotplug $(1)/etc/hotplug.d/ueservice/; \
		$(INSTALL_BIN)  ./files/ltq-wwan-usb-lte-stop.hotplug $(1)/etc/hotplug.d/net/; \
	)
endef

define Build/Clean
	$(if $(wildcard $(PKG_BUILD_DIR)),
		$(MAKE) -C "$(LINUX_DIR)" $(MAKE_OPTS) clean
		$(MAKE) -C $(APP_SRC) clean
		$(RM) -r $(PKG_BUILD_DIR)/ipkg-$(BOARD)
	)
endef

$(eval $(call KernelPackage,$(PKG_NAME)))
$(eval $(call BuildPackage,$(PKG_NAME)))
