/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/*  ***************************************************************************** 
 *         File Name    : ugw_proto.h                                           *	
 *         Description  : Common Library contains functions, defines, 		*
 *			  structs, enums used across the modules like CAL, CAPI,*
 *			  CSD, Servd, and Management Entities			*
 *  *****************************************************************************/

/*! \file ugw_proto.h
 \brief File contains common API prototypes used across all modules the software
*/

/** \addtogroup LIBHELP */
/* @{ */


#ifndef _UGW_PROTO_H
#define _UGW_PROTO_H

#include "ugw_defs.h"
#include "ugw_enums.h"
#include "ugw_structs.h"
#include <help_objlist.h>

#include <libubus.h>
#include <libubox/blobmsg_json.h>
#include <libubox/blobmsg.h>


/*!  \brief  API to create a ubus context
  \return Ubus context on successful
*/
struct ubus_context * help_ubusConnect(void);

/*!  \brief  API to disconnect/close an ubus context
  \param[in] ctx Pointer to the ubus context
  \return 
  */
void help_ubusDisconnect( IN struct ubus_context *ctx);

/*!  \brief API to extract the ubus blob message and fill the ObjList data structure. This function is invoked upon receipt of a message over the ubus 
  \param[in] msg Ubus blob msg
  \param[out] pxMsgObject Pointer to Msg header and ObjList structure
  \return  
*/
int help_blobToObjList(IN struct blob_attr *msg,
									OUT MsgHeader *pxMsgObject);

/*!  \brief API to prepare the ubus blob message from the ObjList data structure. This function is invoked before a message is sent over the ubus
  \param[out] buf Ubus blob message
  \param[in] pxMsgObject Pointer to Msg header and ObjList structure
  \param[in] ctx In case of retransmission, required context details, else NO_VALUE_ARG can be passed
  \param[in] req ptr Access data information recieved msg over ubus
    \return  
*/
int help_objListToBlob(OUT struct blob_buf *buf,
									IN MsgHeader *pxMsgObject,
									IN struct ubus_context *ctx,
									IN struct ubus_request_data *req);


/*!  \brief API to send msg to server, which is registered with UBUSD
  \param[in] pxReqMsg Request msg to set/get 
  \param[in] fnCallBak Callback function on return
  \param[in] pcServerName Server name registered with the ubusd, for ex-csd, servd, etc
  \param[in] pcOper Operation supported operation by any server
  \param[out] pxResMsgHeader Response msg for the requested msg
    \return  UGW_SUCCESS on successful /  UGW_FAILURE on failure
*/
int help_sendMsgToServer(IN MsgHeader *pxReqMsg, 
							IN ubus_data_handler_t fnCallBak, 
							IN const char *pcServerName, 
							IN const char *pcOper, 
							OUT MsgHeader *pxResMsgHeader);

/*!  \brief  API to dump/print the Object Info from the provided Object List 
  \param[in] pxMsg Pointer to the Msg header and objlist
  \return  
*/
void help_printMsg(IN MsgHeader *pxMsg);

/*!  \brief  API to dump/print the Object Info from the provided Object List in json format
  \param[in] pxMsg Pointer to the Msg header and objlist
  \param[out] ppcBuf Buf with json format string
  \return  
*/
int help_getJsonFormat(IN MsgHeader *pxMsg,
						OUT char **ppcBuf);

/*!  \brief  API to free the object info from the Object List structure
  \param[in] pxMsg Pointer to the List head object node
  \param[in] nFlag Flag to remove complete msg
  \return  
*/
void help_delMsg(IN MsgHeader *pxMsg,bool nFlag);

/*! \brief  API to send a response message to the CAL module if the received request was for addition or deletion of an object 
        \param[in] blob_buf Ubus blob message
        \param[in] nStatus Status, based on which the blob message will be constructed. Else need to provide instance number or key string to identify the object or parameter
        \return 
*/
void help_RetStBlobMsg(OUT struct blob_buf *buf,
						IN int nStatus);

/*! \brief  API to measure time taken in milliseconds of any exection
        \param[in] starttime  Start time
        \param[in] finishtime End time
        \return Time diff on successful
*/
long help_getDiffTime(IN struct timeval *starttime, 
						IN struct timeval *finishtime);

/*! \brief  API to covert blob msg to notify list
        \param[in] msg Blob msg
        \param[in] pxNotify Notification list
        \return Notify structure gets filled on successful
*/
int help_blobToNotifyList(IN struct blob_attr *msg, 
							OUT NotifyObjData *pxNotify);

/*! \brief  API to covert notify list to blobmsg
        \param[in] buf Buffer to fill notify struct
        \param[in] pxNotify Notification list
        \return Buf filled with notify struct on successful
*/
int help_notifyListToBlob(OUT struct blob_buf *buf,
							IN NotifyObjData *pxNotify);

/*! \brief  API to get msg type
        \param[in] head Message head
        \param[in] len Message length
        \return Type of the msg on successful
*/
int help_getMsgType(IN struct blob_attr *head,
						OUT unsigned int len);

/*!
	\brief Prints recieved or constructed msg
	\param[in] pxMsg Msg header ptr
	\return 
*/
static inline void  HELP_PRINT_MSG(IN MsgHeader *pxMsg)
{
	 help_printMsg(pxMsg);
}

/*!
	\brief Macro to delete msg header
	\param[in] pxMsg Msg head ptr
	\return 
*/
static inline void HELP_DELETE_MSG(IN MsgHeader *pxMsg)
{
	help_delMsg(pxMsg,1);
}

#endif  //#ifndef _UGW_PROTO_H
/* @} */
