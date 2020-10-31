/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/*  ***************************************************************************** 
 *         File Name    : capi.h 	                                        *
 *	   Author	: Ramesh.Pungavanam@lantiq.com				*
 *         Description  : CAPI - Configuration API layer which acts 		*
 *         		  as interface between CAL - CSD.			*
 *                                                                              *
 *  *****************************************************************************/

/*! \file capi.h
  \brief This file contains all function prototype of CAPI layer functionalities.
  The CAPI provides a set of procedures that allow the get/set of data from the database for diffrent modules across the system.
*/

#ifndef _CAPI_H
#define _CAPI_H

#include <ugw_proto.h>
#include <libubus.h>
#include <libubox/blobmsg_json.h>
#include <libubox/blobmsg.h>

/** \defgroup  CSD_CFG Config Storage Daemon
  */
/* @{ */

/*!
        \brief CAPI_UBUS_SERVER
*/
#define CAPI_UBUS_SERVER "csd" /*!<  csd server object which offers many methods like get,set etc . */

/*!
        \brief CAPI_UBUS_METHOD_PATH
*/
#define CAPI_UBUS_METHOD_PATH "path" /*!< polciy method.*/

/*!
        \brief CAPI_UBUS_METHOD_GET
*/
#define CAPI_UBUS_METHOD_GET "get" /*!< get method.*/

/*!
        \brief CAPI_UBUS_METHOD_SET
*/
#define CAPI_UBUS_METHOD_SET "set" /*!< set method.*/

/*!
        \brief CAPI_UBUS_METHOD_ADD
*/
#define CAPI_UBUS_METHOD_ADD "add" /*!< add method.*/

/*!
        \brief CAPI_UBUS_METHOD_DEL
*/
#define CAPI_UBUS_METHOD_DEL "del" /*!< DEL method.*/

/*! \brief capi_getValue API is used to get the value from xml tree.
  \param[in] pxMsgObjList name,value,type pair returned the caller.
  \return UGW_SUCCESS / UGW_FAILURE.
*/
int capi_getValue(INOUT MsgHeader *pxMsgObjList);
				

/*! \brief capi_setValue API is used to set the node values to xml tree.
  \param[in] pxMsgObjList with values returned to the caller.
  \return UGW_SUCCESS / UGW_FAILURE
  */
int capi_setValue(IN MsgHeader *pxMsgObjList);

/*! \brief capi_blobmsgTest API is used to blob msg between server and client.
  \param[in] buf complete msg in blob format.
  \return UGW_SUCCESS / UGW_FAILURE
  */
int capi_blobmsgTest(IN struct blob_buf *buf);

#endif  //#ifndef _CAPI_H
/* @} */
