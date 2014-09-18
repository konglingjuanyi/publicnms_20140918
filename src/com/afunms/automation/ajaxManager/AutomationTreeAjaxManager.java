package com.afunms.automation.ajaxManager;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.afunms.automation.dao.CmdCfgFileDao;
import com.afunms.automation.dao.CompGroupRuleDao;
import com.afunms.automation.dao.NetCfgFileNodeDao;
import com.afunms.automation.model.CmdCfgFile;
import com.afunms.automation.model.CompGroupRule;
import com.afunms.automation.model.NetCfgFileNode;
import com.afunms.common.base.AjaxBaseManager;
import com.afunms.common.base.AjaxManagerInterface;
import com.afunms.common.util.SessionConstant;
import com.afunms.system.model.User;

public class AutomationTreeAjaxManager extends AjaxBaseManager implements AjaxManagerInterface {

	public void execute(String action) {
		if (action.equals("getChildrenNodes")) {
			getChildrenNodes();
		}else if (action.equals("getTreeL3")) {
//			getTreeL3();
		}else if (action.equals("getConfiglistNodes")) {
			getConfiglistNodes();
		}else if (action.equals("getInspectionListNodes")) {
			getInspectionListNodes();
		}else if (action.equals("getPwdListNodes")) {
			getPwdListNodes();
		}else if (action.equals("updateGroupRule")) {
			updateGroupRule();
		}
		
		
		
	}

	/**
	 * ȡ�ø��ݸ��ڵ�id�������ӽڵ� ���ܲ˵���
	 * @author 
	 * @date 2014-3-6
	 */
	public void getChildrenNodes() {
		//System.out.println(request.getParameter("id") + " " + request.getParameter("name") + " " + request.getParameter("otherParam"));
		String id = request.getParameter("id");
		String rootPath = request.getContextPath();
		//������ڵ���Ϣ  
		List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();  
		
		//ҵ��ڵ�
		
			Map<String,Object> item = new HashMap<String,Object>(); 
			item.put("id", 0);
			item.put("pId", -1);
			item.put("name", "�豸��Դ��");
			item.put("url_my", "netCfgFile.do?action=list&flag=1");
			item.put("icon", rootPath+"/automation/images/base.gif");
			items.add(item);
			
			List<NetCfgFileNode> businessList = new ArrayList<NetCfgFileNode>();
			NetCfgFileNodeDao nodeDao = new NetCfgFileNodeDao();
			try {
				businessList = nodeDao.loadAll();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				nodeDao.close();
			}
			for(NetCfgFileNode node : businessList){
				String bid = node.getId()+"";
				String pid = "0";
				String name = "";
				if(id == null || "0".equals(id)){
				 name = node.getIpaddress();
				}else{
					name=node.getAlias();
				}
				String img=getImgName(node.getDeviceRender());
				item = new HashMap<String,Object>(); 
				item.put("id", bid);
				item.put("pId", pid);
				item.put("name", name);
				item.put("isParent", false);
				item.put("icon", rootPath+"/automation/images/"+img);
				item.put("url_my", "netCfgFile.do?action=queryById&id="+bid);
				
				items.add(item);
			}
		JSONArray json = JSONArray.fromObject(items);//ת��json��ʽ  
		out.print(json);
		out.flush();
	}
	public void getConfiglistNodes() {
		String id = request.getParameter("id");
		String rootPath = request.getContextPath();
		//������ڵ���Ϣ  
		List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();  
		
		//ҵ��ڵ�
			Map<String,Object> item = new HashMap<String,Object>(); 
			item.put("id", 0);
			item.put("pId", -1);
			item.put("name", "�豸��Դ��");
			item.put("url_my", "netCfgFile.do?action=configlist&flag=1");
			item.put("icon", rootPath+"/automation/images/base.gif");
			items.add(item);
			
			List<NetCfgFileNode> businessList = new ArrayList<NetCfgFileNode>();
			NetCfgFileNodeDao nodeDao = new NetCfgFileNodeDao();
			try {
				businessList = nodeDao.loadAll();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				nodeDao.close();
			}
			for(NetCfgFileNode node : businessList){
				String bid = node.getId()+"";
				String pid = "0";
				String name = "";
				if(id == null || "0".equals(id)){
					 name = node.getIpaddress();
					}else{
						name=node.getAlias();
					}
				String img=getImgName(node.getDeviceRender());
				item = new HashMap<String,Object>(); 
				item.put("id", bid);
				item.put("pId", pid);
				item.put("name", name);
				item.put("isParent", false);
				item.put("icon", rootPath+"/automation/images/"+img);
				item.put("url_my", "netCfgFile.do?action=queryCfgFileById&flag=1&id="+bid);
				
				items.add(item);
			}
		JSONArray json = JSONArray.fromObject(items);//ת��json��ʽ  
		out.print(json);
		out.flush();
	}
	
	public void getInspectionListNodes() {
		String id = request.getParameter("id");
		String rootPath = request.getContextPath();
		//������ڵ���Ϣ  
		List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();  
		
		//ҵ��ڵ�
			Map<String,Object> item = new HashMap<String,Object>(); 
			item.put("id", 0);
			item.put("pId", -1);
			item.put("name", "�豸��Դ��");
			item.put("url_my", "autoControl.do?action=cmdCfgList&flag=1");
			item.put("icon", rootPath+"/automation/images/base.gif");
			items.add(item);
			
			CmdCfgFileDao cmdCfgFileDao = new CmdCfgFileDao();
			List<CmdCfgFile> cmdDevicelist = cmdCfgFileDao.getAllcfgList();
			cmdCfgFileDao.close();
			NetCfgFileNodeDao telnetConfDao =null;
			List list=null;
			Map<String,NetCfgFileNode> map=new HashMap<String,NetCfgFileNode>();
			try {
				telnetConfDao = new NetCfgFileNodeDao();
				list=telnetConfDao.loadAll();
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				telnetConfDao.close();
			}
			if(list!=null){
				for (int i = 0; i < list.size(); i++) {
					NetCfgFileNode node=(NetCfgFileNode)list.get(i);
					map.put(node.getIpaddress(), node);
				}
			}
			if(cmdDevicelist!=null&&cmdDevicelist.size()>0){
			for(CmdCfgFile file : cmdDevicelist){
				String bid = file.getId()+"";
				String pid = "0";
				String name = "";
				String img="";
				if(map!=null&&map.containsKey(file.getIpaddress())){
					NetCfgFileNode cfgNode=(NetCfgFileNode)map.get(file.getIpaddress());
					name=cfgNode.getAlias();
				    img=getImgName(cfgNode.getDeviceRender());
				}
				if(id == null || "0".equals(id)){
					 name = file.getIpaddress();
					}
				item = new HashMap<String,Object>(); 
				item.put("id", bid);
				item.put("pId", pid);
				item.put("name", name);
				item.put("isParent", false);
				item.put("icon", rootPath+"/automation/images/"+img);
				item.put("url_my", "autoControl.do?action=queryCmdNodeById&flag=1&id="+bid);
				
				items.add(item);
			}
			}
		JSONArray json = JSONArray.fromObject(items);//ת��json��ʽ  
		out.print(json);
		out.flush();
	}
	public void getPwdListNodes() {
		String id = request.getParameter("id");
		String rootPath = request.getContextPath();
		//������ڵ���Ϣ  
		List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();  
		
		//ҵ��ڵ�
		
			Map<String,Object> item = new HashMap<String,Object>(); 
			item.put("id", 0);
			item.put("pId", -1);
			item.put("name", "�豸��Դ��");
			item.put("url_my", "remoteDevice.do?action=passwdList&flag=1");
			item.put("icon", rootPath+"/automation/images/base.gif");
			items.add(item);
			
			List<NetCfgFileNode> businessList = new ArrayList<NetCfgFileNode>();
			NetCfgFileNodeDao nodeDao = new NetCfgFileNodeDao();
			try {
				businessList = nodeDao.loadAll();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				nodeDao.close();
			}
			for(NetCfgFileNode node : businessList){
				String bid = node.getId()+"";
				String pid = "0";
				String name = "";
				if(id == null || "0".equals(id)){
				 name = node.getIpaddress();
				}else{
					name=node.getAlias();
				}
				String img=getImgName(node.getDeviceRender());
				item = new HashMap<String,Object>(); 
				item.put("id", bid);
				item.put("pId", pid);
				item.put("name", name);
				item.put("isParent", false);
				item.put("icon", rootPath+"/automation/images/"+img);
				item.put("url_my", "remoteDevice.do?action=queryNodesListById&flag=1&id="+bid);
				
				items.add(item);
			}
		JSONArray json = JSONArray.fromObject(items);//ת��json��ʽ  
		out.print(json);
		out.flush();
	}
	public void updateGroupRule(){
	    int id=getParaIntValue("id");
		String name=getParaValue("name");
		String desciption=getParaValue("description");
		String[] temp=request.getParameterValues("checkbox");
		
		StringBuffer ids=new StringBuffer();
		if (temp!=null) {
			for (int i = 0; i < temp.length; i++) {
				ids.append(temp[i]+",");
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		User user = (User) session.getAttribute(SessionConstant.CURRENT_USER);
		CompGroupRule vo=new CompGroupRule();
		vo.setId(id);
		vo.setName(name);
		
		vo.setDescription(desciption);
		vo.setRuleId(ids.toString());
		//vo.setCreatedBy(user.getName());
		//vo.setCreatedTime(sdf.format(date));
		vo.setLastModifiedBy(user.getName());
		vo.setLastModifiedTime(sdf.format(date));
		CompGroupRuleDao dao=new CompGroupRuleDao();
		boolean isSucess=dao.update(vo);
		Map<String,String> item=new HashMap();
		if(isSucess){
		item.put("result", "1");
		}else {
			item.put("result", "0");
		}
		JSONArray json = JSONArray.fromObject(item);//ת��json��ʽ  
		out.print(json);
		out.flush();
	}
	public String getImgName(String type) {
		String image="";
		 if (type.startsWith("cisco")) // cisco
			image = "a_cisco.gif";
		else if (type.startsWith("h3c")) // huawei
			image = "a_h3c.gif";
		else if (type.startsWith("redgiant")) // ���
			image = "a_redg.gif";
		else if (type.startsWith("huawei")) // ���
			image = "a_hw.gif";
		else if (type.startsWith("zte")) // ���
			image = "a_zte.gif";
		return image;
		
	}
}
