package com.afunms.automation.manage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.afunms.automation.dao.CmdCfgFileDao;
import com.afunms.automation.dao.NetCfgFileDao;
import com.afunms.automation.dao.NetCfgFileNodeDao;
import com.afunms.automation.dao.TimingBackupCfgFileDao;
import com.afunms.automation.model.CmdCfgFile;
import com.afunms.automation.model.ConfiguringDevice;
import com.afunms.automation.model.NetCfgFile;
import com.afunms.automation.model.NetCfgFileNode;
import com.afunms.automation.model.TimingBackupCfgFile;
import com.afunms.automation.task.BatchBackupTask;
import com.afunms.automation.task.BatchDeployTask;
import com.afunms.automation.telnet.NetCfgFileTelnet;
import com.afunms.capreport.common.DateTime;
import com.afunms.common.base.BaseManager;
import com.afunms.common.base.BaseVo;
import com.afunms.common.base.DaoInterface;
import com.afunms.common.base.ErrorMessage;
import com.afunms.common.base.JspPage;
import com.afunms.common.base.ManagerInterface;
import com.afunms.common.util.SysLogger;
import com.afunms.initialize.ResourceCenter;
import com.afunms.topology.dao.HostNodeDao;
import com.afunms.topology.model.HostNode;

public class NetCfgFileManager extends BaseManager implements ManagerInterface {
	private static NetCfgFileManager netCfgFileManager;
	static StringBuffer result = new StringBuffer();

	public static synchronized NetCfgFileManager getInstance() {
		if (netCfgFileManager == null) {
			netCfgFileManager = new NetCfgFileManager();
		}
		return netCfgFileManager;
	}

	public String list() {
		List configingDeviceList = new ArrayList();
		NetCfgFileDao vpnFileDao = new NetCfgFileDao();
		List vpnDevicelist = vpnFileDao.getDeviceWithLastModify();
		vpnFileDao.close();

		NetCfgFileNodeDao netCfgFileNodeDao = new NetCfgFileNodeDao();
		this.list(netCfgFileNodeDao);
		List telnetConfList = (List) request.getAttribute("list");
		int vpnDevicelistSize = 0;
		int tmp = 0;
		NetCfgFile tmp2 = null;
		for (int i = 0; i < telnetConfList.size(); i++) {
			ConfiguringDevice cfgingDevice = new ConfiguringDevice();
			NetCfgFileNode telnetConf = (NetCfgFileNode) telnetConfList.get(i);
			cfgingDevice.setId(telnetConf.getId());
			cfgingDevice.setIpaddress(telnetConf.getIpaddress());
			cfgingDevice.setAlias(telnetConf.getAlias());
			// cfgingDevice.setPrompt(telnetConf.getDefaultpromtp());
			// cfgingDevice.setEnablevpn(telnetConf.getEnablevpn());
			// cfgingDevice.setIsSynchronized(telnetConf.getIsSynchronized());
			cfgingDevice.setDeviceRender(telnetConf.getDeviceRender());
			vpnDevicelistSize = vpnDevicelist.size();
			tmp = 0;

			while (vpnDevicelistSize > tmp)// �б����ļ����豸��װ�������һ�εı���ʱ�䣬�ޱ����ļ����豸���ֶ���null
			{
				tmp2 = (NetCfgFile) vpnDevicelist.get(tmp);
				if (tmp2.getIpaddress().equals(telnetConf.getIpaddress())) {
					cfgingDevice.setLastUpdateTime(tmp2.getBackupTime());
					break;
				}
				tmp++;
				if (vpnDevicelistSize == tmp) {
					cfgingDevice.setLastUpdateTime(null);
				}
			}
			configingDeviceList.add(cfgingDevice);
		}
		request.setAttribute("configlist", configingDeviceList);
		return "/automation/cfgfile/list.jsp";
	}

	public String readyAdd() {
		return "/automation/cfgfile/add.jsp";
	}

	/**
	 * 
	 * @description �ڵ�������
	 * @author wangxiangyong
	 * @date Aug 28, 2014 8:31:10 AM
	 * @return String
	 * @return
	 */
	public String add() {
		String ipAddress = getParaValue("ipaddress");
		String alias = getParaValue("alias");
		String ostype = this.getParaValue("ostype");
		String suuser = this.getParaValue("suuser");
		int connecttype = this.getParaIntValue("connecttype");
		String deviceVender = this.getParaValue("deviceVender");
		NetCfgFileNodeDao tmpDao = new NetCfgFileNodeDao();
		NetCfgFileNode tmp = (NetCfgFileNode) tmpDao.loadByIp(ipAddress);
		tmpDao.close();
//		String superName = "";
//		if (isSuper == null) {
//			superName = "0";
//		} 
		if (tmp != null) {
			setErrorCode(ErrorMessage.IP_ADDRESS_EXIST);
			return null;
		} else {

			NetCfgFileNode vo = new NetCfgFileNode();
			vo.setOstype(ostype);
			vo.setDeviceRender(deviceVender);
			vo.setAlias(alias);
			if (deviceVender == null || deviceVender.equals("null") || deviceVender.trim().length() == 0) {
				HostNodeDao hostNodeDao = new HostNodeDao();
				HostNode hostNode = null;
				BaseVo baseVo = null;
				try {
					baseVo = hostNodeDao.findByIpaddress(ipAddress);
					if (baseVo != null)
						hostNode = (HostNode) baseVo;// �����topo_host_node���ҵ�ָ��ip���������򲻻ᱨ��
				} catch (Exception e) {
					hostNode = null;// ����˵��topo_host_node��û�ж�Ӧ����
				}
				hostNodeDao.close();
				if (hostNode != null) {
					vo.setId(hostNode.getId());
					String sysOid = hostNode.getSysOid();

					if (sysOid.startsWith("1.3.6.1.4.1.25506") || sysOid.startsWith("1.3.6.1.4.1.2011"))// ����
					{
						vo.setDeviceRender("h3c");
					} else if (sysOid.equals("1.3.6.1.4.1.9.1.209"))// ˼��
					{
						vo.setDeviceRender("cisco");
					} else if (sysOid.startsWith("1.3.6.1.4.1.4881"))// ���
					{
						vo.setDeviceRender("redgiant");
					} else if (sysOid.startsWith("1.3.6.1.4.1.3902"))// ����
					{
						vo.setDeviceRender("zte");
					} else {
						vo.setDeviceRender("unknow");
					}
				}
			}
			
			
//			if(vo.getDeviceRender().equals("h3c")||vo.getDeviceRender().equals("huawei")){
//				superName="system";
//			}else{
//				superName="enable";
//			}
			vo.setUser(getParaValue("user"));
			vo.setPassword(getParaValue("password"));
			vo.setSuuser(suuser);
			vo.setSupassword(getParaValue("supassword"));
			vo.setIpaddress(ipAddress);
			vo.setPort(getParaIntValue("port"));
			vo.setConnecttype(connecttype);
			NetCfgFileNodeDao dao = new NetCfgFileNodeDao();
			try {
				int id = dao.getNextId();
				vo.setId(id);
				dao.save(vo);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				dao.close();
			}
			return list();
		}
	}

	/**
	 * 
	 * @description �Զ�����ҳɾ���ڵ����
	 * @author wangxiangyong
	 * @date Aug 28, 2014 8:40:33 AM
	 * @return String
	 * @return
	 */
	private String delete() {
		String[] id = getParaArrayValue("checkbox");
		if (id != null) {
			NetCfgFileNodeDao telnetcfgdao = new NetCfgFileNodeDao();
			List telnetcfgList = telnetcfgdao.loadByIds(id);
			telnetcfgdao.close();
			NetCfgFileDao vpnDao = new NetCfgFileDao();
			if (id != null && id.length != 0) {
				for (int i = 0; i < telnetcfgList.size(); i++) {
					NetCfgFileNode node = (NetCfgFileNode) telnetcfgList.get(i);
					List vpnConfigList = vpnDao.loadByIp(node.getIpaddress());
					String[] vpncol = new String[vpnConfigList.size()];
					for (int j = 0; j < vpnConfigList.size(); j++) {
						NetCfgFile tmp = (NetCfgFile) vpnConfigList.get(j);
						vpncol[j] = new Integer(tmp.getId()).toString();
						File f = new File(tmp.getFileName());
						f.delete();
					}
					vpnDao.delete(vpncol);
				}
			}
			vpnDao.close();
			DaoInterface dao = new NetCfgFileNodeDao();
			setTarget("/netCfgFile.do?action=list&jp=1");
			return delete(dao);
		} else
			return list();
	}
	/**
	 * 
	 * @description �޸Ľڵ���Ϣ
	 * @author wangxiangyong
	 * @date Sep 2, 2014 7:41:55 AM
	 * @return String  
	 * @return
	 */
	private String update() {
		int id = getParaIntValue("id");
		String ipAddress = getParaValue("ipaddress");
		String alias = getParaValue("alias");
		String ostype = this.getParaValue("ostype");
		String suuper = this.getParaValue("suuser");
		int connecttype = this.getParaIntValue("connecttype");
		String deviceVender = this.getParaValue("deviceVender");
//		String superName = "";
		
		NetCfgFileNode vo = new NetCfgFileNode();
		vo.setId(id);
		vo.setOstype(ostype);
		vo.setDeviceRender(deviceVender);
		vo.setAlias(alias);
//		if (isSuper == null) {
//			superName = "0";
//		} else {
//			if(vo.getDeviceRender().equals("h3c")||vo.getDeviceRender().equals("huawei")){
//				superName="system";
//			}else{
//				superName="enable";
//			}
//		}
		
		vo.setUser(getParaValue("user"));
		vo.setPassword(getParaValue("password"));
		vo.setSuuser(suuper);
		vo.setSupassword(getParaValue("supassword"));
		vo.setIpaddress(ipAddress);
		vo.setPort(getParaIntValue("port"));
		vo.setConnecttype(connecttype);
		NetCfgFileNodeDao dao = new NetCfgFileNodeDao();
		String target = null;
		if (dao.update(vo))
			target = list();
		return target;
	}
	/**
	 * ͬ������ �ѱ�topo_node_telnetconfig �� is_synchronized=0 ����
	 * ip_address=topo_host_node.ip_address ���������id����Ϊ��topo_host_node �е� id
	 */
	private String synchronizeData() {
		NetCfgFileNodeDao hdao = new NetCfgFileNodeDao();
		String sql = "update topo_node_telnetconfig  set topo_node_telnetconfig.id=(select topo_host_node.id from topo_host_node where topo_node_telnetconfig.ip_address=topo_host_node.ip_address),topo_node_telnetconfig.is_synchronized=1,topo_node_telnetconfig.device_render=(select topo_host_node.sys_oid from topo_host_node where topo_node_telnetconfig.ip_address=topo_host_node.ip_address) where topo_node_telnetconfig.is_synchronized=0 and topo_node_telnetconfig.ip_address in(select topo_host_node.ip_address from topo_host_node)";
		hdao.executeUpdate(sql);
		hdao.close();
		return list();
	}

	/**
	 * 
	 * @description �Ҽ��˵� �༭
	 * @author wangxiangyong
	 * @date Aug 28, 2014 9:01:32 AM
	 * @return String
	 * @return
	 */
	private String ready_edit() {
		DaoInterface dao = new NetCfgFileNodeDao();
		setTarget("/automation/cfgfile/edit.jsp");
		return readyEdit(dao);
	}

	/**
	 * 
	 * @description ���豸�б���ѡ��IP
	 * @author wangxiangyong
	 * @date Aug 28, 2014 9:12:16 AM
	 * @return String
	 * @return
	 */
	public String netip() {
		HostNodeDao dao = new HostNodeDao();
		List list = dao.loadNetwork(1);
		int listsize = list.size();
		request.setAttribute("iplist", list);
		HostNodeDao listdao = new HostNodeDao();
		setTarget("/automation/cfgfile/netip.jsp");
		String page = list(listdao);
		JspPage jp = (JspPage) request.getAttribute("page");
		jp.setTotalRecord(listsize);
		request.setAttribute("page", jp);
		return page;
	}

	/**
	 * 
	 * @description �����豸�����ļ��б�
	 * @author wangxiangyong
	 * @date Aug 28, 2014 9:54:24 AM
	 * @return String
	 * @return
	 */
	public String configlist() {
		List configingDeviceList = new ArrayList();
		NetCfgFileDao vpnFileDao = new NetCfgFileDao();
		List vpnDevicelist = vpnFileDao.getDeviceWithLastModify();
		vpnFileDao.close();

		NetCfgFileNodeDao telnetConfDao = new NetCfgFileNodeDao();
		this.list(telnetConfDao);
		List telnetConfList = (List) request.getAttribute("list");
		int vpnDevicelistSize = 0;
		int tmp = 0;
		NetCfgFile tmp2 = null;
		for (int i = 0; i < telnetConfList.size(); i++) {
			ConfiguringDevice cfgingDevice = new ConfiguringDevice();
			NetCfgFileNode telnetConf = (NetCfgFileNode) telnetConfList.get(i);
			cfgingDevice.setId(telnetConf.getId());
			cfgingDevice.setIpaddress(telnetConf.getIpaddress());
			cfgingDevice.setAlias(telnetConf.getAlias());
			cfgingDevice.setDeviceRender(telnetConf.getDeviceRender());
			vpnDevicelistSize = vpnDevicelist.size();
			tmp = 0;
			while (vpnDevicelistSize > tmp)// �б����ļ����豸��װ�������һ�εı���ʱ�䣬�ޱ����ļ����豸���ֶ���null
			{
				tmp2 = (NetCfgFile) vpnDevicelist.get(tmp);
				if (tmp2.getIpaddress().equals(telnetConf.getIpaddress())) {
					cfgingDevice.setLastUpdateTime(tmp2.getBackupTime());
					break;
				}
				tmp++;
				if (vpnDevicelistSize == tmp) {
					cfgingDevice.setLastUpdateTime(null);
				}
			}
			configingDeviceList.add(cfgingDevice);
		}
		request.setAttribute("list", configingDeviceList);
		return "/automation/cfgfile/configlist.jsp";
	}
	/**
	 * 
	 * @description �����豸�����ļ��б�
	 * @author wangxiangyong
	 * @date Aug 28, 2014 9:54:24 AM
	 * @return String
	 * @return
	 */
	public String queryCfgFileById() {
		String id=getParaValue("id");
		List configingDeviceList = new ArrayList();
		
		NetCfgFileDao vpnFileDao = new NetCfgFileDao();
		List vpnDevicelist = vpnFileDao.getDeviceWithLastModify();
		vpnFileDao.close();
		List telnetConfList=null;
		NetCfgFileNodeDao telnetConfDao=null;
		 try {
				telnetConfDao = new NetCfgFileNodeDao();
			   telnetConfList = telnetConfDao.loadById(id);
				this.list(telnetConfDao);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				telnetConfDao.close();
			}
		int vpnDevicelistSize = 0;
		int tmp = 0;
		NetCfgFile tmp2 = null;
		for (int i = 0; i < telnetConfList.size(); i++) {
			ConfiguringDevice cfgingDevice = new ConfiguringDevice();
			NetCfgFileNode telnetConf = (NetCfgFileNode) telnetConfList.get(i);
			cfgingDevice.setId(telnetConf.getId());
			cfgingDevice.setIpaddress(telnetConf.getIpaddress());
			cfgingDevice.setAlias(telnetConf.getAlias());
			cfgingDevice.setDeviceRender(telnetConf.getDeviceRender());
			vpnDevicelistSize = vpnDevicelist.size();
			tmp = 0;
			while (vpnDevicelistSize > tmp)// �б����ļ����豸��װ�������һ�εı���ʱ�䣬�ޱ����ļ����豸���ֶ���null
			{
				tmp2 = (NetCfgFile) vpnDevicelist.get(tmp);
				if (tmp2.getIpaddress().equals(telnetConf.getIpaddress())) {
					cfgingDevice.setLastUpdateTime(tmp2.getBackupTime());
					break;
				}
				tmp++;
				if (vpnDevicelistSize == tmp) {
					cfgingDevice.setLastUpdateTime(null);
				}
			}
			configingDeviceList.add(cfgingDevice);
		}
		request.setAttribute("list", configingDeviceList);
		return "/automation/cfgfile/configlist.jsp";
	}
	/**
	 * 
	 * @description �����豸�����ļ��б�
	 * @author wangxiangyong
	 * @date Aug 28, 2014 9:54:24 AM
	 * @return String
	 * @return
	 * 
	 *
	 */
	public String queryById() {
		String id=getParaValue("id");
		List configingDeviceList = new ArrayList();
		NetCfgFileNodeDao netCfgFileNodeDao = new NetCfgFileNodeDao();
		this.list(netCfgFileNodeDao);
		netCfgFileNodeDao.close();
		List telnetConfList=null;
		NetCfgFileNodeDao telnetConfDao=null;
		 try {
				telnetConfDao = new NetCfgFileNodeDao();
			   telnetConfList = telnetConfDao.loadById(id);
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				telnetConfDao.close();
			}
		for (int i = 0; i < telnetConfList.size(); i++) {
			ConfiguringDevice cfgingDevice = new ConfiguringDevice();
			NetCfgFileNode telnetConf = (NetCfgFileNode) telnetConfList.get(i);
			cfgingDevice.setId(telnetConf.getId());
			cfgingDevice.setIpaddress(telnetConf.getIpaddress());
			cfgingDevice.setAlias(telnetConf.getAlias());
			cfgingDevice.setDeviceRender(telnetConf.getDeviceRender());
			
			configingDeviceList.add(cfgingDevice);
		}
		request.setAttribute("configlist", configingDeviceList);
		return "/automation/cfgfile/list.jsp";
	}
	/**
	 * 
	 * @description �������������ļ�
	 * @author wangxiangyong
	 * @date Aug 28, 2014 9:57:21 AM
	 * @return String
	 * @return
	 */
	private String readyBackupConfig() {
		String id = this.getParaValue("id");
		NetCfgFileNodeDao dao = new NetCfgFileNodeDao();
		NetCfgFileNode vo = null;
		String ipaddress = "";
		try {
			vo = (NetCfgFileNode) dao.findByID(id);
		} catch (Exception e) {
			SysLogger.error("NetCfgFileManager.readyBackupConfig()", e);
		} finally {
			dao.close();
		}
		if (vo != null) {
			ipaddress = vo.getIpaddress();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH-mm-ss");
		String b_time = sdf.format(new Date());
//		String prefix = ResourceCenter.getInstance().getSysPath().replace("\\", "\\\\");
//		String fileName =  "cfg\\\\" + ipaddress + "_" + b_time + "cfg.cfg";
		String fileName =   ipaddress + "_" + b_time + "cfg.cfg";
		request.setAttribute("id", id);
		request.setAttribute("ipaddress", ipaddress);
		request.setAttribute("fileName", fileName);
		return "/automation/cfgfile/backupCfgFile.jsp";
	}

	/**
	 * 
	 * @description �����豸�����ļ���������
	 * @author wangxiangyong
	 * @date Aug 28, 2014 9:59:32 AM
	 * @return String
	 * @return
	 */
	private String ready_deployCfgForBatch() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH-mm");
		String b_time = sdf.format(new Date());
		String prefix = ResourceCenter.getInstance().getSysPath().replace("\\", "\\\\");
		String fileName = prefix + "cfg\\\\" + "_" + b_time + "batch.cfg";
		request.setAttribute("fileName", fileName);
		return "/automation/cfgfile/deployCfgForBatch.jsp";
	}

	/**
	 * 
	 * @description ����IP����ʾ�������������ļ�
	 * @author wangxiangyong
	 * @date Aug 28, 2014 10:03:36 AM
	 * @return String
	 * @return
	 */
	private String showAllFile() {
		String ip = getParaValue("ip");
		String type = getParaValue("type");// �豸����
		NetCfgFileDao vpnFileDao = new NetCfgFileDao();
		List vpnDevicelist = vpnFileDao.loadByIp(ip);
		vpnFileDao.close();

		request.setAttribute("ip", ip);
		request.setAttribute("type", type);
		request.setAttribute("list", vpnDevicelist);
		return "/automation/cfgfile/allConfiglist.jsp";
	}

	/**
	 * 
	 * @description ���û��������ļ�
	 * @author wangxiangyong
	 * @date Aug 28, 2014 10:38:08 AM
	 * @return String
	 * @return
	 */
	private String setBaseLine() {
		String id = getParaValue("id");
		String ipaddress = getParaValue("ipaddress");
		NetCfgFileDao dao = null;
		try {
			dao = new NetCfgFileDao();
			dao.cancelBaseLine(ipaddress);
			dao = new NetCfgFileDao();
			dao.updateBaseLine(id, 1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dao != null)
				dao.close();
		}
		return showAllFile();
	}

	/**
	 * 
	 * @description �����ļ��ȶ�
	 * @author wangxiangyong
	 * @date Aug 28, 2014 10:40:08 AM
	 * @return String
	 * @return
	 */
	private String compareContent() {

		String baseFileName = getParaValue("filename");
		String comparefilename = getParaValue("baskcheckbox");
		String baseCfgName = "";
		String cmpCfgName = "";

		// ��ȡ�����ļ�
		// ��������ļ�
		String[] baseCfgNames = new String[baseFileName.split("/").length];
		baseCfgNames = baseFileName.split("/");
		if (baseCfgNames.length > 1) {
			baseCfgName = baseCfgNames[baseCfgNames.length - 1];
		} else {
			baseCfgNames = new String[baseFileName.split("\\\\").length];
			baseCfgNames = baseFileName.split("\\\\");
			if (baseCfgNames.length > 0)
				baseCfgName = baseCfgNames[baseCfgNames.length - 1];
		}

		// ѭ����ȥ�Ƚ��ļ����ļ���Ϣ����hashtable��key Ϊ�ļ�����value Ϊ�ļ�����
		String conffilename = "";
		if (comparefilename != null && comparefilename.indexOf(";") > 0) {

			String[] strs = comparefilename.split(";");

			for (int t = 0; t < strs.length; t++) {

				try {

					conffilename = strs[t];
					String[] cmpCfgNames = new String[conffilename.split("/").length];
					cmpCfgNames = conffilename.split("/");
					if (cmpCfgNames.length > 1) {
						cmpCfgName = cmpCfgNames[cmpCfgNames.length - 1];
					} else {
						cmpCfgNames = new String[conffilename.split("\\\\").length];
						cmpCfgNames = conffilename.split("\\\\");
						if (cmpCfgNames.length > 0)
							cmpCfgName = cmpCfgNames[cmpCfgNames.length - 1];

					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
		session.setAttribute("baseCfgName", baseFileName);
		session.setAttribute("compareCfgName", conffilename);
		request.setAttribute("baseCfgName", baseCfgName);
		request.setAttribute("compareCfgName", cmpCfgName);
		return "/automation/cfgfile/compareContent.jsp";
	}

	/**
	 * 
	 * @description ɾ�������ļ�
	 * @author wangxiangyong
	 * @date Aug 28, 2014 10:47:41 AM
	 * @return String
	 * @return
	 */
	private String deleteFile() {
		String[] ids = getParaArrayValue("checkbox");
		if (ids != null) {

			NetCfgFileDao vpnDao = new NetCfgFileDao();
			if (ids != null && ids.length != 0) {

				List vpnConfigList = vpnDao.loadByIds(ids);
				for (int j = 0; j < vpnConfigList.size(); j++) {
					NetCfgFile tmp = (NetCfgFile) vpnConfigList.get(j);
					if (tmp != null) {
						File f = new File(tmp.getFileName());
						if (f.exists())
							f.delete();
					}
				}

			}
			vpnDao.delete(ids);
			vpnDao.close();

		}
		return showAllFile();

	}

	/**
	 * 
	 * @description ������������ҳ��
	 * @author wangxiangyong
	 * @date Aug 28, 2014 2:02:53 PM
	 * @return String
	 * @return
	 */
	private String ready_backupForBatch() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH-mm");
		String b_time = sdf.format(new Date());
		String prefix = ResourceCenter.getInstance().getSysPath().replace("\\", "/");
		String fileName = prefix + "cfg/" + "IP" + "_" + b_time + "cfg.cfg";

		request.setAttribute("fileName", fileName);
		return "/automation/cfgfile/batchBackup.jsp";
	}

	/**
	 * 
	 * @description ִ���������������ļ�����
	 * @author wangxiangyong
	 * @date Aug 28, 2014 2:03:18 PM
	 * @return String
	 * @return
	 */
	private String bkpCfg_forBatch() {

		String bkpType = this.getParaValue("bkptype");
		result.delete(0, result.length());
		String fileName = this.getParaValue("fileName");
		String fileDesc = this.getParaValue("fileDesc");
		String ipAddresses = request.getParameter("ipaddress");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH-mm");
		String bkptime = "";
		Date bkpDate = new Date();
		String reg = "_(.*)cfg.cfg";
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(fileName);
		if (m.find()) {
			bkptime = m.group(1);
		}
		try {
			bkpDate = sdf.parse(bkptime);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] split = ipAddresses.substring(1).split(",");
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 30, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(30), new ThreadPoolExecutor.CallerRunsPolicy());
		String s = "";
		for (int i = 0; i < split.length; i++) {
			s = s + "," + split[i];
		}
		String s2 = s.substring(1);
		String sql = "select * from automation_node where ip_address in('" + s2.replace(",", "','") + "')";
		NetCfgFileNodeDao dao = new NetCfgFileNodeDao();
		List list = dao.findByCriteria(sql);
		dao.close();
		result.delete(0, result.length());
		for (int i = 0; i < split.length; i++) {
			threadPool.execute(new BatchBackupTask(result, (NetCfgFileNode) list.get(i), fileName, fileDesc, bkpDate, bkpType));
		}
		threadPool.shutdown();
		try {
			boolean loop = true;
			do { // �ȴ������������
				loop = !threadPool.awaitTermination(2, TimeUnit.SECONDS);
			} while (loop);
		} catch (Exception e) {
		}

		request.setAttribute("result", result.toString());
		return "/automation/cfgfile/multi_modify_status.jsp";
	}

	/**
	 * @description ������д���ļ���������
	 * @author wangxiangyong
	 * @param bkpType
	 *            ���ݵ��ļ�����
	 * @param fileName
	 *            �ļ���
	 * @param fileDesc
	 *            ����
	 * @param bkpDate
	 *            ��������
	 * @param vo
	 * @param result
	 *            �ļ�����
	 * @date Aug 28, 2014 2:31:48 PM
	 * @return void
	 */
	public synchronized void writeFileAndToDb(String bkpType, String fileName, String fileDesc, Date bkpDate, NetCfgFileNode vo, String result) {
		String prefix = ResourceCenter.getInstance().getSysPath().replace("\\", "/");
		String path = prefix + "cfg/" +fileName;
		File f = new File(path);
		int fileSize = 0;
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f);
			fw.write(result);
			fw.flush();
			fw.close();
			FileInputStream fis = new FileInputStream(f);
			fileSize = fis.available();
			if (fileSize != 0) {
				fileSize = fileSize / 1000;
				if (fileSize == 0)
					fileSize = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		NetCfgFile h3vpn = new NetCfgFile();
		h3vpn.setFileName(fileName);
		h3vpn.setDescri(fileDesc);
		h3vpn.setIpaddress(vo.getIpaddress());
		h3vpn.setFileSize(fileSize);
		h3vpn.setBackupTime(new Timestamp(bkpDate.getTime()));
		h3vpn.setBkpType(bkpType);
		NetCfgFileDao h3Dao = new NetCfgFileDao();
		h3Dao.save(h3vpn);
		h3Dao.close();
	}

	/**
	 * @description ������д���ļ���������
	 * @author wangxiangyong
	 * @param bkpType
	 *            ���ݵ��ļ�����
	 * @param fileName
	 *            �ļ���
	 * @param fileDesc
	 *            ����
	 * @param bkpDate
	 *            ��������
	 * @param vo
	 * @param result
	 *            �ļ�����
	 * @date Aug 28, 2014 2:31:48 PM
	 * @return void
	 */
	public synchronized void writeLogAndToDb(String bkpType, String fileName, String fileDesc, Date bkpDate, NetCfgFileNode vo, String result) {
			String prefix = ResourceCenter.getInstance().getSysPath().replace("\\", "/");
		String path = prefix + "cfg/" +fileName;
		File f = new File(path);
		int fileSize = 0;
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f);
			fw.write(result);
			fw.flush();
			fw.close();
			FileInputStream fis = new FileInputStream(f);
			fileSize = fis.available();
			if (fileSize != 0) {
				fileSize = fileSize / 1000;
				if (fileSize == 0)
					fileSize = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		CmdCfgFile h3vpn = new CmdCfgFile();
		h3vpn.setFileName(fileName);
		h3vpn.setContent(fileDesc);
		h3vpn.setIpaddress(vo.getIpaddress());
		h3vpn.setFileSize(fileSize);
		h3vpn.setBackupTime(new Timestamp(bkpDate.getTime()));
		h3vpn.setBkpType(bkpType);
		CmdCfgFileDao h3Dao = new CmdCfgFileDao();
		h3Dao.save(h3vpn);
		h3Dao.close();
	}
	private String deployCfgForBatch() {
		String fileName = null;
		String serverFilePath = null;
		FileItem fileIntem = null;
		String ipAddresses = null;

		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(1024 * 10);
		String prefix = ResourceCenter.getInstance().getSysPath().replace("\\", "\\\\");
		factory.setRepository(new File(prefix + "cfg\\batch"));// ���÷������˱���·��
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(1000000);
		try {
			List fileItems = upload.parseRequest(this.request);
			Iterator iter = fileItems.iterator(); // ���δ���ÿ���ؼ�
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();// �����������ļ�������б���Ϣ
				// System.out.println(item.getName());
				if (item.getName() != null) {
					fileIntem = item;
					System.out.println(fileIntem.getName());
					fileName = fileIntem.getName();
				}
				if (item.isFormField()) {
					if (item.getFieldName().equals("ipaddress"))// �ļ���
					{
						ipAddresses = item.getString();
					}

				}
			}
			serverFilePath = prefix + "cfg\\\\batch\\\\" + fileName;
			fileIntem.write(new File(serverFilePath));

		} catch (Exception e) {
			e.printStackTrace();
			setErrorCode(0);// δ֪����
		}

		String[] split = ipAddresses.substring(1).split(",");
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 30, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(30), new ThreadPoolExecutor.CallerRunsPolicy());
		String s = "";
		for (int i = 0; i < split.length; i++) {
			s = s + "," + split[i];
		}
		String s2 = s.substring(1);
		String sql = "select * from topo_node_telnetconfig where ip_address in('" + s2.replace(",", "','") + "')";
		NetCfgFileNodeDao dao = new NetCfgFileNodeDao();
		List list = dao.findByCriteria(sql);
		dao.close();

		result.delete(0, result.length());
		for (int i = 0; i < split.length; i++) {
			threadPool.execute(new BatchDeployTask(result, (NetCfgFileNode) list.get(i), fileName, serverFilePath));
		}
		threadPool.shutdown();
		try {
			boolean loop = true;
			do { // �ȴ������������
				loop = !threadPool.awaitTermination(2, TimeUnit.SECONDS);
			} while (loop);
		} catch (Exception e) {
		}

		request.setAttribute("result", result.toString());
		return "/config/vpntelnet/multi_modify_status.jsp";
	}

	/**
	 * 
	 * @description ת��ʱ����ҳ��
	 * @author wangxiangyong
	 * @date Aug 29, 2014 8:05:03 AM
	 * @return String
	 * @return
	 */
	private String ready_timingBackup() {
		TimingBackupCfgFileDao timingBackupTelnetConfigDao = new TimingBackupCfgFileDao();
		List<TimingBackupCfgFile> timingBackupTelnetConfigList = null;
		try {
			timingBackupTelnetConfigList = timingBackupTelnetConfigDao.getAlList();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timingBackupTelnetConfigDao.close();
		}
		request.setAttribute("timingBackupTelnetConfigList", timingBackupTelnetConfigList);
		return "/automation/cfgfile/timingBackup.jsp";
	}

	/**
	 * 
	 * @description ���Ӷ�ʱ���������ļ�������
	 * @author wangxiangyong
	 * @date Aug 29, 2014 8:41:06 AM
	 * @return String
	 * @return
	 */
	private String addTimingBackupTelnetConfig() {
		return "/automation/cfgfile/addTimingBackup.jsp";
	}

	/**
	 * 
	 * @description ��ʱ����
	 * @author wangxiangyong
	 * @date Aug 29, 2014 8:45:18 AM
	 * @return String
	 * @return
	 */
	private String timingBackup() {
		TimingBackupCfgFileDao timingBackupTelnetConfigDao = new TimingBackupCfgFileDao();
		TimingBackupCfgFile timingBackupTelnetConfig = new TimingBackupCfgFile();
		String ipaddress = getParaValue("ipaddress");
		String[] sendtimemonth = request.getParameterValues("sendtimemonth");
		String[] sendtimeweek = request.getParameterValues("sendtimeweek");
		String[] sendtimeday = request.getParameterValues("sendtimeday");
		String[] sendtimehou = request.getParameterValues("sendtimehou");
		String transmitfrequency = request.getParameter("transmitfrequency");
		String status = getParaValue("status");
		String bkpType = getParaValue("bkpType");
		String checkbox = getParaValue("checkbox");
		List<TimingBackupCfgFile> timingBackupTelnetConfigList = null;

		DateTime dt = new DateTime();
		try {
			timingBackupTelnetConfig.setTelnetconfigids(ipaddress);
			timingBackupTelnetConfig.setBackup_sendfrequency(Integer.parseInt(transmitfrequency));
			timingBackupTelnetConfig.setBackup_time_month(arrayToString(sendtimemonth));
			timingBackupTelnetConfig.setBackup_time_week(arrayToString(sendtimeweek));
			timingBackupTelnetConfig.setBackup_time_day(arrayToString(sendtimeday));
			timingBackupTelnetConfig.setBackup_time_hou(arrayToString(sendtimehou));
			timingBackupTelnetConfig.setBackup_date(Integer.parseInt(dt.getMyDateTime(DateTime.Datetime_Format_14)));
			timingBackupTelnetConfig.setStatus(status);
			timingBackupTelnetConfig.setBkpType(bkpType);
			timingBackupTelnetConfig.setCheckupdateflag(checkbox);
			timingBackupTelnetConfigDao.save(timingBackupTelnetConfig);
			timingBackupTelnetConfigDao = new TimingBackupCfgFileDao();
			timingBackupTelnetConfigList = timingBackupTelnetConfigDao.getAlList();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timingBackupTelnetConfigDao.close();
		}
		request.setAttribute("timingBackupTelnetConfigList", timingBackupTelnetConfigList);
		return "/automation/cfgfile/timingBackup.jsp";
	}

	public String arrayToString(String[] array) {
		StringBuilder sb = new StringBuilder();
		if (array != null) {
			for (String value : array) {
				sb.append("/");
				sb.append(value);
			}
			sb.append("/");
		}
		return sb.toString();
	}

	/**
	 * ɾ����ʱ���������ļ�������
	 * 
	 * @return
	 */
	private String deleteTimingBackupCfgFile() {
		String[] ids = getParaArrayValue("checkbox");
		if (ids != null) {
			if (ids != null && ids.length != 0) {
				TimingBackupCfgFileDao timingBackupTelnetConfigDao = new TimingBackupCfgFileDao();
				try {
					for (String id : ids) {
						if (id != null && !"".equals(id)) {
							timingBackupTelnetConfigDao.deleteById(id);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					timingBackupTelnetConfigDao.close();
				}
			}
		}
		return ready_timingBackup();
	}
	private String bkpCfg() {
		String bkpType = this.getParaValue("bkptype");
		String id = getParaValue("id");// Huaweitelnetconf ������ID
		String fileName = this.getParaValue("fileName");
		String fileDesc = this.getParaValue("fileDesc");
        if (fileName==null) return null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HH-mm");
		String bkptime = "";
		Date bkpDate = new Date();
		String reg = "_(.*)cfg.cfg";
		Pattern p = Pattern.compile(reg);
		Matcher m = p.matcher(fileName);
		if (m.find()) {
			bkptime = m.group(1);
		}
		try {
			bkpDate = sdf.parse(bkptime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		NetCfgFileTelnet telnet=new NetCfgFileTelnet();
		
		String jsp=telnet.bkpCfgFile(id, bkpType, fileName, fileDesc, bkpDate);
		request.setAttribute("id", id);
		if (jsp == null) {
			this.setErrorCode(1003);// �û������������
		}
		return jsp;
	}
	private String readyBkpManage() {
		String id = this.getParaValue("id");
		String ipaddress = this.getParaValue("ipaddress");
		NetCfgFileDao h3Dao = new NetCfgFileDao();
		List list = h3Dao.loadByIp(ipaddress);
		h3Dao.close();
		request.setAttribute("list", list);
		request.setAttribute("id", id);
		request.setAttribute("ipaddress", ipaddress);
		return "/automation/cfgfile/bkpManage.jsp";
	}
	private String download() {
		String id = this.getParaValue("id");
		NetCfgFileDao h3Dao = new NetCfgFileDao();
		NetCfgFile h3 = (NetCfgFile) h3Dao.findByID(id);
		h3Dao.close();
		String pri=ResourceCenter.getInstance().getSysPath().replace("\\", "/")+ "cfg/" ;
		String filename =pri+ h3.getFileName();
		request.setAttribute("filename", filename);
		return "/automation/controller/download.jsp";
	}
	public String execute(String action) {
		if (action.equals("list")) {// �Զ�����ҳ�ڵ��б�
			return list();
		}
		if (action.equals("ready_add"))// �Զ�����ҳ�ڵ����ҳ��
		{
			return readyAdd();
		}
		if (action.equals("add")) // ��ӽڵ����
		{
			return add();
		}
		if (action.equals("delete")) // ɾ���ڵ�
		{
			return delete();
		}
		if (action.equals("update")) // ɾ���ڵ�
		{
			return update();
		}
		if (action.equals("synchronizeData")) // ͬ������
		{
			return synchronizeData();
		}
		if (action.equals("ready_edit")) {// �Զ�����ҳ �Ҽ��˵� �༭
			return ready_edit();
		}
		if (action.equals("netip")) {// ���豸�б���ѡ��IP
			return netip();
		}
		if (action.equals("bkpCfg")) {// ���������ļ�
			return bkpCfg();
		}
		
		if (action.equals("configlist")) {// �����豸�����ļ��б�
			return configlist();
		}
		if (action.equals("readyBackupConfig")) {// �������������ļ�
			return readyBackupConfig();
		}
		if (action.equals("ready_deployCfgForBatch")) {// �������������豸�����ļ�
			return ready_deployCfgForBatch();
		}
		if (action.equals("showAllFile")) {// ����IP����ʾ�������������ļ�
			return showAllFile();
		}
		if (action.equals("setBaseLine")) {// ���û��������ļ�
			return setBaseLine();
		}
		if (action.equals("compareContent")) {// �����ļ��ȶ�
			return compareContent();
		}
		if (action.equals("deleteFile")) {// ɾ�������ļ�
			return deleteFile();
		}
		if (action.equals("ready_backupForBatch")) {// ������������ҳ��
			return ready_backupForBatch();
		}
		if (action.equals("bkpCfg_forBatch")) {// ��������
			return bkpCfg_forBatch();
		}

		if (action.equals("ready_timingBackup")) {// ��ʱ����
			return ready_timingBackup();
		}
		if (action.equals("timingBackup")) {// ��ʱ����
			return timingBackup();
		}

		if (action.equals("addTimingBackupTelnetConfig")) {// ��������
			return addTimingBackupTelnetConfig();
		}
		if (action.equals("deleteTimingBackupCfgFile")) {// ɾ����ʱ��������
			return deleteTimingBackupCfgFile();
		}
		if (action.equals("queryById")) {
			return queryById();
		}
		if (action.equals("queryCfgFileById")) {
			return queryCfgFileById();
		}
		if (action.equals("readyBkpManage")) {
			return readyBkpManage();
		}
		if (action.equals("download")) {
			return download();
		}
		
		return null;
	}
}