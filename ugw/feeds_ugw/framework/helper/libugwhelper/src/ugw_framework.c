/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/* ****************************************************************************** 
 *         File Name    : ugw_framework.c                                       *
 *         Description  : helper Library , which contains functions,defines,	*
 *			  structs,enums used across the modules like CAL,CAPI,	*
 *			  CSD,Servd,and Management Entities			*
 * ******************************************************************************/

#include <stdio.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <string.h>
#include <sys/time.h>
#include <libubus.h>
#include <libubox/blobmsg_json.h>
#include <libubox/blobmsg.h>
#include "ugw_proto.h"
#include <ugw_structs.h>
#include <ugw_defs.h>
#include <ugw_enums.h>
#include <ugw_objmsg.h>


//Global variables 
static int gnTimeOut=60; //time out in sec


#ifndef LOG_LEVEL
uint16_t LOGLEVEL = SYS_LOG_DEBUG + 1;
#else
uint16_t LOGLEVEL = LOG_LEVEL + 1;
#endif

#ifndef LOG_TYPE
uint16_t LOGTYPE = SYS_LOG_TYPE_FILE;
#else
uint16_t LOGTYPE = LOG_TYPE;
#endif

/*  =============================================================================
 *   Function Name 	: help_timeValDiff					*
 *   Description 	: Function to measure time diffrence in milliseconds.	*
 *  ============================================================================*/
long help_getDiffTime(IN struct timeval *starttime, IN struct timeval *finishtime)
{
        long msec;
        msec=(finishtime->tv_sec-starttime->tv_sec)*1000;
        msec+=(finishtime->tv_usec-starttime->tv_usec)/1000;
        return msec;
}

/*  =============================================================================
 *   Function Name 	: help_RetStBlobMsg					*
 *   Description 	: Function to construct common response msg to the	*
 *			  caller						*
 *  ============================================================================*/
void help_RetStBlobMsg(OUT struct blob_buf *buf,IN int nStatus)
{
        blobmsg_buf_init(buf);
	if(nStatus)
	{
		blobmsg_add_string(buf, "ReturnStatus", "Success");
	}
	else
	{
		blobmsg_add_string(buf, "ReturnStatus", "Failure");
	}
}
/*  =============================================================================
 *   Function Name 	: help_fillTableACSAttr					*
 *   Description 	: Function to construct objlist				*
 *  ============================================================================*/
static int help_fillTableACSAttr(struct blob_attr *head, unsigned int len,MsgHeader *pxMsgObject,const char *pcObjName)
{

	char sObjName[MAX_LEN_OBJNAME]={0};
	char sParamName[MAX_LEN_PARAM_NAME]={0},sParamValue[MAX_LEN_PARAM_VALUE]={0};
	uint32_t unParamFlag=0;
	uint32_t unObjOper=0,unObjFlag=0;	
	struct blob_attr *attr;
	struct blobmsg_hdr *hdr;
	int nType;
	static int nObjParamCount=0;
	static int nMsgHeaderCount=0;
	ObjACSList *pxObj=NULL;
	char *pcTmp=NULL;

	__blob_for_each_attr(attr, head, len) 
	{
		hdr = blob_data(attr);
		nType = blobmsg_type(attr);
		switch(nType) 
		{
			case BLOBMSG_TYPE_TABLE :
				if(strcmp((char *)hdr->name,"Mgh") == 0)
				{
					/* Reset required to parse new msg */
					nMsgHeaderCount=1;
					nObjParamCount=0;
					help_fillTableACSAttr(blobmsg_data(attr), blobmsg_data_len(attr), pxMsgObject, NULL);
				}
				else
				{
					/* Reset ObjList */
					nObjParamCount=0;
					help_fillTableACSAttr(blobmsg_data(attr), blobmsg_data_len(attr), pxMsgObject,(char *)hdr->name);
				}
				break;	
			case BLOBMSG_TYPE_STRING:
				pcTmp = blobmsg_get_string(attr);
				if(pcTmp != NULL)
				{	
					if(nObjParamCount == 2)
					{
						memset(&sParamName[0], 0, sizeof(sParamName));
						strncpy(sParamName, pcTmp ,MAX_LEN_PARAM_NAME);
						sParamName[MAX_LEN_PARAM_NAME-1]='\0';
						nObjParamCount++;
					}
					else if(nObjParamCount == 3)
					{
						memset(&sParamValue[0], 0, sizeof(sParamValue));
						strncpy(sParamValue, pcTmp, MAX_LEN_PARAM_VALUE);
						sParamValue[MAX_LEN_PARAM_VALUE-1]='\0';
						nObjParamCount++;
					}
				}
				break;
			case BLOBMSG_TYPE_INT8:
				printf("%d\n", blobmsg_get_u8(attr));
				break;
			case BLOBMSG_TYPE_INT16:
				break;

			case BLOBMSG_TYPE_INT32:
				if (nObjParamCount == 0)
				{
					if (nMsgHeaderCount == 1)
					{
						pxMsgObject->unMainOper = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 2)
					{
						pxMsgObject->unSubOper = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 3)
					{
						pxMsgObject->unOwner = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 4)
					{
						/*reserved flag not used as of now */
						pxMsgObject->nFlag = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
						LOGF_LOG_DEBUG("Msg Header[%d:%d:%d:%d]\n",pxMsgObject->unMainOper, pxMsgObject->unSubOper, pxMsgObject->unOwner, pxMsgObject->nFlag);
					}
				}
				if (nObjParamCount == 0 && nMsgHeaderCount == 5)
				{
					unObjOper = blobmsg_get_u32(attr);
					nObjParamCount++;
				}
				else if (nObjParamCount == 1)
				{
					unObjFlag = blobmsg_get_u32(attr);
					strncpy(sObjName,pcObjName,MAX_LEN_OBJNAME);
					sObjName[MAX_LEN_OBJNAME-1]='\0';
					LOGF_LOG_DEBUG(" ObjList %s:%d:%d\n", sObjName, unObjOper, unObjFlag);
					pxObj=help_addAcsObjList(pxMsgObject->pObjType, sObjName, unObjOper, unObjFlag);
					nObjParamCount++;
				}
				else if (nObjParamCount == 4)
				{
					unParamFlag = blobmsg_get_u32(attr);
					LOGF_LOG_DEBUG(" ParamList  %s:%s:%d\n",sParamName, sParamValue, unParamFlag);
					help_addAcsParamList(pxObj, sParamName, sParamValue, unParamFlag);
					/* Reset ParamList */
					nObjParamCount = 2; 
				}
				else if(strcmp("MsgSize", (char *)hdr->name) == 0)
				{
					pxMsgObject->unMsgSize += blobmsg_get_u32(attr);
					/* Msgsize is last msg in blob hence reseting msg counters. */ 
					nMsgHeaderCount=0;
					nObjParamCount=0;
				}
				break;
			case BLOBMSG_TYPE_INT64:
				printf("%"PRIu64"\n", blobmsg_get_u64(attr));
				break;
		}
	}
	return UGW_SUCCESS;
}

/*  =============================================================================
 *   Function Name 	: help_fillTableAttr					*
 *   Description 	: Function to construct objlist				*
 *  ============================================================================*/
static int help_fillTableAttr(struct blob_attr *head, unsigned int len,MsgHeader *pxMsgObject,const char *pcObjName)
{
	char sObjName[MAX_LEN_OBJNAME],sWebName[MAX_LEN_WEBNAME];
	uint32_t unObjAttr;

	char sParamName[MAX_LEN_PARAM_NAME],sParamProfile[MAX_LEN_PROFILENAME],sParamValue[MAX_LEN_PARAM_VALUE],
	     sParamWebName[MAX_LEN_WEBNAME],sParamValidVal[MAX_LEN_VALID_VALUE];
	uint32_t unMinVal=0,unMaxVal=0,unMinLen=0,unMaxLen=0,unParamFlag=0;
	
	struct blob_attr *attr;
	struct blobmsg_hdr *hdr;
	int nType;
	char *pcTmp=NULL;

	static int nMsgHeaderCount=0;
	static int nObjParamCount=0;

	ObjAttrList *pxObj=NULL;

	__blob_for_each_attr(attr, head, len) 
	{
		hdr = blob_data(attr);
		nType = blobmsg_type(attr);
		switch(nType) 
		{
			case BLOBMSG_TYPE_TABLE :
				if(strcmp((char *)hdr->name,"Mgh") == 0)
				{
					/* Reset required to parse new msg */
					nMsgHeaderCount=1;
					nObjParamCount=0;
					help_fillTableAttr(blobmsg_data(attr), blobmsg_data_len(attr),pxMsgObject,NULL);
				}
				else
				{
					/* Reset ObjList */
					nObjParamCount=0;
					help_fillTableAttr(blobmsg_data(attr), blobmsg_data_len(attr),pxMsgObject,(char *)hdr->name);
				}
				break;	
			case BLOBMSG_TYPE_STRING:
				pcTmp = blobmsg_get_string(attr);
				if(pcTmp != NULL)
				{
					if ( nObjParamCount == 0)
					{
						memset(&sWebName[0], 0x0, sizeof(sWebName));
						strncpy(sWebName, pcTmp, MAX_LEN_WEBNAME);
						sWebName[MAX_LEN_WEBNAME-1]='\0';
						nObjParamCount++;
					}
					else if(nObjParamCount == 2)
					{
						memset(&sParamName[0], 0x0, sizeof(sParamName));
						strncpy(sParamName, pcTmp, MAX_LEN_PARAM_NAME);
						sParamName[MAX_LEN_PARAM_NAME-1]='\0';
						nObjParamCount++;
					}
					else if(nObjParamCount == 3)
					{
						memset(&sParamProfile[0], 0, sizeof(sParamProfile));
						strncpy(sParamProfile, pcTmp, MAX_LEN_PROFILENAME);
						sParamProfile[MAX_LEN_PROFILENAME-1]='\0';
						nObjParamCount++;
					}
					else if(nObjParamCount == 4)
					{
						memset(&sParamWebName[0], 0, sizeof(sParamWebName));
						strncpy(sParamWebName, pcTmp,MAX_LEN_WEBNAME);
						sParamWebName[MAX_LEN_WEBNAME-1]='\0';
						nObjParamCount++;
					}
					else if(nObjParamCount == 5)
					{
						memset(&sParamValidVal[0], 0, sizeof(sParamValidVal));
						strncpy(sParamValidVal, pcTmp, MAX_LEN_VALID_VALUE);
						sParamValidVal[MAX_LEN_VALID_VALUE-1]='\0';
						nObjParamCount++;
					}
					else if(nObjParamCount == 6)
					{
						memset(&sParamValue[0], 0, sizeof(sParamValue));
						strncpy(sParamValue, pcTmp, MAX_LEN_PARAM_VALUE);
						sParamValue[MAX_LEN_PARAM_VALUE-1]='\0';
						nObjParamCount++;
					}
				}
				break;
			case BLOBMSG_TYPE_INT8:
				printf("%d\n", blobmsg_get_u8(attr));
				break;
			case BLOBMSG_TYPE_INT16:
				printf("%d\n", blobmsg_get_u16(attr));
				break;
			case BLOBMSG_TYPE_INT32:
				if (nObjParamCount == 0)
				{
					if (nMsgHeaderCount == 1)
					{
						pxMsgObject->unMainOper = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 2)
					{
						pxMsgObject->unSubOper = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 3)
					{
						pxMsgObject->unOwner = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 4)
					{
						/*reserved flag not used as of now */
						pxMsgObject->nFlag = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
				}
				if (nObjParamCount == 1)
				{
					unObjAttr = blobmsg_get_u32(attr);
					strncpy(sObjName,pcObjName,MAX_LEN_OBJNAME);
					sObjName[MAX_LEN_OBJNAME-1]='\0';
					LOGF_LOG_DEBUG("ObjList %s:%s:%d\n",sObjName,sWebName,unObjAttr);
					pxObj=help_addObjAttrList(pxMsgObject->pObjType,sObjName,sWebName,unObjAttr);
					nObjParamCount++;
				}
				if (nObjParamCount == 7)
				{
					unMinVal = blobmsg_get_u32(attr);
					nObjParamCount++;
				}
				else if (nObjParamCount == 8)
				{
					unMaxVal = blobmsg_get_u32(attr);
					nObjParamCount++;
				}
				else if (nObjParamCount == 9)
				{
					unMinLen = blobmsg_get_u32(attr);
					nObjParamCount++;
				}
				else if (nObjParamCount == 10)
				{
					unMaxLen = blobmsg_get_u32(attr);
					nObjParamCount++;
				}
				else if (nObjParamCount == 11)
				{
					unParamFlag = blobmsg_get_u32(attr);
					LOGF_LOG_DEBUG(" ParamList  %s:%s:%s:%s:%s:%d:%d:%d:%d:%d\n",sParamName,sParamProfile,sParamWebName,
									sParamValidVal,sParamValue,unMinVal,unMaxVal,unMinLen,unMaxLen,unParamFlag);
					help_addParamAttrList(pxObj,sParamName,sParamProfile,sParamWebName,sParamValidVal,
										sParamValue,unMinVal,unMaxVal,unMinLen,unMaxLen,unParamFlag);
					/* Reset ParamList */
					nObjParamCount = 2; 
				}
				else if(strcmp("MsgSize", (char *)hdr->name) == 0)
				{
					pxMsgObject->unMsgSize += blobmsg_get_u32(attr);
					/* Msgsize is last msg in blob hence reseting msg counters. */ 
					nMsgHeaderCount=0;
					nObjParamCount=0;
				}
				break;
			case BLOBMSG_TYPE_INT64:
				printf("%"PRIu64"\n", blobmsg_get_u64(attr));
				break;
		}
	}
	return UGW_SUCCESS;
}
/*  =============================================================================
 *   Function Name 	: help_fillTableValue					*
 *   Description 	: Function to construct objlist				*
 *  ============================================================================*/
static int help_fillTableValue(struct blob_attr *head, unsigned int len,MsgHeader *pxObjList,const char *pcObjName)
{
	char sObjName[MAX_LEN_OBJNAME];
	uint16_t unOid=0,unSid=0,unParamId=0;
	char sParamName[MAX_LEN_PARAM_NAME]={0},sParamValue[MAX_LEN_PARAM_VALUE]={0};
	uint32_t unParamFlag=0;
	uint32_t unObjOper=0,unObjFlag=0;	
	struct blob_attr *attr;
	struct blobmsg_hdr *hdr;
	int nType=0;
	static int nObjParamCount=0;
	static int nMsgHeaderCount=0;
	ObjList *pxObj=NULL;
	char *pcTmp=NULL;


	__blob_for_each_attr(attr, head, len) 
	{
		hdr = blob_data(attr);
		nType = blobmsg_type(attr);
		switch(nType) 
		{
			case BLOBMSG_TYPE_TABLE :
				if(strcmp((char *)hdr->name,"Mgh") == 0)
				{
					/* Reset required to parse new msg */
					nMsgHeaderCount=1;
					nObjParamCount=0;
					help_fillTableValue(blobmsg_data(attr), blobmsg_data_len(attr),pxObjList,NULL);
				}
				else
				{
					/* Reset ObjList */
					nObjParamCount=0;
					help_fillTableValue(blobmsg_data(attr), blobmsg_data_len(attr),pxObjList,(char *)hdr->name);
				}
				break;	
			case BLOBMSG_TYPE_STRING:	
				pcTmp = blobmsg_get_string(attr);
				if (pcTmp != NULL)
				{
					if(nObjParamCount == 4)
					{
						sParamName[0]='\0';
						strncpy(sParamName, pcTmp, MAX_LEN_PARAM_NAME);
						sParamName[MAX_LEN_PARAM_NAME-1] = '\0';
						nObjParamCount++;
					}
					if(nObjParamCount == 6)
					{
						sParamValue[0]='\0';
						strncpy(sParamValue, pcTmp, MAX_LEN_PARAM_VALUE);
						sParamValue[MAX_LEN_PARAM_VALUE-1]='\0';
						nObjParamCount++;
					}
				}
				break;
			case BLOBMSG_TYPE_INT8:
				printf("%d\n", blobmsg_get_u8(attr));
				break;
			case BLOBMSG_TYPE_INT16:
				if ( nObjParamCount == 0)
				{
					unSid = blobmsg_get_u16(attr);
					nObjParamCount++;
				}
				else if(nObjParamCount == 1)
				{
					unOid = blobmsg_get_u16(attr);
					nObjParamCount++;
				}
				else if(nObjParamCount == 5)
				{
					unParamId = blobmsg_get_u16(attr);
					nObjParamCount++;
				}
				break;

			case BLOBMSG_TYPE_INT32:
				if (nObjParamCount == 0)
				{
					if (nMsgHeaderCount == 1)
					{
						pxObjList->unMainOper = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 2)
					{
						pxObjList->unSubOper = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 3)
					{
						pxObjList->unOwner = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					}
					else if (nMsgHeaderCount == 4)
					{
						/*reserved flag not used as of now */
						pxObjList->nFlag = blobmsg_get_u32(attr);
						nMsgHeaderCount++;
					LOGF_LOG_DEBUG("Msg Header[%d:%d:%d:%d]\n",pxObjList->unMainOper,pxObjList->unSubOper,pxObjList->unOwner,pxObjList->nFlag);
					}
				}
				if (nObjParamCount == 2)
				{
					unObjOper = blobmsg_get_u32(attr);
					nObjParamCount++;
				}
				else if (nObjParamCount == 3)
				{
					unObjFlag = blobmsg_get_u32(attr);
					strncpy(sObjName,pcObjName,MAX_LEN_OBJNAME);
					sObjName[MAX_LEN_OBJNAME-1]='\0';
					LOGF_LOG_DEBUG(" ObjList %s:%d:%d:%d:%d\n",sObjName,unSid,unOid,unObjOper,unObjFlag);
					pxObj=help_addObjList(pxObjList->pObjType,sObjName,unSid,unOid,unObjOper,unObjFlag);
					nObjParamCount++;
				}
				else if (nObjParamCount == 7)
				{
					unParamFlag = blobmsg_get_u32(attr);
					LOGF_LOG_DEBUG(" ParamList  %s:%d:%s:%d\n",sParamName,unParamId,sParamValue,unParamFlag);
					help_addParamList(pxObj,sParamName,unParamId,sParamValue,unParamFlag);
					/* Reset ParamList */
					nObjParamCount = 4; 
				}
				else if(strcmp("MsgSize", (char *)hdr->name) == 0)
				{
					pxObjList->unMsgSize += blobmsg_get_u32(attr);
					/* Msgsize is last msg in blob hence reseting msg counters. */ 
					nMsgHeaderCount=0;
					nObjParamCount=0;
				}
				break;
			case BLOBMSG_TYPE_INT64:
				printf("%"PRIu64"\n", blobmsg_get_u64(attr));
				break;
		}
	}
	return UGW_SUCCESS;
}

/*  =============================================================================
 *   Function Name 	: help_getMsgType					*
 *   Description 	: Function to type of object list 			*
 *  ============================================================================*/
int help_getMsgType(struct blob_attr *head,unsigned int len)
{
	struct blob_attr *attr;
	int nType;
	
	__blob_for_each_attr(attr, head, len) 
	{
		nType = blobmsg_type(attr);
		switch(nType) 
		{
			case BLOBMSG_TYPE_INT32:
						return blobmsg_get_u32(attr);
		 
		}
	}
	/* when msg is empty */
	return UGW_SUCCESS;
 }
/*  =============================================================================
 *   Function Name 	: help_blobToObjList					*
 *   Description 	: Function to extract the blob msg and fill the caller 	*
 *   			  struct.						*
 *  ============================================================================*/
int help_blobToObjList(IN struct blob_attr *msg,OUT MsgHeader *pxMsg)
{
	int nRet = UGW_SUCCESS;

	if ( IS_OBJLIST(pxMsg->unSubOper))
	{
		nRet = help_fillTableValue(blobmsg_data(msg), blobmsg_data_len(msg),pxMsg,NULL);

	}
	else if (IS_SOPT_OBJATTR(pxMsg->unSubOper))
	{
		nRet = help_fillTableAttr(blobmsg_data(msg), blobmsg_data_len(msg),pxMsg,NULL);
	}
	
	else if (IS_SOPT_OBJACSATTR(pxMsg->unSubOper))
	{
		nRet = help_fillTableACSAttr(blobmsg_data(msg), blobmsg_data_len(msg),pxMsg,NULL);
	}
	return nRet;

}
/*  =============================================================================
 *   Function Name 	: help_blobToNotifyList					*
 *   Description 	: Function to blob msg to notify list	 		*
 *  ============================================================================*/
int help_blobToNotifyList(struct blob_attr *msg,NotifyObjData *pxNotify)
{
	struct blob_attr *attr;
	struct blob_attr *head;
	int nType;
	 unsigned int len=0;
	uint32_t unParamCnt=0;
	char *pxTmp = NULL;
	char sParamName[MAX_LEN_PARAM_NAME]={0},sParamValue[MAX_LEN_PARAM_VALUE]={0};

	void *pParam;

	head = blobmsg_data(msg);
	len = blobmsg_data_len(msg);

        pParam = HELP_CREATE_PARAM(SOPT_OBJVALUE);	
	
	__blob_for_each_attr(attr, head, len) 
	{
		nType = blobmsg_type(attr);
		switch(nType) 
		{
			case BLOBMSG_TYPE_INT32:
					pxNotify->unNotifyId = blobmsg_get_u32(attr);
					break;
			case BLOBMSG_TYPE_INT8:
					pxNotify->ucType = blobmsg_get_u8(attr);
					break;
			case BLOBMSG_TYPE_STRING:
					pxTmp = blobmsg_get_string(attr);
					if(pxTmp != NULL)
					{
						if (unParamCnt == 0)
						{
							sParamName[0]='\0';
							strncpy(sParamName, pxTmp, MAX_LEN_PARAM_NAME);
							sParamName[MAX_LEN_PARAM_NAME-1]='\0';
							unParamCnt++;
						}
						else
						{
							sParamValue[0]='\0';
							strncpy(sParamValue, pxTmp , MAX_LEN_PARAM_VALUE);
							sParamValue[MAX_LEN_PARAM_VALUE-1]='\0';
							if(strlen(sParamName) > 0)
								HELP_ONLY_PARAM_SET(pParam,sParamName,sParamValue,SOPT_OBJVALUE);
							unParamCnt=0;
						}
					}
					break;
		}
	}

	pxNotify->pxParamList = pParam;
	return UGW_SUCCESS;
}

/*  =============================================================================
 *   Function Name 	: help_notifyListToBlob					*
 *   Description 	: Function to construct the blob msg 			*
 *  ============================================================================*/
int help_notifyListToBlob(OUT struct blob_buf *buf,IN NotifyObjData *pxNotify)
{
	ParamList *pxParam;
	blobmsg_buf_init(buf);

	blobmsg_add_u32(buf, "nid",pxNotify->unNotifyId);
	blobmsg_add_u8(buf, "type",pxNotify->ucType);
	FOR_EACH_PARAM_ONLY(pxNotify->pxParamList,pxParam) 
	{
		blobmsg_add_string(buf, "pn",pxParam->sParamName);
		blobmsg_add_string(buf, "pv", pxParam->sParamValue);
	}
	return UGW_SUCCESS;
}
/*  =============================================================================
 *   Function Name 	: help_sendMsgToServer					*
 *   Description 	: Function to send get/set request msg to ubus server   *
 * 			  get appropriate response msg				* 
 *  ============================================================================*/
int help_sendMsgToServer(IN MsgHeader *pxReqMsg, IN ubus_data_handler_t fnCallBak, IN const char *pcServerName, 
								IN const char *pcOper, OUT MsgHeader *pxResMsgHeader)
{
	
	static struct blob_buf buf;
	uint32_t unMsgSize=0;
	unsigned int unId=0;
	static struct ubus_context *pxCtx=NULL;
	void *ptbl=NULL;
	int8_t  nRetryCnt = 0;

	int32_t nRet = UGW_SUCCESS;


	memset(&buf, 0x0 , sizeof(struct blob_buf));

	blobmsg_buf_init(&buf);

Retry:
	pxCtx = help_ubusConnect();
	if ( pxCtx == NULL ) {
		LOGF_LOG_CRITICAL(" ubus connect failed \n");
		nRet = UGW_FAILURE;
		//TODO
		sleep(1);
		if (nRetryCnt < 3)
		{
			nRetryCnt++;
			goto Retry;
		}
		else
		{
			goto finish;
		}
	}

	nRet = UGW_SUCCESS;
	
	if (ubus_lookup_id(pxCtx, pcServerName, &unId)) {
		LOGF_LOG_CRITICAL("ubus look-up failed");
		nRet = UGW_FAILURE;
		goto finish;
	}

	blobmsg_add_u32(&buf,"objflag",pxReqMsg->unSubOper);
	ptbl = blobmsg_open_table(&buf,"Mgh");
	blobmsg_add_u32(&buf,"mo",pxReqMsg->unMainOper);
	blobmsg_add_u32(&buf,"so",pxReqMsg->unSubOper);
	blobmsg_add_u32(&buf,"ow",pxReqMsg->unOwner);
	blobmsg_add_u32(&buf,"rf",pxReqMsg->nFlag);
	blobmsg_close_table(&buf, ptbl);

	if(IS_OBJLIST(pxReqMsg->unSubOper))
	{
		ObjList *pxTmpObj;
		ParamList *pxParam;

		
		FOR_EACH_OBJ(pxReqMsg->pObjType,pxTmpObj)
		{
			if (buf.buflen >= UBUS_MSG_LENGTH) 
			{
				/* retransmission msg */
				nRet = ubus_invoke(pxCtx, unId, pcOper , buf.head, fnCallBak, pxResMsgHeader, gnTimeOut * 1000);
				if ( nRet == UBUS_STATUS_TIMEOUT )
				{
					nRet = ERR_UBUS_TIME_OUT;
					goto finish;
				}
				
				unMsgSize += buf.buflen;
				blob_buf_free(&buf);
				memset(&buf,0x0,sizeof(struct blob_buf));
				blobmsg_buf_init(&buf);
				blobmsg_add_u32(&buf,"objflag",pxReqMsg->unSubOper);
				ptbl = blobmsg_open_table(&buf,"Mgh");
				blobmsg_add_u32(&buf,"mo",pxReqMsg->unMainOper);
				blobmsg_add_u32(&buf,"so",pxReqMsg->unSubOper);
				blobmsg_add_u32(&buf,"ow",pxReqMsg->unOwner);
				blobmsg_add_u32(&buf,"rf",pxReqMsg->nFlag);
				blobmsg_close_table(&buf, ptbl);
			}
			ptbl = blobmsg_open_table(&buf,pxTmpObj->sObjName);
			blobmsg_add_u16(&buf,"sid",pxTmpObj->unSid); 
			blobmsg_add_u16(&buf,"oid",pxTmpObj->unOid);
			blobmsg_add_u32(&buf,"oo",pxTmpObj->unObjOper);
			blobmsg_add_u32(&buf,"of",pxTmpObj->unObjFlag);
			FOR_EACH_PARAM(pxTmpObj, pxParam)
			{
				blobmsg_add_string(&buf, "pn",pxParam->sParamName);
				blobmsg_add_u16(&buf, "pi", pxParam->unParamId);
				blobmsg_add_string(&buf, "pv", pxParam->sParamValue);
				blobmsg_add_u32(&buf, "pf", pxParam->unParamFlag);
			}
		blobmsg_close_table(&buf, ptbl);
		}
	}
	else if (IS_SOPT_OBJATTR(pxReqMsg->unSubOper))
	{
		ObjAttrList *pxAttrObj;
		ParamAttrList *pxAttrParam;

		FOR_EACH_OBJATTR(pxReqMsg->pObjType,pxAttrObj)
		{
			if (buf.buflen >= UBUS_MSG_LENGTH) 
			{
				blobmsg_close_table(&buf, ptbl);
				nRet = ubus_invoke(pxCtx, unId, pcOper , buf.head, fnCallBak, pxResMsgHeader, gnTimeOut * 1000);
				if ( nRet == UBUS_STATUS_TIMEOUT )
				{
					nRet = ERR_UBUS_TIME_OUT;
					goto finish;
				}

				unMsgSize += buf.buflen;
				blob_buf_free(&buf);
				
				memset(&buf,0x0,sizeof(struct blob_buf));
				blobmsg_buf_init(&buf);
				blobmsg_add_u32(&buf,"objflag",pxReqMsg->unSubOper);
				ptbl = blobmsg_open_table(&buf,"Mgh");
				blobmsg_add_u32(&buf,"mo",pxReqMsg->unMainOper);
				blobmsg_add_u32(&buf,"so",pxReqMsg->unSubOper);
				blobmsg_add_u32(&buf,"ow",pxReqMsg->unOwner);
				blobmsg_add_u32(&buf,"rf",pxReqMsg->nFlag);
				blobmsg_close_table(&buf, ptbl);
			}	
			ptbl = blobmsg_open_table(&buf,GET_ATTR_OBJNAME(pxAttrObj));
			blobmsg_add_string(&buf,"wn",GET_ATTR_WEBNAME(pxAttrObj)); 
			blobmsg_add_u32(&buf,"fg",GET_ATTR_FLAG(pxAttrObj));
			FOR_EACH_PARAM_ATTR(pxAttrObj,pxAttrParam)
			{
				blobmsg_add_string(&buf, "pn",GET_ATTR_PARAMNAME(pxAttrParam));
				blobmsg_add_string(&buf, "pr", GET_ATTR_PARAMPROFILE(pxAttrParam));
				blobmsg_add_string(&buf, "pw", GET_ATTR_PARAMWEBNAME(pxAttrParam));
				blobmsg_add_string(&buf, "pvv", pxAttrParam->sParamValidVal);
				blobmsg_add_string(&buf, "pv", GET_ATTR_PARAMVALUE(pxAttrParam));
				blobmsg_add_u32(&buf, "pmiv", GET_ATTR_MINVAL(pxAttrParam));
				blobmsg_add_u32(&buf, "pmav", GET_ATTR_MAXVAL(pxAttrParam));
				blobmsg_add_u32(&buf, "pmil", GET_ATTR_MINLEN(pxAttrParam));
				blobmsg_add_u32(&buf, "pmal", GET_ATTR_MAXLEN(pxAttrParam));
				blobmsg_add_u32(&buf, "pf", GET_ATTR_PARAMFLAG(pxAttrParam));
			}
		blobmsg_close_table(&buf, ptbl);
		}
	}
	else if (IS_SOPT_OBJACSATTR(pxReqMsg->unSubOper))
	{
		ObjACSList *pxTmpObjAcs;
		ParamACSList *pxParamAcs;

		FOR_EACH_OBJ_ACS_ATTR(pxReqMsg->pObjType,pxTmpObjAcs)
		{
			if (buf.buflen >= UBUS_MSG_LENGTH) 
			{
				/* retransmission msg */
				nRet = ubus_invoke(pxCtx, unId, pcOper , buf.head, fnCallBak, pxResMsgHeader, gnTimeOut * 1000);
				if ( nRet == UBUS_STATUS_TIMEOUT )
				{
					nRet = ERR_UBUS_TIME_OUT;
					goto finish;
				}				
	
				unMsgSize += buf.buflen;
				blob_buf_free(&buf);
				memset(&buf,0x0,sizeof(struct blob_buf));
				blobmsg_buf_init(&buf);
				blobmsg_add_u32(&buf,"objflag",pxReqMsg->unSubOper);
				ptbl = blobmsg_open_table(&buf,"Mgh");
				blobmsg_add_u32(&buf,"mo",pxReqMsg->unMainOper);
				blobmsg_add_u32(&buf,"so",pxReqMsg->unSubOper);
				blobmsg_add_u32(&buf,"ow",pxReqMsg->unOwner);
				blobmsg_add_u32(&buf,"rf",pxReqMsg->nFlag);
				blobmsg_close_table(&buf, ptbl);
			}
			ptbl = blobmsg_open_table(&buf,pxTmpObjAcs->sObjName);
			blobmsg_add_u32(&buf,"oo",pxTmpObjAcs->unObjOper);
			blobmsg_add_u32(&buf,"of",pxTmpObjAcs->unObjFlag);
			FOR_EACH_PARAM_ACS_ATTR(pxTmpObjAcs, pxParamAcs)
			{
				blobmsg_add_string(&buf, "pn",pxParamAcs->sParamName);
				blobmsg_add_string(&buf, "pv", pxParamAcs->sParamValue);
				blobmsg_add_u32(&buf, "pf", pxParamAcs->unParamFlag);
			}
		blobmsg_close_table(&buf, ptbl);
		}

	}
	
	unMsgSize += buf.buflen;
	blobmsg_add_u32(&buf,"MsgSize",unMsgSize);
	nRet = ubus_invoke(pxCtx, unId, pcOper , buf.head, fnCallBak, pxResMsgHeader, gnTimeOut * 1000);
	if ( nRet == UBUS_STATUS_TIMEOUT )
	{
		nRet = ERR_UBUS_TIME_OUT;
		goto finish;
	}

finish:
	if(buf.buf != NULL)
		blob_buf_free(&buf);

	help_ubusDisconnect(pxCtx);

	return nRet;
}


/*  =============================================================================
 *   Function Name 	: help_objListToBlob					*
 *   Description 	: Function to extract the blob msg and fill the caller 	*
 *   			  struct.						*
 *  ============================================================================*/
int help_objListToBlob(OUT struct blob_buf *buf,IN MsgHeader *pxMsg,struct ubus_context *ctx,struct ubus_request_data *req)
{
	void *ptbl;
	uint32_t unMsgSize=0;
	blobmsg_buf_init(buf);

	blobmsg_add_u32(buf,"objflag",pxMsg->unSubOper);

	ptbl = blobmsg_open_table(buf,"Mgh");
	blobmsg_add_u32(buf,"mo",pxMsg->unMainOper);
	blobmsg_add_u32(buf,"so",pxMsg->unSubOper);
	blobmsg_add_u32(buf,"ow",pxMsg->unOwner);
	blobmsg_add_u32(buf,"rf",pxMsg->nFlag);
	blobmsg_close_table(buf, ptbl);

	if (IS_OBJLIST(pxMsg->unSubOper))
	{
		ObjList *pxTmpObj;
		ParamList *pxParam;

		FOR_EACH_OBJ(pxMsg->pObjType, pxTmpObj)
		{
			if (buf->buflen >= UBUS_MSG_LENGTH) 
			{
				if ( (ctx != NO_ARG_VALUE) && (req != NO_ARG_VALUE))
				{
					ubus_send_reply(ctx, req, buf->head);
					unMsgSize += buf->buflen;
					blob_buf_free(buf);

					memset(buf,0x0,sizeof(struct blob_buf));
					blobmsg_buf_init(buf);
					blobmsg_add_u32(buf,"objflag",pxMsg->unSubOper);
					ptbl = blobmsg_open_table(buf,"Mgh");
					blobmsg_add_u32(buf,"mo",pxMsg->unMainOper);
					blobmsg_add_u32(buf,"so",pxMsg->unSubOper);
					blobmsg_add_u32(buf,"ow",pxMsg->unOwner);
					blobmsg_add_u32(buf,"rf",pxMsg->nFlag);
					blobmsg_close_table(buf, ptbl);
				}
				else
				{
					printf(" Your Msg May Not Be Sent Due To Overload \n");
				}
			}	
			ptbl = blobmsg_open_table(buf,pxTmpObj->sObjName);
			blobmsg_add_u16(buf,"sid",pxTmpObj->unSid); 
			blobmsg_add_u16(buf,"oid",pxTmpObj->unOid);
			blobmsg_add_u32(buf,"oo",pxTmpObj->unObjOper);
			blobmsg_add_u32(buf,"of",pxTmpObj->unObjFlag);
			FOR_EACH_PARAM(pxTmpObj, pxParam)
			{
				blobmsg_add_string(buf, "pn",pxParam->sParamName);
				blobmsg_add_u16(buf, "pi", pxParam->unParamId);
				blobmsg_add_string(buf, "pv", pxParam->sParamValue);
				blobmsg_add_u32(buf, "pf", pxParam->unParamFlag);
			}
		blobmsg_close_table(buf, ptbl);
		}
	}
	else if (IS_SOPT_OBJATTR(pxMsg->unSubOper))
	{
		ObjAttrList *pxAttrObj;
		ParamAttrList *pxAttrParam;

		FOR_EACH_OBJATTR(pxMsg->pObjType,pxAttrObj)
		{
			if (buf->buflen >= UBUS_MSG_LENGTH) 
			{
				if ( (ctx != NO_ARG_VALUE) && (req != NO_ARG_VALUE))
				{
					ubus_send_reply(ctx, req, buf->head);
					unMsgSize += buf->buflen;
					blob_buf_free(buf);
					
					memset(buf,0x0,sizeof(struct blob_buf));
					blobmsg_buf_init(buf);
					blobmsg_add_u32(buf,"objflag",pxMsg->unSubOper);
					ptbl = blobmsg_open_table(buf,"Mgh");
					blobmsg_add_u32(buf,"mo",pxMsg->unMainOper);
					blobmsg_add_u32(buf,"so",pxMsg->unSubOper);
					blobmsg_add_u32(buf,"ow",pxMsg->unOwner);
					blobmsg_add_u32(buf,"rf",pxMsg->nFlag);
					blobmsg_close_table(buf, ptbl);
				}
				else
				{
					printf(" Your Msg May Not Be Sent Due To Overload \n");
				}
			}	
			ptbl = blobmsg_open_table(buf,GET_ATTR_OBJNAME(pxAttrObj));
			blobmsg_add_string(buf,"wn",GET_ATTR_WEBNAME(pxAttrObj)); 
			blobmsg_add_u32(buf,"fg",GET_ATTR_FLAG(pxAttrObj));
			FOR_EACH_PARAM_ATTR(pxAttrObj,pxAttrParam)
			{
				blobmsg_add_string(buf, "pn",GET_ATTR_PARAMNAME(pxAttrParam));
				blobmsg_add_string(buf, "pr", GET_ATTR_PARAMPROFILE(pxAttrParam));
				blobmsg_add_string(buf, "pw", GET_ATTR_PARAMWEBNAME(pxAttrParam));
				blobmsg_add_string(buf, "pvv", pxAttrParam->sParamValidVal);
				blobmsg_add_string(buf, "pv", GET_ATTR_PARAMVALUE(pxAttrParam));
				blobmsg_add_u32(buf, "pmiv", GET_ATTR_MINVAL(pxAttrParam));
				blobmsg_add_u32(buf, "pmav", GET_ATTR_MAXVAL(pxAttrParam));
				blobmsg_add_u32(buf, "pmil", GET_ATTR_MINLEN(pxAttrParam));
				blobmsg_add_u32(buf, "pmal", GET_ATTR_MAXLEN(pxAttrParam));
				blobmsg_add_u32(buf, "pf", GET_ATTR_PARAMFLAG(pxAttrParam));
			}
		blobmsg_close_table(buf, ptbl);
		}
	}
	else if (IS_SOPT_OBJACSATTR(pxMsg->unSubOper))
	{
		ObjACSList *pxTmpObjAcs;
		ParamACSList *pxParamAcs;

		FOR_EACH_OBJ_ACS_ATTR(pxMsg->pObjType,pxTmpObjAcs)
		{
			if (buf->buflen >= UBUS_MSG_LENGTH) 
			{
				if ( (ctx != NO_ARG_VALUE) && (req != NO_ARG_VALUE))
				{
					ubus_send_reply(ctx, req, buf->head);
				
					unMsgSize += buf->buflen;
					blob_buf_free(buf);
					memset(buf,0x0,sizeof(struct blob_buf));
					blobmsg_buf_init(buf);
					blobmsg_add_u32(buf,"objflag",pxMsg->unSubOper);
					ptbl = blobmsg_open_table(buf,"Mgh");
					blobmsg_add_u32(buf,"mo",pxMsg->unMainOper);
					blobmsg_add_u32(buf,"so",pxMsg->unSubOper);
					blobmsg_add_u32(buf,"ow",pxMsg->unOwner);
					blobmsg_add_u32(buf,"rf",pxMsg->nFlag);
					blobmsg_close_table(buf, ptbl);
				}
				else
				{
					printf(" Your Msg May Not Be Sent Due To Overload \n");
				}
			}
			ptbl = blobmsg_open_table(buf,pxTmpObjAcs->sObjName);
			blobmsg_add_u32(buf,"oo",pxTmpObjAcs->unObjOper);
			blobmsg_add_u32(buf,"of",pxTmpObjAcs->unObjFlag);
			FOR_EACH_PARAM_ACS_ATTR(pxTmpObjAcs, pxParamAcs)
			{
				blobmsg_add_string(buf, "pn",pxParamAcs->sParamName);
				blobmsg_add_string(buf, "pv", pxParamAcs->sParamValue);
				blobmsg_add_u32(buf, "pf", pxParamAcs->unParamFlag);
			}
		blobmsg_close_table(buf, ptbl);
		}
	}
	
	unMsgSize += buf->buflen;
	blobmsg_add_u32(buf,"MsgSize",unMsgSize);
	return UGW_SUCCESS;
}

/*  =============================================================================
 *   Function Name 	: help_printJsonFormat					*
 *   Description 	: Function to traverse list and dump object name,id	*
 *  ============================================================================*/
int help_getJsonFormat(IN MsgHeader *pxMsg,char **ppcBuf)
{

	ObjList *pxTmpObj;
	ObjList *pxObj;
	ParamList *pxParam;
	char *pcBuf;
	static int nIntLvl=0;
	int nParamFlag=0;
	uint32_t unSize = 0;
	static int nTopObj=0,nDevFlag=0,nCloseTag=0;
	
	pxObj = pxMsg->pObjType;

	unSize = pxMsg->unMsgSize;
	/* additional space allocation required for json msg fromat */	
	pcBuf = HELP_MALLOC((unSize*2)+65536); 

	if (pcBuf == NULL)
		return ERR_MEMORY_ALLOC_FAILED;

	sprintf(pcBuf,"%*s {\n",nIntLvl+TOP, " ");

	list_for_each_entry(pxTmpObj,&(pxObj->xOlist),xOlist) 
	{
		
		if (strcmp("Device",pxTmpObj->sObjName) == 0)
		{
			sprintf(pcBuf,"%s %*s \"%s\" : {\n",pcBuf,nIntLvl+DEV_LVL, " ",pxTmpObj->sObjName);
			nCloseTag=1;
			continue;
		}
		else if(nDevFlag==0)
		{
			sprintf(pcBuf,"%s %*s \"Objects\": [\n",pcBuf,nIntLvl+LEVEL_OBJ, " ");
			nDevFlag=1;
		}
		if ( nTopObj == 1)
		{
			sprintf(pcBuf,"%s %*s },\n",pcBuf,nIntLvl+LEVEL_OBJ_PARAM," ");
			nTopObj = 0;
		}
		sprintf(pcBuf,"%s %*s {\n",pcBuf,nIntLvl+LEVEL_OBJ_PARAM," " );
		sprintf(pcBuf,"%s %*s \"ObjName\": \"%s\", \n",pcBuf,nIntLvl+LEVEL_OBJ_PARAM_VAL, " " ,pxTmpObj->sObjName);
		/* add more attribute to obj */

		nParamFlag = 0;
		sprintf(pcBuf,"%s %*s \"Param\":[\n",pcBuf,nIntLvl+LEVEL_PARAM_TOP, " " );
		list_for_each_entry(pxParam,&(pxTmpObj->xParamList.xPlist),xPlist)
		{
			if(nParamFlag == 1){
				sprintf(pcBuf,"%s %*s },\n",pcBuf,nIntLvl+LEVEL_PARAM," ");
			}
			sprintf(pcBuf,"%s %*s {\n",pcBuf,nIntLvl+LEVEL_PARAM, " " );
			sprintf(pcBuf,"%s %*s \"ParamName\":\"%s\", \n",pcBuf,nIntLvl+LEVEL_PARAM_VAL, " " ,pxParam->sParamName);
			sprintf(pcBuf,"%s %*s \"ParamId\": \"%d\",\n",pcBuf,nIntLvl+LEVEL_PARAM_VAL, " " ,GET_PARAM_ID(pxParam));
			sprintf(pcBuf,"%s %*s \"ParamValue\": \"%s\",\n",pcBuf,nIntLvl+LEVEL_PARAM_VAL, " " ,pxParam->sParamValue);
			sprintf(pcBuf,"%s %*s \"ParamFlag\": \"0x%x\"\n",pcBuf,nIntLvl+LEVEL_PARAM_VAL, " " ,pxParam->unParamFlag);
			nParamFlag = 1;
			
		}
		nTopObj=1;
		if (nParamFlag == 1)
			sprintf(pcBuf,"%s %*s }\n",pcBuf,nIntLvl+LEVEL_PARAM," ");
			
		sprintf(pcBuf,"%s %*s ] \n",pcBuf,nIntLvl+LEVEL_PARAM_TOP, " " );
	}

	if(nTopObj == 1)
	{	
		sprintf(pcBuf,"%s %*s } \n",pcBuf,nIntLvl+LEVEL_OBJ_PARAM, " " );
		nTopObj=0;
	}
	//Device.
	sprintf(pcBuf,"%s %*s ] \n",pcBuf,nIntLvl+LEVEL_OBJ, " " );
	if(nCloseTag == 1)
	{
		sprintf(pcBuf,"%s %*s } \n",pcBuf,nIntLvl+DEV_LVL, " " );
		nCloseTag=0;
	}
   	nDevFlag=0;
	//Top Level
	sprintf(pcBuf,"%s %*s } \n",pcBuf,nIntLvl+TOP, " ");
	pcBuf[strlen(pcBuf)]='\0';
	*ppcBuf = pcBuf;
	return UGW_SUCCESS;
}

/*  =============================================================================
 *   Function Name 	: help_printMsg						*
 *   Description 	: Function to traverse list and dump object name,id	*
 *  ============================================================================*/
void help_printMsg(IN MsgHeader *pxMsg)
{
	fprintf(stderr,"{\n");
	fprintf(stderr," \t \"MsgHeader\":{\n");
	fprintf(stderr,"\t\t \"MainOper\":%ju,\n",(uintmax_t)pxMsg->unMainOper);
	fprintf(stderr,"\t\t \"SubOper\":%ju,\n",(uintmax_t)pxMsg->unSubOper);
	fprintf(stderr,"\t\t \"Owner\":%ju,\n",(uintmax_t)pxMsg->unOwner);
	fprintf(stderr,"\t\t \"ReservdFlag\":%ju\n",(uintmax_t)pxMsg->nFlag);
	fprintf(stderr,"\t },\n");
	help_printObj(pxMsg->pObjType, pxMsg->unSubOper);
	fprintf(stderr," }\n");
}

/*  =============================================================================
 *   Function Name 	: help_delMsg						*
 *   Description 	: Function to traverse list and free objects 		*
 *  ============================================================================*/
void help_delMsg(IN MsgHeader *pxMsg,bool bFlag)
{

	if (IS_OBJLIST(pxMsg->unSubOper))
	{
		ObjList *pxTempObj; 
		ObjList *pxObjList=NULL;
		ParamList *pxParamList; 

		pxObjList = pxMsg->pObjType;

		while( !list_empty(&pxObjList->xOlist) ) 
		{
			pxTempObj = list_entry(pxObjList->xOlist.next,ObjList,xOlist);
			while( !list_empty(&pxTempObj->xParamList.xPlist) ) 
			{
				pxParamList = list_entry(pxTempObj->xParamList.xPlist.next,ParamList,xPlist);
				list_del(&pxParamList->xPlist);
				free(pxParamList);
				pxParamList = NULL;
			}
			list_del(&pxTempObj->xOlist);
			free(pxTempObj);
			pxTempObj = NULL;
		}
	}
	else if (IS_SOPT_OBJATTR(pxMsg->unSubOper))
	{
		ObjAttrList *pxTempObj; 
		ObjAttrList *pxObjList=NULL;
		ParamAttrList *pxParamList; 

		pxObjList = pxMsg->pObjType;

		while( !list_empty(&pxObjList->xOlist) ) 
		{
			pxTempObj = list_entry(pxObjList->xOlist.next,ObjAttrList,xOlist);
			while( !list_empty(&pxTempObj->xParamAttrList.xPlist) ) 
			{
				pxParamList = list_entry(pxTempObj->xParamAttrList.xPlist.next,ParamAttrList,xPlist);
				list_del(&pxParamList->xPlist);
				free(pxParamList);
				pxParamList = NULL;
			}
			list_del(&pxTempObj->xOlist);
			free(pxTempObj);
			pxTempObj = NULL;
		}
	}
	if(IS_SOPT_OBJACSATTR(pxMsg->unSubOper))	
	{
		ObjACSList *pxTempAcsObj; 
		ParamACSList *pxParamAcsList; 
		ObjACSList *pxObjAcsList;

		pxObjAcsList = pxMsg->pObjType;
		while( !list_empty(&pxObjAcsList->xOlist) ) 
		{
			pxTempAcsObj = list_entry(pxObjAcsList->xOlist.next,ObjACSList,xOlist);
			while( !list_empty(&pxTempAcsObj->xParamAcsList.xPlist) ) 
			{
				pxParamAcsList = list_entry(pxTempAcsObj->xParamAcsList.xPlist.next,ParamACSList,xPlist);
				list_del(&pxParamAcsList->xPlist);
				free(pxParamAcsList);
				pxParamAcsList = NULL;
			}
			list_del(&pxTempAcsObj->xOlist);
			free(pxTempAcsObj);
			pxTempAcsObj = NULL;
		}
	}
	if(bFlag)
	{
		if (pxMsg->pObjType != NULL)
		 	 free(pxMsg->pObjType);
			 pxMsg->pObjType = NULL;
	}
}

/*  =============================================================================
 *   Function Name 	: help_ubusConnect					*
 *   Description 	: Function opens ubus socket and creates context 	*
 *   			  to connect with csd daemon				*
 *  ============================================================================*/
struct ubus_context * help_ubusConnect(void){
	const char *ubus_socket = NULL;
	static struct ubus_context *ctx;
	ctx =  ubus_connect(ubus_socket);
	if(ctx)
		return ctx;
	else 
		return NULL;
}
/*  =============================================================================
 *   Function Name 	: help_ubusDisconnect					*
 *   Description 	: Function to terminate ubus connection			*
 *  ============================================================================*/
void help_ubusDisconnect(struct ubus_context * ctx)
{
	if(ctx != NULL)
	{
		ubus_free(ctx);
		ctx = NULL;
	}
}


