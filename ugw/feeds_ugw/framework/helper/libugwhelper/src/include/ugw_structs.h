/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/*  ***************************************************************************** 
 *         File Name    :  ugw_structs.h                                        *
 *         Description  : Common Library contains functions, defines,		*
 *			  structs, enums used across the modules like CAL, CAPI,*
 *			  CSD, Servd, and Management Entities			*
 *  *****************************************************************************/


/*! \file ugw_structs.h
\brief File contains the data
    structures common across all modules in UGW software
*/

#ifndef _UGW_STRUCTS_H
#define _UGW_STRUCTS_H

#include <help_structs.h>
#include "ugw_enums.h"
#include "ugw_defs.h"

/** \addtogroup LIBHELP */
/* @{ */

/*! 
  \brief Contains the header part of the msg with objlist 
  */
typedef struct
{
	uint32_t unMainOper;/*!< GET | SET | ROLLBACK | FACTORY RESET | BACKUP | RESTORE | REBOOT | FW_UPGRADE */
	uint32_t unSubOper;/*!< ID_ONLY | SUB_TREE | ATTR_ONLY | ATTR_ACS | OTHER */
	uint32_t unOwner;/*!< WEB | TR069 | UPnP | CLI | OTHER */
	int32_t nFlag;/*!< Reserved */
	int32_t unMsgSize;/*!< MsgSize */
	void *pObjType ; /*!< List ptr refers Objlist | ObjAttrList | ObjAcsList */
}MsgHeader;

/*! 
    \brief Contains the call parameters(name, faultcode) 
      functions and Callbacks 
*/
typedef struct 
{
   char sName[MAX_LEN_OBJNAME];/*!< Name of the parameter */
   int RespCode;/*!< Fault response */
}RespCode;

/*!
  \brief Contains the notification details
  */
typedef struct
{
        unsigned int  unNotifyId;       /*!< Notify ID from SL*/
        unsigned char ucType;           /*!< SYNC/ASYNC */
        ParamList *pxParamList;         /*!< Name Value Pair */
} NotifyObjData;

/*!
  \brief  Servd to provide Data to Service Libraries 
*/
typedef struct {
	uint16_t		unSid;		/*!< Assign Service Id */
	uint32_t		unMainOper;	/*!< INIT/GET/MODIFY/NOTIFY/UNINIT|ROLLBACK|FACTORY RESET|BACKUP|RESTORE|REBOOT|FW_UPGRADE */
	uint32_t		unOwner;	/*!< WEB | TR069 | UPnP | CLI | OTHER */
	ObjList			*pxObjList;	/*!< ObjList data from CAL to SL */
	NotifyObjData		xNotify;	/*!< Optional Notify list that can be filled by SL to ServD */
	int			nStatus;		/*!< Operation status from SL SUCCESS/FAIL */
} ServdData;

/* @} */

#endif //#ifndef _UGW_STRUCTS_H



