/********************************************************************************
 
  	Copyright (c) 2015
	Lantiq Beteiligungs-GmbH & Co. KG
        Lilienthalstrasse 15, 85579 Neubiberg, Germany 
	For licensing information, see the file 'LICENSE' in the root folder of
        this software module.
 
********************************************************************************/

/*  ***************************************************************************** 
 *         File Name    : ugw_objmsg.h                                          *	
 *         Description  : Common Helper Library contains functions, 		*
 *			  defines, structs, and enums used across the modules 	*
 *			  like CAL, CAPI, CSD, Servd, and Management Entities 	*	
 *  *****************************************************************************/


/*! \file ugw_objmsg.h
\brief File contains the Constants, enumerations, related Data
    structures and API's common for all modules in LQ software.
*/

/** \addtogroup LIBHELP */
/* @{ */

#ifndef _UGW_OBJMSG_H
#define _UGW_OBJMSG_H

#include <stdio.h>
#include <string.h>
#include <inttypes.h>

#include <help_objlist.h>
#include "ugw_proto.h"
#include "ugw_common.h"


static inline void  HELP_CREATE_MSG(OUT MsgHeader *MsgHdr, IN uint32_t unMainOper, IN uint32_t unSubOper, IN uint32_t unOwner, IN uint32_t unFlag)
{
        memset(MsgHdr,0x0,sizeof(MsgHeader));	
	
	MsgHdr->unMainOper =  unMainOper; 
	MsgHdr->unSubOper = unSubOper; 
	MsgHdr->unOwner = unOwner; 
	MsgHdr->nFlag = unFlag;
	MsgHdr->pObjType = HELP_CREATE_OBJ(unSubOper);
}

#endif //_UGW_OBJMSG_H 
/* @} */
