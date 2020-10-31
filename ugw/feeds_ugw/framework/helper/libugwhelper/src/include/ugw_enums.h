/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/*  ***************************************************************************** 
 *         File Name    : ugw_enums.h	                                        *	
 *         Description  : Common Library contains functions, defines,		*
 *			  structs, enums used across modules like CAL, CAPI,	*
 *			  CSD, Servd, and Management Entities			*
 *  *****************************************************************************/


/*! \file ugw_enums.h
\brief File contains all enumerations for the common library 
*/

#ifndef _UGW_ENUMS_H
#define _UGW_ENUMS_H

#include <help_enums.h>

/** \addtogroup LIBHELP */

/* @{ */


/*! \enum ValueType
    \brief Enum containing the data type of parameter int, string, boolean and others
*/
typedef enum 
{
   DTYP_INT = 0x1,/*!< Int */
   DTYP_UNSIGNEDINT = 0x2,/*!< Unsigned int  */
   DTYP_STRING = 0x4,/*!< Char[] */
   DTYP_DATETIME = 0x8,/*!< DateTime */
   DTYP_BOOLEAN = 0x10,/*!< Bool */
   DTYP_HEXBINARY = 0x20,/*!< Hex binary */ 
   DTYP_LONG = 0x40,/*!< Long */ 
   DTYP_UNLONG = 0x80,/*!< Unsigned Long */ 
   DTYP_BASE64 = 0x100, /*!< Base */ 
   DTYP_NONE = 0x200, /*!< No Type */ 
   DTYP_FLOAT = 0x10000000/*!< Float */ 
}ValueType;

/*! \enum UPERMIT
    \brief Enum containing the user privileges information 
*/
typedef enum
{
  UPER_SUADMIN = 0x20000, /*!< Super admin user */
  UPER_ADMIN = 0x40000, /*!< Admin user */
  UPER_NORMAL  = 0x80000, /*!< Normal user */
  UPER_GUEST  = 0x100000, /*!< Guest user */
}UserPer;

/*! \enum MISC
    \brief Enum containing the both Attribute and Acs.
*/
typedef enum
{
  MISC_READ = 0x200000, /*!< Read bit*/
  MISC_WRITE = 0x400000, /*!< Write bit*/
  MISC_DYNAMIC = 0x800000, /*! < Dynamic bit */
  MISC_MODIFIED = 0x1000000, /*! < Modified bit*/
  MISC_PASSWD = 0x40000000, /*! < Password bit */
}Misc;

/*! \enum OptObj
    \brief Enum containing Optional object get method operations to be used with Servd's cal get API 
*/
typedef enum
{
  GETOPT_LOCAL = 0x2000000, /*!< Get data only from Servd's local cache */
  GETOPT_LOCALDB = 0x4000000, /*!< Get data from Servd's local cache and failsafe from DB */
  GETOPT_DBLOCAL = 0x8000000, /*!< Get data from DB and failsafe from Servd's local cache */
} OptObj;

/*! \enum ObjOper
    \brief Enum containing the Sub operation at object level
	\note It is possible to recieve multiple objects with mutiple operations from ME, for example: Any WIZARD page can include 'N' number of objects and at same time can
    \ include add, delete, modify for different objects
*/
typedef enum
{
   OBJOPT_ADD = 0x1,/*!< Add operation */
   OBJOPT_DEL = 0x2,/*!< Delete operation */
   OBJOPT_MODIFY = 0x4,/*!< Modify operation */
   OBJOPT_NOOBJOPER = 0x8,/*!< No Object Operation */
   OBJOPT_ACTIVATE = 0x10 /*!< Need Activate Operation*/
}ObjOper;

/*! \enum MainOper
    \brief Enum containing the main operation which includes GET, SET, ROLLBACK, FACTORY_RESET, BACKUP, REBOOT, FW_UPGRADE
    \ for per transaction 
*/
typedef enum
{
   MOPT_GET = 0x1,/*!< GET operation */
   MOPT_SET = 0x2,/*!< SET operation */
   MOPT_INIT = 0x4,/*!< INIT operation */
   MOPT_UNINIT = 0x8,/*!< UNINIT operation */
   MOPT_NOTIFY = 0x10,/*!< NOTIFY operation */
   MOPT_FACTORYRESET = 0x20,/*!< FACTORY_RESET operation */
   MOPT_BACKUP = 0x40,/*!< BACKUP operation */
   MOPT_REBOOT = 0x80,/*!< REBOOT operation */
   MOPT_FWUPGRADE = 0x100,/*!< FW_UPGRADE operation */
   MOPT_ROLLBACK = 0x200,/*!< ROLLBACK */
   MOPT_NOOPER = 0x400,/*!< NO_OPER */
   MOPT_LOGSET = 0x800,/*!< LOGSET */
   MOPT_TIMERACTION = 0x1000,/*!< TIMERACTION*/
   MOPT_ACTIVATE = 0x2000,/*!< NEED ACTIVATE*/
}MainOper;

/*! \enum SubOper
    \brief Enum containing the type of operation which includes ID_ONLY,SUB_TREE etc
    \ for per transaction 
*/
typedef enum
{
   NXT_LVL_TRUE = 0x200,/*!< Get level 2 objects only */
   GET_NOTIFY_SET = 0x400, /*!< Get the parameter where active notification is set and the value changed */
   RESET_CHANGEFLAG = 0x800, /*!< Reset change flag where change flag is set */
   GET_PWD_VAL = 0x1000 /*!< Get the parameter value even if password attribute is set */
}OwnerSpec;


/*! \enum NotifyType
    \brief Enum containing the type of notification in the system
*/
typedef enum {
        NOTIFY_SYNC = 0x1,  /*!< SYNC path Notification */
        NOTIFY_ASYNC = 0x2 /*!< ASYNC path Notification */
} NotifyType;



/* @} */

#endif // #ifndef _UGW_ENUMS_H
