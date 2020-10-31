/*****************************************************************************

  Copyright (c) 2015
  Lantiq Beteiligungs-GmbH & Co. KG
  Lilienthalstrasse 15, 85579 Neubiberg, Germany 

  For licensing information, see the file 'LICENSE' in the root folder of
  this software module.

 ******************************************************************************/

/***************************************************************************** *
 *     File Name  : cal.h                                                      *
 *     Description: cal - common adaptation layer exposes APIs that can be     *
 *                  used to get or set values in the database.                 *
 *                  CAL APIs are called from various management entities and   *
 *                  or service libraries to interface with the system and      * 
 *                  the database.                                              *
 *                                                                             *
 ******************************************************************************/

/*! \file cal.h
    \brief File contains structure and function definitions for CAL functionalities.
    CAL provides a set of APIs that allow the get/set of data from the database for different modules across the system
*/
/** \addtogroup LIBCAL */
/* @{ */
#ifndef _CAL_H
#define _CAL_H

#include <ugw_objmsg.h>
#include <ugw_proto.h>
#include <capi.h>


/*! \brief  API to get the values of parameters in the given object, their SID and OID from the DB and real time statistics from the system
        \param[in,out] pxObj Get the parameter values 
        \note Parameter Value: To get the values, fill the MsgHeader->unSubOper=SOPT_OBJVALUE, MsgHeader->pObjType as type ObjList and fill the expected value names as Input
              \n The Output value returned is the ObjList with values filled for the requested parameters
        \note Parameter Attributes: To get the values, fill the MsgHeader->unSubOper=SOPT_OBJATTR, MsgHeader->pObjType as type ObjAttrList and fill the expected value names as Input
              \n The Output value is ObjAttrList with validation attribute values filled for the requested parameters
        \return UGW_SUCCESS on success / ERROR Code on failure
*/
int cal_getValue(INOUT MsgHeader * pxObj);

/*! \brief  API to set the value(s) for parameter(s) in one or multiple objects in the System
        \param[in,out] pxObjList Sets the Name, Id and values for the list of objects and parameters to be set
        \note For Input, fill the MsgHeader->unSubOper=SOPT_OBJVALUE, MsgHeader->pObjType as type ObjList and fill in the name value pairs to be set.
              \n The Output MsgHeader is filled only if the SET operation fails. 
                 The ObjList contains the list of failed object/parameter and eror string in the parameter name and value fields respectively.
        \return UGW_SUCCESS on success / ERROR Code on failure
*/
int cal_setValue(INOUT MsgHeader * pxObjList);

/*! \brief  API to get the values of parameters in an object, their OID, SID
            and dynamic attribute information from the DB only
        \param[in,out] pxObj Gets the list of objects and parameters with name of values that need to be fetched
        \return UGW_SUCCESS on success / ERROR Code on failure
*/
int cal_getValueFromDb(INOUT MsgHeader * pxObj);

/*! \brief  API to set the value(s) for parameter(s) in one or multiple objects to the DB
        \param[in] pxObjList Sets the Name, Id and values for the desired list of objects and parameters
        \return UGW_SUCCESS on success / ERROR Code on failure
*/
int cal_setValueToDb(IN MsgHeader * pxObjList);

/*! \brief  API to fetch the values of parameters from SL and set it to DB.
        \note   If the object contains no parameters, then all the parameters of that object will be fetched
        \param[in,out] pxMsg Fetches parameter values and sets it to DB.
        \note For Input, fill the MsgHeader->unSubOper=SOPT_OBJVALUE, MsgHeader->pObjType as type ObjList and fill the expected value names
              \n The Output MsgHeader is filled only if the SET to DB fails. 
                 The ObjList contains the list of failed object/parameter and eror string in the parameter name and value fields respectively.
        \return UGW_SUCCESS on success / UGW_FAILURE on failure
*/
int cal_updatePeriodicValues(INOUT MsgHeader * pxMsg);

/*! 
    \brief API to send the object(s) and timer(s) to polld
        \param[in] pxMsg Message header contains objList and ParamList
        \param[in] pPolldTime Contains polld timer info
        \return UGW_SUCCESS on success / UGW_FAILURE on failure
*/
int cal_sendMsgToPolld(IN MsgHeader * pxMsg, IN PolldTimeInfo * pPolldTime);

#endif /*_CAL_H*/
/* @} */
