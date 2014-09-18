<%@page language="java" contentType="text/html;charset=gb2312"%>
<%@page import="com.afunms.automation.model.NetCfgFileNode"%>
<%@ include file="/automation/common/globe.inc"%>
<%@ include file="/automation/common/globeChinese.inc"%>

<%@page import="java.util.List"%>
<%
	String rootPath = request.getContextPath();
	List list = (List) request.getAttribute("list");
%>
<html>
	<head>

		<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
		<script type="text/javascript" src="<%=rootPath%>/automation/js/swfobject.js"></script>
		<script language="JavaScript" type="text/javascript" src="<%=rootPath%>/automation/js/navbar.js"></script>
		<link href="<%=rootPath%>/resource/<%=com.afunms.common.util.CommonAppUtil.getSkinPath() %>css/global/global.css"
			rel="stylesheet" type="text/css" />
		<script type="text/javascript" src="<%=rootPath%>/automation/js/page.js"></script>
		<script language="JavaScript" src="<%=rootPath%>/automation/js/date.js"></script>

		<link rel="stylesheet" type="text/css" href="<%=rootPath%>/automation/css/ext/ext-all.css" charset="gb2312" />
		<script type="text/javascript" src="<%=rootPath%>/automation/js/ext/ext-base.js" charset="gb2312"></script>
		<script type="text/javascript" src="<%=rootPath%>/automation/js/ext/ext-all.js" charset="gb2312"></script>
		<script type="text/javascript" src="<%=rootPath%>/automation/js/ext/ext-lang-zh_CN.js" charset="utf-8"></script>
        <script type="text/javascript" src="<%=rootPath%>/automation/js/jquery-1.8.2.min.js"></script>


		<script language="JavaScript" type="text/JavaScript">
  Ext.onReady(function()
{  
$('#fragment-1').hide();
$('#fragment-2').hide();

setTimeout(function(){
	        Ext.get('loading').remove();
	        Ext.get('loading-mask').fadeOut({remove:true});
	    }, 250);
}

);

function CreateWindow(url)
{
msgWindow=window.open(url,"protypeWindow","toolbar=no,width=400,height=400,directories=no,status=no,scrollbars=no,menubar=no")
}   

</script>

		<script language="JavaScript" type="text/JavaScript">

function setClass(){
	document.getElementById('scriptTitle-0').className='detail-data-title';
	
}
function firstFra(){
    document.getElementById('scriptTitle-0').className='detail-data-title';
	document.getElementById('scriptTitle-1').className='detail-data-title-out';
	document.getElementById('scriptTitle-2').className='detail-data-title-out';
	
	$('#fragment-0').show();
	$('#fragment-1').hide();
	$('#fragment-2').hide();
}

function secondFra(){

   var command=$('#commands').val();
   if(command==null||command==""){
    alert("�������Ϊ�գ�");
    return;
   }
    document.getElementById('scriptTitle-0').className='detail-data-title-out';
	document.getElementById('scriptTitle-1').className='detail-data-title';
	document.getElementById('scriptTitle-2').className='detail-data-title-out';
	
	$('#fragment-0').hide();
	$('#fragment-1').show();
	$('#fragment-2').hide();
}

 function exeCmd(){
 var ip=$("[name='checkbox'][checked]").val();
 if(ip==undefined||ip==""){
 alert("��ѡ���豸IP!!!");
 return;
 }
  Ext.MessageBox.wait('���ݼ����У����Ժ�.. '); 
   mainForm.action="<%=rootPath%>/autoControl.do?action=exeCmd";
	mainForm.submit();
 }
function refer(action){
		var mainForm = document.getElementById("mainForm");
		mainForm.action = '<%=rootPath%>' + action;
		mainForm.submit();
}
function loadFile(){
window.open("<%=rootPath%>/autoControl.do?action=loadFile","oneping", "height=500, width= 800, top=100, left=100,scrollbars=yes");		
}
function saveFile(){
var commands=$("#commands").text();
   if(commands==null||commands==""){
    alert("�������Ϊ�գ�");
    return;
   }
reg = new RegExp("(\r)","g");
var value=commands.replace(reg,";;") 
window.open("<%=rootPath%>/autoControl.do?action=saveFile&&commands="+value,"oneping", "height=300, width= 500, top=100, left=100,scrollbars=yes");
				}
</script>


	</head>
	<body id="body" class="body" onload="setClass()">
		<IFRAME frameBorder=0 id=CalFrame marginHeight=0 marginWidth=0
			noResize scrolling=no src="<%=rootPath%>/include/calendar.htm"
			style="DISPLAY: none; HEIGHT: 189px; POSITION: absolute; WIDTH: 148px; Z-INDEX: 100"></IFRAME>
		<form id="mainForm" method="post" name="mainForm">
			<div id="loading">
				<div class="loading-indicator">
					<img src="<%=rootPath%>/js/ext/lib/resources/extanim64.gif"
						width="32" height="32" style="margin-right: 8px;" align="middle" />
					Loading...
				</div>
			</div>
			<div id="loading-mask" style=""></div>

			<table id="body-container" class="body-container">
				<tr>
					<td class="td-container-main">

						<table id="container-main" class="container-main">

							<tr>
								<td class="td-container-main-report">
									<table id="container-main-report" class="container-main-report">
										<tr>
											<td>

												<table id="report-content" class="report-content">
													<tr>
														<td>
															<table width="100%"
																background="<%=rootPath%>/common/images/right_t_02.jpg"
																cellspacing="0" cellpadding="0">
																<tr>
																	<td align="left">
																		<div class="noPrint">
																			<img src="<%=rootPath%>/common/images/right_t_01.jpg"
																				width="5" height="29" />
																		</div>
																	</td>
																	<td class="layout_title">
																		<b>�Զ��� >> �Զ������� >> ִ�нű�����</b>
																	</td>
																	<td align="right">
																		<div class="noPrint">
																			<img src="<%=rootPath%>/common/images/right_t_03.jpg"
																				width="5" height="29" />
																		</div>
																	</td>
																</tr>
															</table>
														</td>

													</tr>
													<tr>
														<td>
															<table id="report-content-header"
																class="report-content-header">
																<tr>
																	<td>
																		<%=scriptTitleTable%>
																	</td>
																</tr>
															</table>
														</td>
													</tr>
													<tr>
														<td>
															<table id="report-content-body"
																class="report-content-body">
																<tr>
																	<td align=center>

																		<table id="report-data-header"
																			class="report-data-header">
																			<tr>
																				<td>

																					<table id="report-data-header-title"
																						class="report-data-header-title">
																						<tr>
																							<td>
																								&nbsp; <font color="#DC143C">ִ�в��裺1.�������� >> 2.ѡ���豸 >> 3.ִ�н��</font>
																							</td>
																						</tr>
																					</table>
																				</td>
																			</tr>
																		</table>

																	</td>
																</tr>

																<tr>
																	<td>
																		<div id="fragment-0" >
																			<table height="350">
																			<tr>
																					<td>
																					&nbsp;
																					</td>
																					<td>
																					&nbsp;	
																					</td>
																				</tr>
																				<tr>
																					<td width="15%">
																						&nbsp;&nbsp;�Ƿ��з��ؽ����
																					</td>
																					<td width="85%">
																						<select name="isReturn" id="isReturn">
																							<option value="0">
																								��
																							</option>
																							<option value="1">
																								��
																							</option>
																						</select>
																					</td>
																				</tr>
																<tr>
																    <td ></td>
																	<td align=center width="85%">

																		<table id="report-data-header"
																			class="report-data-header">
																			<tr>
																				<td>

																					<table id="report-data-header-title"
																						class="report-data-header-title">
																						<tr>
																						   
																							<td>
																								
																								&nbsp;<a href="#" onclick="loadFile()"><img src="<%=rootPath %>/resource/image/menu/load.gif"/>�����ļ�</a>
																								&nbsp;<a href="#" onclick="saveFile()"><img src="<%=rootPath %>/resource/image/menu/save.gif"/>�����ļ�</a>
																							</td>
																						</tr>
																					</table>
																				</td>
																			</tr>
																		</table>

																	</td>
																</tr>
																				<tr valign=top>
																					
																					<td width="15%">
																						&nbsp;&nbsp;������ű����
																					</td>
																					<td width="85%">
																						<textarea id="commands" name="commands" rows="10"
																							cols="85"></textarea>

																					</td>
																				</tr>

																				<tr>

																					<td colspan=2 align=center>
																						<br>
																						<input type="button" value="��һ��"
																							onclick="secondFra()">
																					</td>
																				</tr>
																				<tr>

																					<td colspan=2 align=center>
																						<br>
																						&nbsp;
																						<br><br><br><br>
																					</td>
																				</tr>
																			</table>

																		</div>
																	</td>
																</tr>
																<tr>
																	<td>

																		<div id="fragment-1">
																			<table cellspacing="0" border="1"
																				bordercolor="#ababab">
																				<tr height=28 style="background: #ECECEC"
																					align="center" class="content-title">
																					<td align="center">
																						<INPUT type="checkbox" name="checkall"
																							onclick="javascript:chkall()">

																					</td>
																					<td align="center">
																						���
																					</td>
																					<td align="center">
																						IP��ַ
																					</td>

																					<td align="center">
																						�豸����
																					</td>

																				</tr>
																				<%
																					NetCfgFileNode vo = null;
																					for (int i = 0; i < list.size(); i++) {
																						vo = (NetCfgFileNode) list.get(i);
																				%>
																				<tr <%=onmouseoverstyle%>>

																					<td align='center'>
																						<input type="checkbox" name='checkbox'
																							id='checkbox' value="<%=vo.getIpaddress()%>" />
																					</td>
																					<td align='center'>
																						<%=i + 1%>
																					</td>
																					<td align='center'>
																						<%=vo.getIpaddress()%>
																					</td>

																					<td align='center'><%=vo.getDeviceRender()%></td>

																				</tr>
																				<%
																					}
																				%>
																				<tr style="background-color: #ECECEC;">
																					<TD nowrap colspan="7" align=center>
																						<br>
																						<input type="button" style="width: 50" value="��һ��"
																							onclick="firstFra()">

																						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
																						<input type="button" value="ִ ��" style="width: 50" onclick="exeCmd()">


																					</TD>

																				</tr>


																			</table>

																		</div>

																		<div id="fragment-2">
																			<table>
																				<tr>
																					<td>
																						ִ�н����
																					</td>
																					<td>
																						&nbsp;
																						<textarea id="content" name="content" rows="32"
																							cols="140"></textarea>
																					</td>
																				</tr>
																				<tr>
																					<td colspan=2 align=center>

																						<input type="button" value="��һ��"
																							onclick="secondFra()">
																					</td>
																				</tr>
																			</table>

																		</div>

																	</td>
																</tr>

															</table>
														</td>
													</tr>

												</table>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>

		</form>
	</BODY>
</HTML>