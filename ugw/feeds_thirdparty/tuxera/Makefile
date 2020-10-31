#
# Openwrt feed for tuxera-ntfs
#

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=tuxera-file-systems
$(if $(CONFIG_PACKAGE_kmod-smvp),$(eval pkgSMP:=-smp))
PKG_VERSION:=3013.11.28-arx388-ugw-6.1$(pkgSMP)
PKG_RELEASE:=1

PKG_SOURCE:=$(PKG_NAME)-$(PKG_VERSION).tgz
PKG_SOURCE_URL:=https://tuxera.com/download/lantiq/
SMP_MD5SUM:=03776c701fe2009181961c1ed339a128
NOSMP_MD5SUM:=35938d7c56fd070b669457e354b52685
PKG_MD5SUM:=$(if $(pkgSMP),$(SMP_MD5SUM),$(NOSMP_MD5SUM))

TUXERA_FS:=ntfs fat
ntfs_tools:=mkntfs ntfsck ntfsdebug ntfsinfo ntfslabel
fat_tools:=dosfslabel fsck_msdos newfs_msdos

PKG_CONFIG_DEPENDS:=\
	$(foreach tfs,$(TUXERA_FS),CONFIG_PACKAGE_tuxera-$(tfs) \
	$(foreach tops,$($(tfs)_tools),CONFIG_PACKAGE_tuxera-$(tfs)_TOOL_$(shell echo $(tops)|tr a-z A-Z)))

include $(INCLUDE_DIR)/package.mk

# Download alternate package also. if smp is selected, then download nosmp tarball else wise-versa
define Download/tuxera$(if $(pkgSMP),,-smp)
  FILE:=$(if $(pkgSMP),$(subst -smp.tgz,.tgz,$(PKG_SOURCE)),$(subst .tgz,-smp.tgz,$(PKG_SOURCE)))
  URL:=$(PKG_SOURCE_URL)
  MD5SUM:=$(if $(pkgSMP),$(NOSMP_MD5SUM),$(SMP_MD5SUM))
endef
$(eval $(call Download,tuxera$(if $(pkgSMP),,-smp)))

define config_gen
  config PACKAGE_tuxera-$(2)_TOOL_$(shell echo $(1)|tr a-z A-Z)
	depends on PACKAGE_tuxera-$(2)
	bool '$(1)'

endef

define tuxeraPkg
define Package/tuxera-$(1)
  SECTION:=utils
  CATEGORY:=Utilities
  SUBMENU:=Filesystem
  TITLE:=Tuxera $(1) for Lantiq ARX3XX platforms
  URL:=http://www.tuxera.com
  CATEGORY:=Utilities
  DEPENDS:=+kmod-nls-utf8 +kmod-nls-cp437 @!TARGET_lantiq_xrx500 @!TARGET_lantiq_xrx500_4kec
  MENU:=1
endef

define Package/tuxera-$(1)/description
  This package contains pre-compiled binaries and tools of Tuxera $(1)
  tuned for Lantiq ARX3XX platforms.
  Supported tools: $(subst $(space),$(strip ,) ,$($(1)_tools))
endef

define Package/tuxera-$(1)/config
  $(foreach file,$($(1)_tools),$(call config_gen,$(file),$(1)))
endef

define Package/tuxera-$(1)/install
	$(INSTALL_DIR) $$(1)/lib/modules/$(LINUX_VERSION)
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/$(1)/kernel-module/*.ko $$(1)/lib/modules/$(LINUX_VERSION)/

	$(INSTALL_DIR) $$(1)/usr/sbin
	$(foreach tbin, $($(1)_tools), \
		$(if $(CONFIG_PACKAGE_tuxera-$(1)_TOOL_$(shell echo $(tbin)|tr a-z A-Z)), \
			$(INSTALL_BIN) $(PKG_BUILD_DIR)/$(1)/tools/$(tbin) $$(1)/usr/sbin/;) \
	)
endef
endef

define Build/Compile
	echo -n;
endef

$(foreach tfile,$(TUXERA_FS),$(eval $(call tuxeraPkg,$(tfile))))
$(foreach tfile,$(TUXERA_FS),$(eval $(call BuildPackage,tuxera-$(tfile))))
