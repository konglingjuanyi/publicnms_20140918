package com.afunms.automation.manage;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.afunms.alarm.dao.AlarmWayDao;
import com.afunms.alarm.model.AlarmWay;
import com.afunms.automation.dao.NetCfgFileAuditDao;
import com.afunms.automation.dao.NetCfgFileNodeDao;
import com.afunms.automation.dao.PasswdTimingConfigDao;
import com.afunms.automation.model.ConfiguringDevice;
import com.afunms.automation.model.NetCfgFileAudit;
import com.afunms.automation.model.NetCfgFileNode;
import com.afunms.automation.model.PasswdTimingConfig;
import com.afunms.automation.task.BatchModifyTask;
import com.afunms.automation.telnet.BaseTelnet;
import com.afunms.automation.telnet.CiscoTelnet;
import com.afunms.automation.telnet.NetTelnetUtil;
import com.afunms.capreport.common.DateTime;
import com.afunms.common.base.BaseManager;
import com.afunms.common.base.ManagerInterface;
import com.afunms.common.util.SessionConstant;
import com.afunms.system.model.User;

public class RemoteDeviceManager extends BaseManager implements ManagerInterface {
	static StringBuffer result = new StringBuffer();
	/**
	 * 
	 * @description Զ���豸�����޸��б�
	 * @author wangxiangyong
	 * @date Aug 30, 2014 4:34:44 PM
	 * @return String  
	 * @return
	 */
	public String passwdList() {
		List configingDeviceList = new ArrayList();
		NetCfgFileNodeDao netCfgFileNodeDao = new NetCfgFileNodeDao();
		this.list(netCfgFileNodeDao);
		List telnetConfList = (List) request.getAttribute("list");
		for (int i = 0; i < telnetConfList.size(); i++) {
			ConfiguringDevice cfgingDevice = new ConfiguringDevice();
			NetCfgFileNode telnetConf = (NetCfgFileNode) telnetConfList.get(i);
			cfgingDevice.setId(telnetConf.getId());
			cfgingDevice.setAlias(telnetConf.getAlias());
			cfgingDevice.setIpaddress(telnetConf.getIpaddress());
			cfgingDevice.setDeviceRender(telnetConf.getDeviceRender());
			configingDeviceList.add(cfgingDevice);
		}
		request.setAttribute("configlist", configingDeviceList);
		return "/automation/remote/passwdList.jsp";
	}
	/**
	 * 
	 * @description �Ҽ��˵��е� �޸����� ����
	 * @author wangxiangyong
	 * @date Aug 30, 2014 10:09:52 PM
	 * @return String  
	 * @return
	 */
	private String readyTelnetModify() {
		String id = this.getParaValue("id");
		NetCfgFileNodeDao telnetcfgdao = new NetCfgFileNodeDao();
		NetCfgFileNode telnetCfg = (NetCfgFileNode) telnetcfgdao.findByID(id);
		request.setAttribute("vpntelnetconf", telnetCfg);
		telnetcfgdao.close();
		String deviceType = telnetCfg.getDeviceRender();
		String jsp = "";
		if (deviceType.equals("h3c") || deviceType.equals("huawei"))// ����/��Ϊ
		{
			jsp = "/automation/remote/telnetmodifypassword.jsp?deviceType=" + deviceType;
		} else { // if(sysOid.equals("1.3.6.1.4.1.9.1.209"))//˼��
			jsp = "/automation/remote/cisco_telnet_modify.jsp?deviceType=" + deviceType;
		}
		request.setAttribute("deviceType", deviceType);
		return jsp;
	}
	/**
	 * 
	 * @description �޸�����
	 * @author wangxiangyong
	 * @date Aug 30, 2014 10:10:05 PM
	 * @return String  
	 * @return
	 */
	private String updatepassword() {

		String deviceType = this.getParaValue("deviceType");

		if (deviceType.equals("h3c"))// h3c
		{
			String id = this.getParaValue("id");
			String modifyuser = this.getParaValue("modifyuser");
			String newpassword = this.getParaValue("newpassword");
			int encrypt = this.getParaIntValue("encrypt");
			String threeA = this.getParaValue("threeA");

			NetCfgFileNodeDao telnetcfgdao = new NetCfgFileNodeDao();
			NetCfgFileNode hmo = (NetCfgFileNode) telnetcfgdao.findByID(id);
			telnetcfgdao.close();

			NetTelnetUtil tvpn = new NetTelnetUtil();
			tvpn.setSuUser(hmo.getSuuser());// su
			tvpn.setSuPassword(hmo.getSupassword());// su����
			tvpn.setUser(hmo.getUser());// �û�
			tvpn.setPassword(hmo.getPassword());// ����
			tvpn.setIp(hmo.getIpaddress());// ip��ַ
//			tvpn.setDEFAULT_PROMPT(hmo.getDefaultpromtp());// ������Ƿ���
			tvpn.setPort(hmo.getPort());
			boolean b = tvpn.modifypassowd(modifyuser, newpassword, encrypt, threeA, hmo.getOstype());

			if (b) {
				if (modifyuser.equals("su")) {
					hmo.setSupassword(newpassword);
				} else {
					hmo.setUser(modifyuser);
					hmo.setPassword(newpassword);
				}
				NetCfgFileNodeDao hdao = new NetCfgFileNodeDao();
				hdao.update(hmo);
				hdao.close();
				return "/automation/common/status.jsp";
			} else {
				setErrorCode(0);
				return null;
			}

		} else if (deviceType.equals("cisco") ) {

			String id = this.getParaValue("id");
			String modifyuser = this.getParaValue("modifyuser");
			String newpassword = this.getParaValue("newpassword");
			NetCfgFileNodeDao telnetcfgdao = new NetCfgFileNodeDao();

			NetCfgFileAudit hmoa = new NetCfgFileAudit();
			NetCfgFileNodeDao dao = new NetCfgFileNodeDao();
			User user = (User) session.getAttribute(SessionConstant.CURRENT_USER);
			int userid = user.getId();
			String username = user.getName();
			String oldPassword = this.getParaValue("password");
			String newPassword = this.getParaValue("newpassword");
			hmoa.setIp(getParaValue("ipaddress"));
			hmoa.setUserid(userid);
			hmoa.setUsername(username);
			hmoa.setOldpassword(oldPassword);
			hmoa.setNewpassword(newPassword);
			dao.save(hmoa);
			NetCfgFileNode hmo = null;
			try {
				hmo = (NetCfgFileNode) telnetcfgdao.findByID(id);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				telnetcfgdao.close();
				dao.close();
			}

			CiscoTelnet ciscoTelnet = new CiscoTelnet(hmo.getIpaddress(), hmo.getUser(), hmo.getPassword(), hmo.getPort());
			if (ciscoTelnet.tologin()) {
				if (ciscoTelnet.modifyPasswd(hmo.getSupassword(), modifyuser, newpassword)) {
					hmo.setUser(modifyuser);
					hmo.setPassword(newpassword);
					NetCfgFileNodeDao tdao = new NetCfgFileNodeDao();
					try {
						tdao.update(hmo);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						tdao.close();
					}
					return "/automation/common/status.jsp";
				} else {
					setErrorCode(-1);

					return "/automation/common/error.jsp";
				}
			} else {
				setErrorCode(-1);
				return "/automation/common/error.jsp";
			}
		} else if (deviceType.equals("zte")|| deviceType.equals("redgiant")||deviceType.equals("huawei")) {
			String id = this.getParaValue("id");
			String modifyuser = this.getParaValue("modifyuser");
			String newpassword = this.getParaValue("newpassword");
			String encrypt = this.getParaValue("encrypt");
			String threeA = this.getParaValue("threeA");
			NetCfgFileNodeDao telnetcfgdao = new NetCfgFileNodeDao();

			NetCfgFileAudit hmoa = new NetCfgFileAudit();
			NetCfgFileAuditDao dao = new NetCfgFileAuditDao();
			User user = (User) session.getAttribute(SessionConstant.CURRENT_USER);
			int userid = user.getId();
			String username = user.getName();
			String oldPassword = this.getParaValue("password");
			String newPassword = this.getParaValue("newpassword");
			hmoa.setIp(getParaValue("ipaddress"));
			hmoa.setUserid(userid);
			hmoa.setUsername(username);
			hmoa.setOldpassword(oldPassword);
			hmoa.setNewpassword(newPassword);
			dao.save(hmoa);
			NetCfgFileNode vo = null;
			try {
				vo = (NetCfgFileNode) telnetcfgdao.findByID(id);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				telnetcfgdao.close();
				dao.close();
			}

			BaseTelnet telnet = new BaseTelnet(vo.getIpaddress(), vo.getUser(), vo.getPassword(), vo.getPort(), vo.getSuuser(), vo.getSupassword());
			String sign=telnet.login();
			if (sign.indexOf("�ɹ�")>-1) {
				if (telnet.modifyDevPasswd(deviceType, modifyuser, newpassword,encrypt)) {
					
					vo.setUser(modifyuser);
					vo.setPassword(newpassword);
					NetCfgFileNodeDao tdao = new NetCfgFileNodeDao();
					try {
						tdao.update(vo);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						tdao.close();
					}
					return "/automation/common/status.jsp";
				} else {
					setErrorCode(-1);
					return "/automation/common/error.jsp";
				}
			} else {
				setErrorCode(-1);
				return "/automation/common/error.jsp";
			}
		} else {
			setErrorCode(-1);
			return "/automation/common/error.jsp";
		}
	}
	/**
	 * 
	 * @description �����޸��������
	 * @author wangxiangyong
	 * @date Aug 30, 2014 11:31:35 PM
	 * @return String  
	 * @return
	 */
	private String ready_multi_modify() {
		return "/automation/remote/multi_telnetmodifypassword.jsp";
	}
	/**
	 * 
	 * @description  �����޸��豸�������
	 * @author wangxiangyong
	 * @date Aug 30, 2014 11:34:24 PM
	 * @return String  
	 * @return
	 */
	private String modifyTelnetPasswordForBatch() {
		String ipAddresses = request.getParameter("ipaddress");
		String modifyuser = this.getParaValue("modifyuser");
		String threeA = this.getParaValue("threeA");
		int encrypt = this.getParaIntValue("encrypt");
		String newpassword = this.getParaValue("newpassword");
		String[] split = ipAddresses.substring(1).split(",");

		/**
		 * �̳߳�
		 * 
		 * @param �̳߳�ά���̵߳���������
		 * @param �̳߳�ά���̵߳��������
		 * @param �̳߳�ά���߳�������Ŀ���ʱ��
		 * @param �̳߳�ά���߳�������Ŀ���ʱ��ĵ�λ
		 * @param �̳߳���ʹ�õĻ������
		 * @param �̳߳ضԾܾ�����Ĵ������
		 *            ,�˴��Ĳ����� ������ӵ�ǰ�����������Զ��ظ�����execute()����
		 * @author GZM
		 */
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
		for (int i = 0; i < list.size(); i++) {
			threadPool.execute(new BatchModifyTask(result, (NetCfgFileNode) list.get(i), modifyuser, newpassword, threeA, encrypt));
		}
		threadPool.shutdown();
		try {
			boolean loop = true;
			do { // �ȴ������������
				loop = !threadPool.awaitTermination(2, TimeUnit.SECONDS);
			} while (loop);
		} catch (Exception e) {
		}

		/*
		 * while(threadPool.getActiveCount() == 0)//��������ִ������Ľ����߳���,Ϊ0
		 * ˵�����������Ѿ�ִ���� { try{ Thread.sleep(1000); }catch(Exception
		 * e){e.printStackTrace();} }
		 */
		System.out.println(result.toString());
		request.setAttribute("result", result.toString());
		return "/automation/cfgfile/multi_modify_status.jsp";
	}
	/**
	 * 
	 * @description ���붨ʱ����б�
	 * 
	 * @author wangxiangyong
	 * @date Aug 30, 2014 11:46:28 PM
	 * @return String  
	 * @return
	 */
	private String deviceList() {
		PasswdTimingConfigDao passwdTimingBackupTelnetConfigDao = new PasswdTimingConfigDao();
		List<PasswdTimingConfig> passwdTimingBackupTelnetConfigList = null;
		try {
			passwdTimingBackupTelnetConfigList = passwdTimingBackupTelnetConfigDao.getAlList();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			passwdTimingBackupTelnetConfigDao.close();
		}

		String nodeid = getParaValue("nodeid");
		String type = getParaValue("type");
		String subtype = getParaValue("subtype");
		String id = getParaValue("id");
		Hashtable<String, String> alarmWayHashtable = new Hashtable<String, String>();

		AlarmWayDao alarmWayDao = new AlarmWayDao();
		try {
			List list = alarmWayDao.loadAll();
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					AlarmWay way = (AlarmWay) list.get(i);
					if (way != null) {
						alarmWayHashtable.put(way.getId() + "", way.getName());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			alarmWayDao.close();
		}
		// }
		request.setAttribute("alarmWayHashtable", alarmWayHashtable);
		// request.setAttribute("alarmIndicatorsNode", alarmIndicatorsNode);
		request.setAttribute("nodeid", nodeid);
		request.setAttribute("type", type);
		request.setAttribute("valTest", "valTest");
		request.setAttribute("subtype", subtype);
		request.setAttribute("list", passwdTimingBackupTelnetConfigList);
		return "/automation/remote/device_list.jsp";
	}
	/**
	 * 
	 * @description ����������
	 * @author wangxiangyong
	 * @date Aug 30, 2014 11:58:43 PM
	 * @return String  
	 * @return
	 */
	private String ready_multi_audit() {
		NetCfgFileAuditDao haweitelnetconfAuditDao = new NetCfgFileAuditDao();

		this.list(haweitelnetconfAuditDao);
		List<NetCfgFileAudit> auditlist = (List<NetCfgFileAudit>) request.getAttribute("list");
		request.setAttribute("list", auditlist);
		return "/automation/remote/passwdAudit.jsp";
	}
	/**
	 * 
	 * @description ��Ʋ�ѯ
	 * @author wangxiangyong
	 * @date Aug 31, 2014 10:48:08 PM
	 * @return String  
	 * @return
	 */
	private String queryByCondition() {
		

		String key = getParaValue("key");
		String value = getParaValue("value");
		NetCfgFileAuditDao haweitelnetconfAuditDao = new NetCfgFileAuditDao();

		request.setAttribute("key", key);
		request.setAttribute("value", value);
		
		return list(haweitelnetconfAuditDao, " where " + key + " = '" + value + "'");
	}
	/**
	 * 
	 * @description ������ʱ�޸�
	 * @author wangxiangyong
	 * @date Aug 31, 2014 11:04:56 PM
	 * @return String  
	 * @return
	 */
	private String addPasswdBackup() {
		String id = getParaValue("id");
		List<PasswdTimingConfig> passwdTimingBackupTelnetConfigList = null;
		PasswdTimingConfigDao passwdTimingConfigDao = new PasswdTimingConfigDao();
		try {
			passwdTimingConfigDao.updateStatus("��", id);
			passwdTimingBackupTelnetConfigList = passwdTimingConfigDao.getAlList();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			passwdTimingConfigDao.close();
		}
		request.setAttribute("list", passwdTimingBackupTelnetConfigList);
		return "/automation/remote/device_list.jsp";
	}
	/**
	 * 
	 * @description ȡ��������ʱ�޸�
	 * @author wangxiangyong
	 * @date Aug 31, 2014 11:07:26 PM
	 * @return String  
	 * @return
	 */
	private String disPasswdBackup() {
		String id = getParaValue("id");
		List<PasswdTimingConfig> passwdTimingBackupTelnetConfigList = null;
		PasswdTimingConfigDao PasswdTimingBackupTelnetConfigDao = new PasswdTimingConfigDao();
		try {
			PasswdTimingBackupTelnetConfigDao.updateStatus("��", id);
			passwdTimingBackupTelnetConfigList = PasswdTimingBackupTelnetConfigDao.getAlList();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			PasswdTimingBackupTelnetConfigDao.close();
		}
		request.setAttribute("list", passwdTimingBackupTelnetConfigList);
		return "/automation/remote/device_list.jsp";
	}
	/**
	 * 
	 * @description ���붨ʱ������ҳ��
	 * @author wangxiangyong
	 * @date Sep 1, 2014 8:13:55 AM
	 * @return String  
	 * @return
	 */
	private String ready_addPasswd() {
		return "/automation/remote/addPasswdTimingCfg.jsp";
	}
	/**
	 * 
	 * @description ���ѷ�ʽ
	 * @author wangxiangyong
	 * @date Sep 16, 2014 1:03:09 PM
	 * @return String  
	 * @return
	 */
	public String chooselist() {
		String jsp = "/automation/remote/chooselist.jsp";
		String alarmWayIdEvent = getParaValue("alarmWayIdEvent");
		String alarmWayNameEvent = getParaValue("alarmWayNameEvent");
		try {
			List list = getList();
			request.setAttribute("list", list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		request.setAttribute("alarmWayIdEvent", alarmWayIdEvent);
		request.setAttribute("alarmWayNameEvent", alarmWayNameEvent);
		return jsp;
	}
	public List getList() {
		String sqlQuery = "";

		AlarmWayDao alarmWayDao = new AlarmWayDao();
		try {
			list(alarmWayDao, sqlQuery);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			alarmWayDao.close();
		}
		List list = (List) request.getAttribute("list");
		return list;
	}
	private String deletePasswdTimingCfg() {
		String[] ids = getParaArrayValue("checkbox");
		if (ids != null) {
			if (ids != null && ids.length != 0) {
				PasswdTimingConfigDao passwdTimingBackupTelnetConfigDao = new PasswdTimingConfigDao();
				try {
					for (String id : ids) {
						if (id != null && !"".equals(id)) {
							passwdTimingBackupTelnetConfigDao.delete(id);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					passwdTimingBackupTelnetConfigDao.close();
				}
			}
		}
		return deviceList();
	}
	/**
	 * 
	 * @description ��ʱִ��ҳ���У����ѡ���豸��ִ�и÷���
	 * @author wangxiangyong
	 * @date Sep 1, 2014 10:52:00 AM
	 * @return String  
	 * @return
	 */
	private String multi_telnet_netip() {
		NetCfgFileNodeDao netCfgFileNodeDao = new NetCfgFileNodeDao();
		String deviceType=getParaValue("deviceType");
		StringBuffer whereSql=new StringBuffer();
		whereSql.append("select * from automation_node ");
		if(deviceType!=null&&!deviceType.equals("null")){
			whereSql.append("where device_render='"+deviceType+"'");
		}
		List list = null;
		try {
			list = netCfgFileNodeDao.findByCriteria(whereSql.toString());
		} catch (RuntimeException e) {
			e.printStackTrace();
		} finally {
			netCfgFileNodeDao.close();
		}
		request.setAttribute("list", list);
		return "/automation/remote/multi_telnet_netip.jsp";
	}
	
	private String savePasswdTimingBackup() {
		PasswdTimingConfigDao passwdTimingBackupTelnetConfigDao = new PasswdTimingConfigDao();
		PasswdTimingConfig passwdTimingBackupTelnetConfig = new PasswdTimingConfig();
		String ipaddress = getParaValue("ipaddress");
		String warntype = getParaValue("way-id");
		String backup_fileName = getParaValue("filename");
		String backup_type = getParaValue("type");
		String[] sendtimemonth = request.getParameterValues("sendtimemonth");
		String[] sendtimeweek = request.getParameterValues("sendtimeweek");
		String[] sendtimeday = request.getParameterValues("sendtimeday");
		String[] sendtimehou = request.getParameterValues("sendtimehou");
		String[] transmitfrequency = request.getParameterValues("transmitfrequency");
		String status = getParaValue("status");
		List<PasswdTimingConfig> passwdTimingBackupTelnetConfigList = null;

		DateTime dt = new DateTime();
		try {
			passwdTimingBackupTelnetConfig.setTelnetconfigids(ipaddress);
			passwdTimingBackupTelnetConfig.setWarntype(warntype);
			passwdTimingBackupTelnetConfig.setBackup_filename(backup_fileName);
			passwdTimingBackupTelnetConfig.setBackup_type(backup_type);
			passwdTimingBackupTelnetConfig.setBackup_sendfrequency(arrayToString(transmitfrequency).substring(1, arrayToString(transmitfrequency).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_time_month(arrayToString(sendtimemonth).substring(1, arrayToString(sendtimemonth).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_time_week(arrayToString(sendtimeweek).substring(1, arrayToString(sendtimeweek).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_time_day(arrayToString(sendtimeday).substring(1, arrayToString(sendtimeday).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_time_hou(arrayToString(sendtimehou).substring(1, arrayToString(sendtimehou).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_date(Integer.parseInt(dt.getMyDateTime(DateTime.Datetime_Format_14)));
			passwdTimingBackupTelnetConfig.setStatus(status);
			passwdTimingBackupTelnetConfigDao.save(passwdTimingBackupTelnetConfig);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			passwdTimingBackupTelnetConfigDao.close();
		}
		return deviceList();
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
	 * 
	 * @description ���붨ʱ����б�༭
	 * @author wangxiangyong
	 * @date Sep 1, 2014 11:06:37 AM
	 * @return String  
	 * @return
	 */
	private String ready_editPasswdTimingCfg() {
		
		String id = getParaValue("id");
		PasswdTimingConfigDao passwdTimingBackupTelnetConfigDao = new PasswdTimingConfigDao();
		PasswdTimingConfig passwdTimingBackupTelnetConfig = null;
		try {
			passwdTimingBackupTelnetConfig = (PasswdTimingConfig) passwdTimingBackupTelnetConfigDao.findByID(id);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			passwdTimingBackupTelnetConfigDao.close();
		}
		request.setAttribute("id", id);
		request.setAttribute("list", passwdTimingBackupTelnetConfig);
		return "/automation/remote/editPasswdTimingCfg.jsp";
	}
	private String editPasswdTimingCfg () {
		
		String id = getParaValue("id");
		PasswdTimingConfigDao passwdTimingConfigDao = new PasswdTimingConfigDao();
		PasswdTimingConfig passwdTimingConfig = null;
		try {
			passwdTimingConfig = (PasswdTimingConfig) passwdTimingConfigDao.findByID(id);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			passwdTimingConfigDao.close();
		}
		PasswdTimingConfigDao passwdTimingBackupTelnetConfigDao = new PasswdTimingConfigDao();
		PasswdTimingConfig passwdTimingBackupTelnetConfig = new PasswdTimingConfig();
		String ipaddress = getParaValue("ipaddress");
		String warntype = getParaValue("way-id");
		String backup_fileName = getParaValue("filename");
		String backup_type = getParaValue("type");
		String[] sendtimemonth = request.getParameterValues("sendtimemonth");
		String[] sendtimeweek = request.getParameterValues("sendtimeweek");
		String[] sendtimeday = request.getParameterValues("sendtimeday");
		String[] sendtimehou = request.getParameterValues("sendtimehou");
		String[] transmitfrequency = request.getParameterValues("transmitfrequency");
		String status = getParaValue("status");
		List<PasswdTimingConfig> passwdTimingBackupTelnetConfigList = null;

		DateTime dt = new DateTime();
		try {
			passwdTimingBackupTelnetConfig.setTelnetconfigids(ipaddress);
			passwdTimingBackupTelnetConfig.setWarntype(warntype);
			passwdTimingBackupTelnetConfig.setBackup_filename(backup_fileName);
			passwdTimingBackupTelnetConfig.setBackup_type(backup_type);
			passwdTimingBackupTelnetConfig.setBackup_sendfrequency(arrayToString(transmitfrequency).substring(1, arrayToString(transmitfrequency).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_time_month(arrayToString(sendtimemonth).substring(1, arrayToString(sendtimemonth).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_time_week(arrayToString(sendtimeweek).substring(1, arrayToString(sendtimeweek).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_time_day(arrayToString(sendtimeday).substring(1, arrayToString(sendtimeday).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_time_hou(arrayToString(sendtimehou).substring(1, arrayToString(sendtimehou).length() - 1));
			passwdTimingBackupTelnetConfig.setBackup_date(Integer.parseInt(dt.getMyDateTime(DateTime.Datetime_Format_14)));
			passwdTimingBackupTelnetConfig.setStatus(status);
			passwdTimingBackupTelnetConfigDao.save(passwdTimingBackupTelnetConfig);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			passwdTimingBackupTelnetConfigDao.close();
		}
		return deviceList();
	}
	/**
	 * 
	 * @description Զ���豸�����޸��б�
	 * @author wangxiangyong
	 * @date Aug 30, 2014 4:34:44 PM
	 * @return String  
	 * @return
	 */
	public String queryNodesListById() {
		String id=getParaValue("id");
		NetCfgFileNodeDao netCfgFileNodeDao = new NetCfgFileNodeDao();
		this.list(netCfgFileNodeDao);
		
		List configingDeviceList = new ArrayList();
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
		return "/automation/remote/passwdList.jsp";
	}
	public String execute(String action) {
		if(action.equals("passwdList")){
			return passwdList();
		}
		if(action.equals("readyTelnetModify")){
			return readyTelnetModify();
		}
		if(action.equals("updatepassword")){
			return updatepassword();
		}
		if(action.equals("ready_multi_modify")){//�����޸��������
			return ready_multi_modify();
		}
		if (action.equals("multi_modify"))// list.jspҳ��->�����޸�����->�޸İ�ť
		{
			return modifyTelnetPasswordForBatch();
		}
		if (action.equals("deviceList"))//���붨ʱ����б�
		{
			return deviceList();
		}
		if (action.equals("ready_multi_audit"))//����������
		{
			return ready_multi_audit();
		}
		if (action.equals("queryByCondition"))//��Ʋ�ѯ
		{
			return queryByCondition();
		}
		if (action.equals("addPasswdBackup"))//��Ʋ�ѯ
		{
			return addPasswdBackup();
		}
		if (action.equals("disPasswdBackup"))//��Ʋ�ѯ
		{
			return disPasswdBackup();
		}
		if (action.equals("ready_addPasswd"))//���붨ʱ������
		{
			return ready_addPasswd();
		}
		if (action.equals("deletePasswdTimingCfg"))//���붨ʱ���ɾ��
		{
			return deletePasswdTimingCfg();
		}
		if (action.equals("multi_telnet_netip"))//��ʱִ��ҳ���У����ѡ���豸��ִ�и÷���
		{
			return multi_telnet_netip();
		}
		if (action.equals("ready_editPasswdTimingCfg"))//���붨ʱ����༭
		{
			return ready_editPasswdTimingCfg();
		}
		if (action.equals("queryNodesListById"))//���붨ʱ����༭
		{
			return queryNodesListById();
		}
		if (action.equals("chooselist"))//���붨ʱ����༭
		{
			return chooselist();
		}
		if (action.equals("savePasswdTimingBackup"))//�������붨ʱ����ڵ�
		{
			return savePasswdTimingBackup();
		}
		if (action.equals("editPasswdTimingCfg"))//�������붨ʱ����༭
		{
			return editPasswdTimingCfg();
		}
		
		return null;
	}

}
