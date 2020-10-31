
#********************************************************************************
#
#  Copyright (c) 2015
#  Lantiq Beteiligungs-GmbH & Co. KG
#  Lilienthalstrasse 15, 85579 Neubiberg, Germany 
#  For licensing information, see the file 'LICENSE' in the root folder of
#  this software module.
#
#********************************************************************************/

import re
from openpyxl.comments import Comment


def insertInExcel(ws,PATH_ROW,PATH_COLUMN,watermarkText):
	if watermarkText != 'No watermark':
		#print watermarkText
		access = re.search('AccessType:(.+)',watermarkText).group(1)
		fileType = re.search('FileType:(.+)',watermarkText).group(1)
		excelWM = fileType+","+access
		
	else:
		excelWM = watermarkText
	
	
	print excelWM
	commentWrite = Comment(excelWM, "Author")
	ws.cell(row=PATH_ROW,column=PATH_COLUMN).comment = commentWrite