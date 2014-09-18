<%@page language="java" contentType="text/html;charset=GB2312"%>
<%@ include file="/automation/common/globe.inc"%>
<%@page import="java.util.List"%>
<%@page import="com.afunms.automation.model.NetCfgFile"%>


<%
	String rootPath = request.getContextPath();
	List list = (List) request.getAttribute("list");
	String ip = (String) request.getAttribute("ip");
	String runImg = rootPath + "/automation/images/Config-Running.gif";
	String startupImg = rootPath + "/automation/images/Config-Startup.gif";
	String baselineImg = rootPath + "/automation/images/Config-Baseline.gif";
	String type = (String) request.getAttribute("type");
	
%>


<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
	
		<script  type="text/javascript" src="<%=rootPath%>/automation/js/navbar.js"></script>
		<link href="<%=rootPath%>/resource/<%=com.afunms.common.util.CommonAppUtil.getSkinPath() %>css/global/global.css" rel="stylesheet"
			type="text/css" />
		<LINK href="<%=rootPath%>/automation/css/style.css" type="text/css" rel="stylesheet">
		<script type="text/javascript" src="<%=rootPath%>/automation/js/page.js"></script>
		<script type="text/javascript" src="<%=rootPath%>/automation/js/menu.js"></script>
		<script type="text/javascript" src="<%=rootPath%>/automation/js/jquery-1.4.4.min.js"></script>
		<script type="text/javascript">
			var listAction = "<%=rootPath%>/netCfgFile.do?action=configlist";
	  		var delAction = "<%=rootPath%>/netCfgFile.do?action=delete";
			
	  		function toDelete(){  
     				mainForm.action = "<%=rootPath%>/netCfgFile.do?action=deleteFile";
     				mainForm.submit();
	  		}
	  		function compareFile(){  
     				var filename=$("input[name=baskcheckbox2][checked]").val();
     				
     				
                  var checkboxs = document.getElementsByName("baskcheckbox");
					var num=0;
					var strs="";
					
					for(var i=0;i<checkboxs.length;i++){
						if(checkboxs[i].checked){
							num++;
							strs+=$(checkboxs[i]).val()+";";  

					 	}
					}
					if(num!=1){
					alert("��ѡ��һ���ȶ��ļ���");
					return;
					}

	  
     			window.open("<%=rootPath%>/netCfgFile.do?action=compareContent&filename="+filename +"&baskcheckbox="+strs ,"_blank", "height=690, width=1240, top=20, left=10,resizable=yes");
			
	  		}
	  		
	  		function setBaseLine(id,ip)
			{
			 if(confirm("��ȷ�����ô˻����ļ���")) {
			    mainForm.action = "<%=rootPath%>/netCfgFile.do?action=setBaseLine&id="+id+"&ipaddress="+ip;
			     mainForm.submit();
			}
	  		}
		</script>
	</head>
	<body id="body" class="body">
		
		<form id="mainForm" method="post" name="mainForm">
			<table id="body-container" class="body-container">
				<tr>
					
					<td class="td-container-main">
						<table id="container-main" class="container-main">
							<tr>
								<td class="td-container-main-content">
									<table id="container-main-content"
										class="container-main-content">
										<tr>
											<td>
												<table id="content-header" class="content-header">
													<tr>
														<td align="left" width="5">
															<img src="<%=rootPath%>/common/images/right_t_01.jpg"
																width="5" height="29" />
														</td>
														<td class="content-title">
															�Զ��� >> �����ļ����� >> ��ʱ�����б�
														</td>
														<td align="right">
															<img src="<%=rootPath%>/common/images/right_t_03.jpg"
																width="5" height="29" />
														</td>
													</tr>
												</table>
											</td>
										</tr>
										<tr>
											<td>
												<table id="content-body" class="content-body">
													<tr>
														<td>
															<table width="100%" cellpadding="0" cellspacing="1">
																<tr>
																	<td bgcolor="#ECECEC" width="60%">
																		&nbsp;&nbsp;
																		<!-- ip��ַ��<input type="text" size="30" name="ipfindaddress">
																		<input type="button" value="��ѯ" onclick="toFindIp()">-->
																		<a href="#" onclick="toDelete()">ɾ��</a>&nbsp;
																		<input type="hidden" id="ip" name="ip"
																			value="<%=ip%>">
																		<input type="hidden" id="type" name="type"
																			value="<%=type%>">
																	</td>

																	<td bgcolor="#ECECEC" width="40%" align='right'>
																		<a href="#" onclick="compareFile()">�����ļ��ȶ�</a>&nbsp;&nbsp;

																	</td>
																</tr>
															</table>
														</td>
													</tr>

													<tr>
														<td>
															<table cellspacing="0" border="1" bordercolor="#ababab">
																<tr height=28 style="background: #ECECEC" align="center"
																	class="content-title">
																	<td align="center">
																		<INPUT type="checkbox" id="checkall" name="checkall"
																			onclick="javascript:chkall()" class=noborder>
																	</td>
																	<td align="center">
																		���
																	</td>
																	<td align="center" width="20%">
																		IP��ַ
																	</td>
																	<td align="center">
																		�豸����
																	</td>
																	<td align="center">
																		����ʱ��
																	</td>

																	<td align="center" >
																		��������
																	</td>
																	<td align="center" >
																		�ȶ��ļ�
																	</td>
																	<td align="center">
																		����
																	</td>

																</tr>
																<%
																NetCfgFile vo = null;
																	//int startRow = jp.getStartRow();
																	if (list != null) {
																		String checked = "";
																		boolean flag=true;
																		for (int i = 0; i < list.size(); i++) {
																			
																			vo = (NetCfgFile) list.get(i);
																%>
																<tr <%=onmouseoverstyle%>>
																	<td align='center'>
																		<INPUT type="checkbox" class=noborder name="checkbox"
																			value="<%=vo.getId()%>">
																	</td>
																	<td align='center'>
																		<font color='blue'><%=i + 1%></font>
																	</td>
																	<td align='center'>
																		<%=vo.getIpaddress()%>
																	</td>
																	<td align='center'><%=type%></td>
																	<td align='center'><%=vo.getBackupTime()%></td>
																	<%
																		String bkpType = vo.getBkpType();
																				String bkpTypeName = "";
																				String bkpImg = "";
																				if (bkpType.equals("run")) {
																					bkpTypeName = "���������ļ�";
																					bkpImg = "<img src='" + runImg + "'>";
																				}
																				if (bkpType.equals("startup")) {
																					bkpTypeName = "���������ļ�";
																					bkpImg = "<img src='" + startupImg + "'>";
																				}
																				if (bkpType.equals("all")) {
																					bkpTypeName = "ȫ������";
																				}
																				if (vo.getBaseline() == 1) {
																					bkpTypeName = "���߱����ļ�";
																					bkpImg = "<img src='" + baselineImg + "'>";

																				}
																	%>
																	<td align='center'><%=bkpImg%><%=bkpTypeName%></td>
																	<%
																		String baseline = "";
																				if (vo.getBaseline() == 1) {
																					
																				  
																					
																					baseline = "<span ><font color='#33CC00'>�����ļ�</font></span>";
																					
																	%>
																	<input type="hidden" name="Baskfilename" value="<%=vo.getFileName()%> "/>
																	<td align='center' >
																		
																	<input type="checkbox" class=noborder
																			value="<%=vo.getFileName()%>" id ="baskcheckbox2" name="baskcheckbox2" 
																			checked = "checked"; disabled="disabled" >	
																	</td>
																	<td align='center' id="<%=vo.getId()%>">
																	
																		<%=baseline%></td>
																	<%
																		} else {
																					baseline = "<span ><font color='blue'>���û���</font></span>";
																					if(flag){
																					checked = "checked";
																					flag=false;
																					}else{
																					checked="";
																					}
																	%>
																	<td align='center'>
																	
																		<input type="checkbox" class=noborder
																			value="<%=vo.getFileName()%>" id ="baskcheckbox" name="baskcheckbox" 
																			<%=checked%>>
																		
																	</td>
																	<td align='center' id="<%=vo.getId()%>">
																		
																		<a href="#" onclick="setBaseLine('<%=vo.getId()%>','<%=vo.getIpaddress() %>')"><%=baseline%></a>
																	</td>
																	
																	<%
																		}
																	%>
																</tr>
																<%
																	}
																	}
																%>
															</table>
														</td>
													</tr>
												</table>
											</td>
										</tr>
										<tr>
											<td>
												<table id="content-footer" class="content-footer">
													<tr>
														<td>
															<table width="100%" border="0" cellspacing="0"
																cellpadding="0">
																<tr>
																	<td align="left" valign="bottom">
																		<img src="<%=rootPath%>/common/images/right_b_01.jpg"
																			width="5" height="12" />
																	</td>
																	<td></td>
																	<td align="right" valign="bottom">
																		<img src="<%=rootPath%>/common/images/right_b_03.jpg"
																			width="5" height="12" />
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
	</body>
</html>
