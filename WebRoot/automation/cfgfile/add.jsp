<%@page language="java" contentType="text/html;charset=gb2312"%>

<%
	String rootPath = request.getContextPath();
%>
<html>
	<head>
    
		<meta http-equiv="Content-Type" content="text/html; charset=gb2312">
		
		<script type="text/javascript" src="<%=rootPath%>/automation/js/swfobject.js"></script>
		<script  type="text/javascript" src="<%=rootPath%>/automation/js/navbar.js"></script>
		<script type="text/javascript" src="<%=rootPath%>/automation/js/wfm.js"></script>
		
		<link href="<%=rootPath%>/resource/<%=com.afunms.common.util.CommonAppUtil.getSkinPath() %>css/global/global.css" rel="stylesheet" type="text/css" />
		
		<link rel="stylesheet" type="text/css" href="<%=rootPath%>/automation/css/ext/ext-all.css" charset="gb2312" />
		<script type="text/javascript" src="<%=rootPath%>/automation/js/ext/ext-base.js" charset="gb2312"></script>
		<script type="text/javascript" src="<%=rootPath%>/automation/js/ext/ext-all.js" charset="gb2312"></script>
		<script type="text/javascript" src="<%=rootPath%>/automation/js/ext/ext-lang-zh_CN.js" charset="utf-8"></script>
        <script type="text/javascript" src="<%=rootPath%>/automation/js/jquery-1.8.2.min.js"></script>
		<script language="JavaScript" type="text/javascript">
 Ext.onReady(function()
{  
$("#loading").hide();
 Ext.get("vpnadd").on("click",function(){
   	//var chk1 = checkinput("user","string","�û�",50,false);
   	 var chk1 = checkinput("alias","string","����",50,false);
   	var chk2 = checkinput("port","string","�˿�",50,false);
   	var chk3 = checkinput("deviceVender","string","�豸����",50,false);
    var chk6 = checkinput("ipaddress","string","ip��ַ",15,false);  
    
      //if(chk1&&chk2&&chk3&&chk6)
      if(chk1&&chk2&&chk3&&chk6)
     {      
          	Ext.MessageBox.wait('���ݼ����У����Ժ�.. ');
        	mainForm.action = "<%=rootPath%>/netCfgFile.do?action=add";
        	mainForm.submit();
     }
 });	
});
   

function unionSelect(){
	var deviceVender = document.getElementById("deviceVender");
	var ostype = document.getElementById("ostype");
	
	var typeValue = deviceVender.value;
	
	
	ostype.length = 0;
	
	if(typeValue == "h3c"){
		ostype.options[1] = new Option("S8500", "S8500" );
		ostype.options[1] = new Option("H3C MSR3011", "H3C MSR3011" );
		ostype.options[1] = new Option("H3C MSR5060", "H3C MSR5060" );
		ostype.options[1] = new Option("H3C SR6608", "H3C SR6608" );
		ostype.options[1] = new Option("H3C S7510", "H3C S7510" );
		ostype.options[1] = new Option("Quidway S3552G", "Quidway S3552G" );
		ostype.options[1] = new Option("Quidway S3528G", "Quidway S3528G" );
		 document.getElementById("suuser").value="enable";
	}else if(typeValue == "cisco"){
		ostype.options[1] = new Option("Cisco 2511", "Cisco 2511" );
		ostype.options[1] = new Option("Cisco Catalyst 2950T-24", "Cisco Catalyst 2950T-24" );
		ostype.options[1] = new Option("Cisco Catalyst 6509", "Cisco Catalyst 6509" );
		ostype.options[1] = new Option("Cisco Catalyst 2950-12", "Cisco Catalyst 2950-12" );
		ostype.options[1] = new Option("Cisco Catalyst 2950C-24", "Cisco Catalyst 2950C-24" );
		ostype.options[1] = new Option("Cisco WS-C3550-48-SMI", "Cisco WS-C3550-48-SMI" );
	}else if(typeValue == "zte"){
		ostype.options[1] = new Option("ZXR10 M6000", "ZXR10 M6000" );
		ostype.options[2] = new Option("ZXR10 T600", "ZXR10 T600" );
		ostype.options[3] = new Option("ZXR10 5900E", "ZXR10 5900E" );
		ostype.options[4] = new Option("ZXR10 3884", "ZXR10 3884" );
		ostype.options[5] = new Option("ZXR10 2900", "ZXR10 2900" );
		 document.getElementById("suuser").value="enable";
	}else if(typeValue == "redgiant"){
	   document.getElementById("suuser").value="enable";
	}
	
	
}

</script>


		<script type="text/javascript">
			function showup(){
				var url="<%=rootPath%>/netCfgFile.do?action=netip";
				window.open(url,"portScanWindow","toolbar=no,width=900,height=600,directories=no,status=no,scrollbars=yes,menubar=no,resizable=yes");
			}
			function verLogin(){
			
			var username=$('#user').val();
			var suuser=$('#suuser').val();
			var pwd=$('#password').val();
			var supassword=$('#supassword').val();
			var ipaddress=$('#ipaddress').val();
			var port=$('#port').val();
			var promtp=$('#defaultpromtp').val();
			var type=document.getElementById("deviceVender").value;
			var connecttype=document.getElementById("connecttype").value;
			
			$("#loading").show();

 $.ajax({
			type:"POST",
			dataType:"json",
			url:"<%=rootPath%>/netCfgFileAjaxManager.ajax?action=verifyLogin",
			data:"connecttype="+connecttype+"&username="+username+"&pwd="+pwd+"&type="+type+"&suuser="+suuser+"&supassword="+supassword+"&ipaddress="+ipaddress+"&promtp="+promtp+"&port="+port+"&random="+Math.random(),
			success:function(data){
			$("#loading").hide();
			alert(data.result);
			}
		});
}
<!--ѡ���¼ģʽ-->
   function changePattern(){
   var connecttype=$("#connecttype").val();
   
   if(connecttype=="1"){
    $("#port").val("22");
    $("#dev_tr").show();
    $("#promtp_tr").hide();
    }else if(connecttype=="0"){
     $("#port").val("23");
       $("#dev_tr").show();
    $("#promtp_tr").show();
    }
    
   }
   function checkSuUser(){
		
		if($("#isSuper").attr('checked')==false){
		 $("#suuser").val("0");
		 $("#supwd_tr").hide();
		  $("#supassword").val("");
		 }else{
		 var type=$("#deviceVender").val();
		 if(type=="h3c"||type=="huawei"){
		 $("#suuser").val("system");
		 }else{
		 $("#suuser").val("enable");
		 }
		 }
		  $("#supwd_tr").show();
		}
		</script>

	</head>
	<body id="body" class="body" >



		<form id="mainForm" method="post" name="mainForm">

			<table id="body-container" class="body-container">
				<tr>
					
					<td class="td-container-main">
						<table id="container-main" class="container-main">
							<tr>
								<td class="td-container-main-add">
									<table id="container-main-add" class="container-main-add">
										<tr>
											<td>
												<table id="add-content" class="add-content">
													<tr>
														<td>
															<table id="add-content-header" class="add-content-header">
																<tr>
																	<td align="left" width="5"><img src="<%=rootPath%>/common/images/right_t_01.jpg" width="5" height="29" /></td>
																	<td class="add-content-title">
																		Զ�̵�¼���� ���
																	</td>
																	<td align="right"><img src="<%=rootPath%>/common/images/right_t_03.jpg" width="5" height="29" /></td>
																</tr>
															</table>
														</td>
													</tr>
													<tr>
														<td>
															<table id="detail-content-body"
																class="detail-content-body">
																<tr>
																	<td>

																		<table border="0" id="table1" cellpadding="0"
																			cellspacing="1" width="100%">
                                                                    
																			<tr >
																				<TD nowrap align="right" height="24">
																					ip��ַ&nbsp;
																				</TD>
																				<TD nowrap>
																					&nbsp;
																					<input name="ipaddress" type="text" size="21" class="formStyle" id="ipaddress" >
																					
																					<font color="red">&nbsp;* </font>
																				</TD>
																				<td align="right" height="20">
																					����&nbsp;
																				</td>
																				<td colspan="3" align="left">
																					&nbsp;
                                                                                 <input name="alias" type="text" size="21" class="formStyle" id="alias" >
                                                                                 <font color="red">&nbsp;* </font>
																				</td>
																			</tr>
																			      <tr style="background-color: #ECECEC;">
																				<TD nowrap align="right" height="24">
																					��¼ģʽ&nbsp;
																				</TD>
																				<TD nowrap>
																					&nbsp;
																					<select name="connecttype" id="connecttype" style="width: 80px" onchange="changePattern();">
																					<option value="0">
																							TELNET
																						</option>
																						<option value="1">
																							SSH
																						</option>
																					</select>
																					<font color="red">&nbsp;* </font>
																				</TD>
																				
																				<td align="right" height="20">
																					�˿�&nbsp;
																				</td>
																				<td colspan="3" align="left">
																					&nbsp;
																					<input name="port" type="text" size="16" id="port" class="formStyle" value="23">
																					<font color="red">&nbsp;* </font>
																				</td>
																			</tr>
																			<tr id="dev_tr">
																				<TD nowrap align="right" height="24" width="10%">
																					�豸������&nbsp;
																				</TD>
																				<TD nowrap width="40%">
																					&nbsp;
																					<select name="deviceVender" id="deviceVender" style="width: 250px" onchange="unionSelect();">
																						<option value="">
																						</option>
																						<option value="h3c">
																							H3C
																						</option>
																						<option value="huawei">
																							HUAWEI
																						</option>
																						<option value="cisco">
																							CISCO
																						</option>
																						<option value="redgiant">
																							RedGiant
																						</option>
																						<option value="zte">
																							ZTE
																						</option>
																					</select>
																					<font color="red">&nbsp;* </font>
																				</TD>
																				<TD nowrap align="right" height="24" width="10%">
																					����ϵͳ�ͺ�&nbsp;
																				</TD>
																				<TD nowrap width="40%">
																					&nbsp;
																					<select name="ostype" id="ostype" style="width: 250px">
																					</select>
																				</TD>
																			</tr>
																			
																			<tr>
																				<TD nowrap align="right" height="24" width="10%">
																					�û���&nbsp;
																				</TD>
																				<TD nowrap width="40%">
																					&nbsp;
																					<input type="text" name="user" id="user" size="40" class="formStyle">
																					<font color="red">&nbsp;(��û��������д)</font>
																				</TD>
																				<TD nowrap align="right" height="24">
																					����&nbsp;
																				</TD>
																				<TD nowrap>
																					&nbsp;
																					<input type="password" name="password" id="password" size="40" class="formStyle">
																					<font color="red">&nbsp;(��û��������д)</font>
																				</TD>
																			</tr>
																			<tr style="background-color: #ECECEC;">
																				<TD nowrap align="right" height="24">
																					�����û�&nbsp;
																				</TD>
																				<TD nowrap>
																					&nbsp;
																					<input type=checkbox name="isSuper" id="isSuper" onclick="checkSuUser()" >
																					<input name="suuser" type="hidden" size="22" id="suuser" class="formStyle" value="0">
																				</TD>
																				<TD nowrap align="right" height="24">
																					&nbsp;
																				</TD>
																				<TD nowrap>
																					&nbsp;
																				</TD>
																			</tr>
																			<tr id="supwd_tr" style="display: none" style="background-color: #ECECEC;">
																				<TD nowrap align="right" height="24">
																					�����û�����&nbsp;
																				</TD>
																				<TD nowrap>
																					&nbsp;
																					<input type="password" name="supassword" id="supassword" size="24" class="formStyle">
																				</TD>
																				<TD nowrap align="right" height="24">
																					&nbsp;
																				</TD>
																				<TD nowrap>
																					&nbsp;
																					
																				</TD>
																			</tr>
 																			<tr>
																		        <td colspan=4>
																		            <div>
																		                 <font color="red">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;��ʾ���������豸����û��telnet�û��������룬�ڴ˴����ʱ������д�����⵱telnet�û�������ͬʱӵ�г�������ԱȨ��ʱ���ڳ����û���������Ҳ������д��</font>
																		            </div>
																		        </td>
																		   </tr>
																			<tr>
																				<TD nowrap colspan="2" align=right>
																					<br>
																					<span class="loading-indicator" id="loading" style="display:none"> ������֤...</span>
																					<input type="button" value="��֤��¼" style="width: 65" id="verifyLogin" onclick="verLogin()"/>
																					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
																				</TD>
																				<TD colspan=2 align=left>
																					<br>
																					<input type="button" value="�� ��" style="width: 50" id="vpnadd" >
																					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
																					<input type="reset" style="width: 50" value="����" onclick="javascript:history.back(1)">
																				</TD>
																			</tr>

																		</TABLE>
																	</td>
																</tr>
															</table>
														</td>
													</tr>
													<tr>
														<td>
															<table id="detail-content-footer"
																class="detail-content-footer">
																<tr>
																	<td>
																		<table width="100%" border="0" cellspacing="0"
																			cellpadding="0">
																			<tr>
																				<td align="left" valign="bottom"><img src="<%=rootPath%>/common/images/right_b_01.jpg" width="5" height="12" /></td>
																				<td></td>
																				<td align="right" valign="bottom"><img src="<%=rootPath%>/common/images/right_b_03.jpg" width="5" height="12" /></td>
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
										<tr>
											<td>
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
</HTML>