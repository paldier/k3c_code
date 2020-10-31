/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/*  ***************************************************************************** 
 *         File Name    :  ugw_common.h                                         *
 *         Description  : Common function prototypes and associated data	*
 *         			structures that are used across multiple SLs	*
 *  *****************************************************************************/

/*! \file ugw_common.h
\brief File contains prototypes and associated data structures that are used across multiple SLs
*/

#ifndef _UGW_COMMON_H
#define _UGW_COMMON_H

/** \addtogroup LIBHELP */
/* @{ */

#include <stdint.h>
#include "ugw_defs.h"
#include "ugw_objmsg.h"
#include "ugw_proto.h"

#include <libubus.h>
#include <libubox/blobmsg_json.h>
#include <libubox/blobmsg.h>


#define PARAM_MAP_NAME "xParamMap"   /*!<param map name */

/*! \enum x_LTQ_Types
    \brief Enum defining various data types
*/
typedef enum {
	LTQ_PARAM_TYPE_OBJ_LIST,		/*!<Objlist type */
	LTQ_PARAM_TYPE_UINT,			/*!<Unsigned int type */
	LTQ_PARAM_TYPE_INT,			    /*!<Int type */
	LTQ_PARAM_TYPE_SHORT_INT,		/*!<Short int type */
	LTQ_PARAM_TYPE_USHORT_INT,		/*!<Ushort int type */
	LTQ_PARAM_TYPE_ULONG,			/*!<Ulong  type */
	LTQ_PARAM_TYPE_LONG,			/*!<Short int type */
	LTQ_PARAM_TYPE_CHAR,			/*!<Char type */
	LTQ_PARAM_TYPE_UCHAR,			/*!<Uchar type */
	LTQ_PARAM_TYPE_FLOAT,			/*!<Float type */	
	LTQ_PARAM_TYPE_BOOL,			/*!<Bool type */
	LTQ_PARAM_TYPE_ENUM,			/*!<ENUM type */
	LTQ_PARAM_TYPE_HEX,				/*!<HEX type */
	LTQ_PARAM_TYPE_STR,				/*!<String type */
	LTQ_PARAM_TYPE_DATETIME,		/*!<Datetime type */
	LTQ_PARAM_TYPE_STRUCT,			/*!<Struct type */
} x_LTQ_Types;

/*! 
    \brief Defines the name-value pairs used
    for string-to-macro enum mapping
*/
typedef struct {
	char name[MAX_LEN_PARAM_NAME]; /*!< Name of the  param */
	int32_t value; /*!< Value of the param */
} name_value_t;

/*! 
    \brief Defines the bit-map to set/identify parameters
    for which values are set
*/
typedef struct {
	uint32_t nParamInc; /*!< Bit-map to indicate various parameters included*/
	uint32_t nParamExc; /*!< Bit-map to indicate various parameters excluded*/
} ParamMap_t;

/*! 
    \brief Defines the parameter-offset map for each structure
*/
typedef struct {
	char acParamName[MAX_LEN_PARAM_NAME]; /*!< Name of the  param */
	uint32_t uiId; /*!<Param id */
	uint32_t uiType;/*!< Param type */
	uint32_t unOffset; /*!< Offset */
	name_value_t *nvLookUp; /*!< Look up table for enums, maximum 16 values for an enum */
	uint32_t unLookUpSize; /*!< Size of look up table for enums */
} x_LTQ_Param;
/* @} */

/** \addtogroup SYSFRAMEWORK_POLLD */
/* @{ */

/* ######################## 
	polld 
   #######################*/
/*! 
  \brief Contains the timer fields for polld
  */
typedef struct polldInfo {
	uint32_t unRequest_type;	/*!< REQTYPE_TIMER_ADD/REQTYPE_TIMER_DEL/REQTYPE_TIMER_MOD*/
	uint32_t unOccurrence;		/*!< TIMERRECURENCE_ONCE/TIMERRECURENCE_RECURRING */
	uint32_t unTimeOut;		/*!< TimeOut in sec */
	uint32_t unReferenceCount;	/*< Number of parameters associated with this timer */
	ListHead xPlist;		/*!< Traverse List */
} PolldTimeInfo;

/*!
    \brief Contains the response code for Callbacks of polld
*/
typedef struct PolldResp{
	int nRespCode;			/*!< Fault response */
} PolldRespCode;

/*! \enum RequestType
    \brief Enum containing the type of request for polld
*/
typedef enum {
	REQTYPE_TIMER_ADD = 0, /*!< Add new timer */
	REQTYPE_TIMER_DEL, /*!< Delete existing timer value */
	REQTYPE_TIMER_MOD /*!< Modify existing timer value */
} RequestType;

/*! \enum TimerRecurence
    \brief Enum containing timeout for once or repeatedly
*/
typedef enum {
	TIMERRECURENCE_ONCE = 0, /*!< Add new timer */
	TIMERRECURENCE_RECURRING /*!< recurring timer */
} TimerRecurence;

/*! 
	\brief Function to create head node for polld timer list
	\return Head node pointer on successful
*/
PolldTimeInfo *help_createTimerList(void);

/*! 
	\brief Function to delete the polld timer linked list
	\param[in] pxTimerList Head pointer of timerlinked list
	\return Head node pointer on successful
*/
void help_delTimerList(IN PolldTimeInfo **pxTimerList);

/*! 
	\brief Function to print polld timer linked list
	\param[in] pxTimerList Head pointer of timer linked list
	\return Prints timer linkedlist values on successful
*/
void help_printTimerList(IN PolldTimeInfo *pxTimerList);

/*! 
	\brief Function to add a new timer node in polld timer linked list
	\param[out] pxTimerList Head pointer of polld timer linked list
	\param[in] pxTimeInfo Polld timer structure to be added
	\return Success on successful 
*/
int help_addTimerList(OUT PolldTimeInfo *pxTimerList, IN PolldTimeInfo *pxTimeInfo);

/*! 
	\brief Function to send ubus message to polld
	\param[in] eOperation Operation such as GET 
	\param[in] pxDynMsg Message header contains objList and ParamList
	\param[out] pxOutputMsg Message header contains objList and ParamList
	\param[in] pxTimerList Struct contains timer info
	\return Success on successful
*/
int ubusMsgToPolld(IN MainOper eOperation, IN MsgHeader *pxDynMsg, OUT MsgHeader *pxOutputMsg, IN PolldTimeInfo *pxTimerList);

/*! 
	\brief Function to convert objlist and polld timers to blob message
	\param[out] buf Blob attribute structure
	\param[in] pxMsg message header contains objList and ParamList
	\param[in] ctx Pointer contains ubus context
	\param[in] req Contains ubus request data
	\param[in] pxTimerList Contains polld timer info
	\return Success on successful
*/
int help_objListToBlobPolld(OUT struct blob_buf *buf, IN MsgHeader *pxMsg, IN struct ubus_context *ctx, IN struct ubus_request_data *req, IN PolldTimeInfo *pxTimerList);

/*! 
	\brief Function to convert blob message to objList and polld timers
	\param[in] msg Contains blob attribute
	\param[out] pxMsg Message header contains objList and ParamList
	\param[out] pxTimerInfo Contains polld time info
	\return Success on successful
*/
int help_blobToObjListPolld(IN struct blob_attr *msg, OUT MsgHeader *pxMsg, OUT PolldTimeInfo *pxTimerInfo);

/*! 
        \brief Macro used to loop through timerlist
        \param[in] __pxSrc Pointer to pxTimerList  
        \param[in] __pxParam Pointer pxTimerTmp, used to copy the actual pxTimerList values
*/
#define  FOR_EACH_TIMER_ONLY(__pxSrc,__pxParam)\
        list_for_each_entry(__pxParam,&(__pxSrc->xPlist),xPlist) \

/*!
	\brief Free up the timer linked list
	\param[in] __pxNode Pointer to free
*/
#define DELETE_TIMER_NODE(__pxNode) do { LOGF_LOG_INFO("Deleting : %p Timer Node\n", pxTimerTmp); list_del(&__pxNode->xPlist); HELP_FREE(__pxNode); } while(0);
/* @} */

/** \addtogroup LIBHELP */
/* @{ */
/*! \enum ParamType
    \brief Enum containing data types supported to construct blob message
*/
typedef enum {
	PARAM_TYPE_INT32,
	PARAM_TYPE_STRING
} ParamType;

/*!  \brief  API for converting string format of enum to integer and vice-versa
  \param[in] enum_nv Enumeration look-up having string to integer mapping
  \param[in] iSize Size of enum_nv
  \param[out] trVal Corresponding string value for enumeration value in uiId
  \param[out] uiId Corresponding integer value for enumeration value in trVal
  \param[in] iSel Select string to integer or integer to string lookup
*/
int32_t enumMapGet(name_value_t enum_nv[], int32_t iSize, char *trVal, int32_t *uiId, int32_t iSel);

/*!  \brief  API for converting name-value pairs coming from servd into C structure
  \param[in] pxObj Pointer to name-value pair list
  \param[out] pxStruct Pointer to structure which will be filled with corresponding values from pxObj
  \param[in] xParamDetail Parameter-offset map for specified structure
  \param[in] iSize Number of map entries in xParamDetail
  \return UGW_SUCCESS on successful / UGW_FAILURE on failure
  */
int32_t help_objlistToStructConv(ObjList *pxObj, void *pxStruct, x_LTQ_Param xParamDetail[], int32_t iSize);

/*!  \brief  API for converting name-value pairs coming from servd into C structure
  \param[in] pxObj Pointer to name-value pair list
  \param[out] pxStruct Pointer to structure which will be filled with corresponding values from pxObj
  \param[in] xParamDetail Parameter-offset map for specified structure
  \param[in] iSize Number of map entries in xParamDetail
  \return UGW_SUCCESS / UGW_FAILURE
  */
int32_t help_structToObjlistConv(ObjList *pxObj, void *pxStruct, x_LTQ_Param xParamDetail[], int32_t iSize);

/*!  \brief  API for Updating the Oject- params values coming from servd with the corresponding value from 
      C structure
  \param[in] pxObj Pointer to name-value pair list which will be filled with corresponding 
   values from pxStruct
  \param[out] pxStruct Pointer to structure which holds the fapi values
  \param[in] xParamDetail Parameter-offset map for specified structure
  \return UGW_SUCCESS / UGW_FAILURE
  */
int32_t help_structToObjlistMapping(ObjList *pxTmpObj, void *pxStruct, x_LTQ_Param xParamDetail[]);
/*!  \brief  Function to perform strdup functionality 
  \param[in] pcInStr Copy string
  \return Strdup String on successful / NULL on failure
  */
OUT char *  HELP_STRDUP(IN char * pcInStr);

/*!  \brief  Function to free strdup allocated memory
  \param[in] pcInStr Free pointer
  \return UGW_SUCCESS on successful / UGW_FAILURE on failure
  */
OUT int32_t  HELP_STRDUP_FREE(IN char * pcInStr);

/*!  \brief  Function to perform safe strncpy
  \param[in] pcDest  Pointer to destination buffer
  \param[in] pcSrc   Pointer to source buffer
  \param[in] nDestSize  Size of destination
  \return Pointer to destination string on successful / NULL on failure
  */
char* HELP_STRNCPY(INOUT char* pcDest, IN const char* pcSrc, IN size_t nDestSize);


/*!  \brief  Function to perform safe strncat
  \param[in] pcDest Pointer to destination buffer
  \param[in] pcSrc  Pointer to source buffer
  \param[in] nDestSize Size of destination
  \return Pointer to destination string on successful / NULL on failure
  */
char* HELP_STRNCAT(INOUT char* pcDest, IN const char* pcSrc, IN size_t nDestSize);


/*!  \brief  Function for string comparision
  \param[in] pcStr1 String 1 for comparision
  \param[in] pcStr2 String 2 for comparision
  \return UGW_SUCCESS on successful / different -ve values on failure 
  */
int HELP_STRCMP(IN const char* pcStr1, IN const char* pcStr2);


/*!  \brief  Function for finding a character in a string
  \param[in] pcStr Input string
  \param[in] nChar Character to be searched for in the string
  \return Pointer to character on successful / NULL on failure
  */
char* HELP_STRCHR(INOUT char* pcStr, int nChar);


/*!  \brief  Function to search for substring in a string
  \param[in] pcHayStack Big string
  \param[in] pcNeedle   Sub string
  \return Pointer to substring on successful / NULL on failure
  */
char* HELP_STRSTR(INOUT char* pcHayStack, IN char* pcNeedle);


/*!  \brief  Function to tokenize a string
  \param[in] pcStr Input string with tokens
  \param[in] pcDelimiters List of tokens
  \param[in] ppcSavePtr  Pointer to char* where pointer to next token is stored for internal use by the function
  \return Pointer to next token on successful / NULL on failure
  */
char* HELP_STRTOK_R(IN char* pcStr, const char* pcDelimiters, char** ppcSavePtr);
/*!  \brief  Function to send the ubus message to Ubus registered daemons
  \param[IN] pxBlob Pointer to the blob message
  \param[IN] psUbusObject Name of the ubus registered daemon
  \param[IN] psUbusMethod Name of the method to be handled in the ubus registered daemon(psUbusObject)
  \return UGW_SUCCESS on successful / UGW_FAILURE on failure
 */
OUT int32_t help_sendUbusMessage (IN struct blob_buf *pxBlob, IN char *psUbusObject, IN char *psUbusMethod);

int32_t help_constructBlobMessageFromParamList(OUT struct blob_buf * pxBlob, IN ParamList * pxParamList);

int32_t help_constructParamListFromBlob(IN struct blob_attr *pxBlob, OUT ParamList *pxParamList);

OUT static inline void  HELP_ONLY_PARAM_SET_WITH_TYPE(IN void *pxParam, IN const char *pcParamName, IN const char *pcParamValue, IN uint32_t unParamFlag)
{
        help_paramListOnly (pxParam, pcParamName, NO_ARG_VALUE, pcParamValue, unParamFlag);
}

OUT static inline int32_t HELP_SEND_UBUS_MESSAGE(IN struct blob_buf *pxBlob, IN char *psUbusObject, IN char *psUbusMethod)
{
        return help_sendUbusMessage (pxBlob, psUbusObject, psUbusMethod);
}

OUT static inline int32_t HELP_CONSTRUCT_BLOB_FROM_PARAMLIST (OUT struct blob_buf *pxBlob, IN ParamList *pxParamList)
{
        return help_constructBlobMessageFromParamList(pxBlob, pxParamList);
}
OUT static inline int32_t HELP_CONSTRUCT_PARAMLIST_FROM_BLOB (IN struct blob_attr *pxBlob, OUT ParamList *pxParamList)
{
        return help_constructParamListFromBlob(pxBlob, pxParamList);
}

OUT static inline int32_t HELP_CONSTRUCT_SERVD_BLOB_FROM_NOTIFY (OUT struct blob_buf *pxBlob, IN NotifyObjData *pxNotify)
{
        return help_notifyListToBlob(pxBlob, pxNotify);
}

OUT static inline int32_t HELP_CONSTRUCT_SERVD_NOTIFY_FROM_BLOB (IN struct blob_attr *pxBlob, OUT NotifyObjData *pxNotify)
{
        return help_blobToNotifyList(pxBlob, pxNotify);
}

/* ######################## 
        wsd 
   #######################*/
/*! \enum ActionType
    \brief Enum containing the actions sent from web to wsd forwarded to servd
*/
typedef enum {
	ACTION_STOP = 0,	/*!< Stop polling operation for servd*/
	ACTION_START = 1,	/*!< Start polling operation for servd*/
} ActionType;

/*!
  \brief Contains the websocket data with Msg Header
*/
typedef struct {
	uint32_t  nWSProto;                     /*!< WebSocket Protocol ID */
        ActionType eWsdAction;                  /*!< START/STOP */
        uint32_t nTimer;                        /*!< Timer value in seconds */
        uint32_t nInstance;                    	/*!< Instance of the WSD connection */
        MsgHeader xMsgHdr;                     	/*!< Message Header */
} WsdData;

/*! 	\brief Function to covert websocket data to blobmsg
        \param[in] buf Buffer to fill notify struct
        \param[in] pxWsdData Notification Object list 
        \return Buf filled with WsdData struct on successful
*/
int32_t help_WsdDataToBlob(struct blob_buf *buf, WsdData *pxWsdData);

/*! 	\brief  Function to covert blob msg to websocket data
        \param[in] msg Blob msg
        \param[in] pxWsdData Notification list
        \return WsdData structure is filled on successful
*/
int32_t help_blobToWsdData(struct blob_attr *msg, WsdData *pxWsdData);

/*!     \brief  Function to send status messages through ubus to wsd and web
        \param[in] unNotifyId Notify id to identify wsd proto
        \param[in] ucType Sync/Async notification
        \param[in] pcMessage Pointer to the messages string
        \return on UGW_SUCCESS on successful / UGW_FAILURE on failure
*/
int32_t help_sendStatusToWeb(uint32_t unNotifyId, unsigned char ucType, char *pcMessage);

/* @} */

#endif // #ifndef _UGW_COMMON_H
