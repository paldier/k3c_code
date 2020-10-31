#
# Openwrt Makefile for eip97
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ###############################
PKG_NAME:=eip97
PKG_VERSION:=1.2.24
PKG_RELEASE:=1
#PKG_MD5SUM:=f8874369f74b5352ba9f4a708485644b
PKG_BASE_NAME:=ltq_eip97
PKG_SOURCE:=$(PKG_BASE_NAME)_$(PKG_VERSION).tar.gz
PKG_BUILD_DIR:=$(KERNEL_BUILD_DIR)/$(PKG_BASE_NAME)_$(PKG_VERSION)
PKG_SOURCE_URL:=http://localhost
PKG_START_SCRIPT:=load_eip97.sh
TEST:=y
include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk

define KernelPackage/$(PKG_NAME)
  SUBMENU:=Lantiq
  TITLE:=EIP97 crypto driver
	DEPENDS:=@TARGET_lantiq_xrx500
  KCONFIG:= \
	CONFIG_CRYPTO_HW=y \
	CONFIG_CRYPTO_DEV_LANTIQ_EIP97=y \
	CONFIG_LTQ_CRYPTO=y \
	CONFIG_CRYPTO_CMAC=y \
	CONFIG_CRYPTO_AEAD=y \
	CONFIG_CRYPTO_HASH=y \
	CONFIG_CRYPTO_AUTHENC=y \
	CONFIG_CRYPTO_HMAC=y \
	CONFIG_CRYPTO_MD5=y \
	CONFIG_CRYPTO_SHA1=y \
	CONFIG_CRYPTO_SHA256=y \
	CONFIG_CRYPTO_SHA512=y 
endef

define KernelPackage/$(PKG_NAME)/description
  Kernel module to EIP-97 crypto library
endef

define KernelPackage/$(PKG_NAME)-TEST
  SUBMENU:=Lantiq
  TITLE:=EIP97 crypto driver
	DEPENDS:=@kmod-$(PKG_NAME)
  KCONFIG:= \
	CONFIG_LTQ_CRYPTO_TEST=m
endef

define KernelPackage/$(PKG_NAME)-TEST/description
  Kernel module to EIP-97 crypto library
endef

define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_version)
$(call Package/$(PKG_NAME)/override_source_path)
	menu "Test features"
		depends on PACKAGE_$(PKG_NAME)
		source "$(SOURCE)/config/Config"
	endmenu
endef

define Package/$(PKG_NAME)
  SECTION:=net
  CATEGORY:=Lantiq
  TITLE:=EIP 97 package
  DEPENDS:=+kmod-$(PKG_NAME)
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Technologies AG
endef

define Package/$(PKG_NAME)/description
	EIP-97 crypto library
endef

#### Target Rules ###############################
define Build/Prepare
	$(call Build/Prepare/Default)
	ln -sf $(PKG_BUILD_DIR) $(LINUX_DIR)/drivers/crypto/lantiq_eip97/Sources
endef

define Build/Compile
	echo "Nothing to do"
endef

define Package/$(PKG_NAME)/install
	# Startup script
	#	$(INSTALL_DIR) $(1)/etc/init.d
	#	$(INSTALL_BIN) ./files/$(PKG_START_SCRIPT) $(1)/etc/init.d/
	#copying the modules
	$(INSTALL_DIR) $(1)/lib/modules/$(LINUX_VERSION)	
	#$(INSTALL_BIN) $(LINUX_DIR)/drivers/crypto/lantiq_eip97/*.ko $(1)/lib/modules/$(LINUX_VERSION)/
endef

$(eval $(call BuildPackage,$(PKG_NAME)))
$(eval $(call KernelPackage,$(PKG_NAME)))
$(eval $(call KernelPackage,$(PKG_NAME)-TEST))
