#
# DB Tool Host Make.
# This provides XML tools for UGW SDK under staging_dir/host/bin/
#

include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk
include $(INCLUDE_DIR)/version.mk

PKG_NAME:=dbtool
PKG_VERSION:=1.0.0.5
PKG_RELEASE:=1
PKG_BUILD_DEPENDS:=$(PKG_NAME)/host

include $(INCLUDE_DIR)/package.mk
include $(INCLUDE_DIR)/host-build.mk

define Package/$(PKG_NAME)
  SECTION:=utils
  CATEGORY:=Lantiq
  SUBMENU:=UGW Software
  TITLE:=XML DB Tool Host Utilities
  URL:=http://www.lantiq.com
  MAINTAINER:=Lantiq Beteiligungs-GmbH & Co. KG
endef

define Package/$(PKG_NAME)/description
	UGW Service Daemon
endef

define Host/Prepare
	$(CP) -L ./src/* $(HOST_BUILD_DIR)/
endef

define Host/Compile
endef

define Host/Install
	$(INSTALL_BIN) $(HOST_BUILD_DIR)/02_src/utils/genHdrnMergeXML/merge.py $(STAGING_DIR_HOST)/bin/
	$(INSTALL_BIN) $(HOST_BUILD_DIR)/02_src/utils/genHdrnMergeXML/target_validate.xsd $(STAGING_DIR_HOST)/bin/
endef

$(eval $(call HostBuild))
$(eval $(call BuildPackage,$(PKG_NAME)))
