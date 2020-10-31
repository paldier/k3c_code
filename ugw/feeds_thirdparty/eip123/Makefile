#
# Openwrt Makefile for eip97
#

#### Includes ###################################
include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

#### Package Info ###############################
PKG_NAME:=eip123
PKG_VERSION:=1.0.8
PKG_RELEASE:=1
#PKG_MD5SUM:=f8874369f74b5352ba9f4a708485644b
PKG_BASE_NAME:=ltq_eip123
PKG_SOURCE:=$(PKG_BASE_NAME)-$(PKG_VERSION).tar.gz
PKG_BUILD_DIR:=$(KERNEL_BUILD_DIR)/$(PKG_BASE_NAME)-$(PKG_VERSION)
PKG_SOURCE_URL:=http://localhost
PKG_START_SCRIPT:=load_eip123.sh
PKG_LIB_PATH:=$(PKG_BUILD_DIR)/ltq_eip123_user_library/Build/build_linux
PKG_MOD_PATH:=$(PKG_BUILD_DIR)/ltq_eip123_kernel_driver
PKG_APP_PATH:=$(PKG_BUILD_DIR)/linux_secure-boot_ddk
include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/package-version-override.mk

CONFIGURE_ARGS += --enable-cm=2 \
            		  --enable-polling \
            			--enable-target=versatile 


define KernelPackage/$(PKG_NAME)
  SUBMENU:=Lantiq
  TITLE:=EIP123 crypto driver
	DEPENDS:=@TARGET_lantiq_xrx500_4kec
  KCONFIG:= \
	CONFIG_CRYPTO=y \
	CONFIG_CRYPTO_HW=y \
	CONFIG_CRYPTO_DEV_LANTIQ_EIP123=y
endef

define KernelPackage/$(PKG_NAME)/description
  Kernel module to EIP-123 crypto library
endef

define Package/$(PKG_NAME)
  SECTION:=net
  CATEGORY:=Lantiq
  TITLE:=EIP 123 package
  DEPENDS:=+kmod-$(PKG_NAME) +libpthread +librt +!$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR:secure-app
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Technologies AG
endef

define Package/$(PKG_NAME)/description
	EIP-123 crypto library
endef

EXTRA_CFLAGS += -DRAM_CAPACITY=$(CONFIG_RAM_CAPACITY) -DPHYS_RAM_OFFSET=$(CONFIG_PHYS_RAM_OFFSET)

ifeq ($(CONFIG_BUILD_IA_ONLY),y)
EXTRA_CFLAGS += -DIA_STANDALONE
IA_STANDALONE=1
export IA_STANDALONE
else
ifeq ($(CONFIG_TEST_TSS),y)
TEST_TSS=1
export TEST_TSS
else
EXTRA_CFLAGS += -DSECURE_BUFFER
endif
endif

ifeq ($(CONFIG_DRM_NORMAL_FLOW),y)
EXTRA_CFLAGS += -DAES_OPT
endif

ifeq ($(CONFIG_DRM_FLUSH),y)
EXTRA_CFLAGS += -DTEST_FLUSH
endif

ifeq ($(CONFIG_UPDATE_OTP_ROLLBACK_ID),y)
EXTRA_CFLAGS += -DCONFIG_UPDATE_OTP_ROLLBACK_ID
endif

CONFIGURE_PATH=ltq_eip123_user_library/Build/build_linux

#### Target Rules ###############################
define Package/$(PKG_NAME)/config
$(call Package/$(PKG_NAME)/override_version)
$(call Package/$(PKG_NAME)/override_source_path)
  menu "eip123 feature"
    depends on PACKAGE_$(PKG_NAME)
    source "$(SOURCE)/config/Config.in"
  endmenu
endef

ifeq ($(CONFIG_$(PKG_NAME)_USE_CUSTOM_SOURCE_DIR),y)
define Build/Configure
	export CONFIGURE_PATH
	$(call Build/Configure/Default)
endef

define Build/Prepare
	$(call Build/Prepare/Default)
	(cd $(PKG_LIB_PATH); \
		./bootstrap.sh;)
	ln -sf $(PKG_MOD_PATH) $(LINUX_DIR)/drivers/crypto/lantiq_eip123/Sources
endef

define Build/Compile
	$(MAKE) -C $(PKG_LIB_PATH) all
	$(INSTALL_DIR) $(PKG_BUILD_DIR)/lib_inc
	$(MAKE) -C $(PKG_LIB_PATH) install DESTDIR=$(PKG_BUILD_DIR)/lib_inc
	$(MAKE_VARS) \
	$(MAKE) -C $(PKG_APP_PATH)/ \
                $(MAKE_FLAGS)
endef

define Package/$(PKG_NAME)/install
	# Startup script
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_BIN) ./files/$(PKG_START_SCRIPT) $(1)/etc/init.d/
	$(INSTALL_DIR) $(1)/usr/sbin
	# copying binaries
	$(CP) $(PKG_APP_PATH)/tss $(1)/usr/sbin/
ifeq ($(CONFIG_TEST_TSS),y)
	$(CP) $(PKG_APP_PATH)/test_tss $(1)/usr/sbin/
endif
	echo "Please do a make target/linux install after this"
endef

else

define Build/Prepare
	$(call Build/Prepare/Default)
	ln -sf $(PKG_MOD_PATH) $(LINUX_DIR)/drivers/crypto/lantiq_eip123/Sources
endef

define Build/Compile
endef

define Package/$(PKG_NAME)/install
	# Startup script
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_BIN) ./files/$(PKG_START_SCRIPT) $(1)/etc/init.d/
	echo "Please do a make target/linux install after this"
endef

endif


$(eval $(call BuildPackage,$(PKG_NAME)))
$(eval $(call KernelPackage,$(PKG_NAME)))
