package com.afunms.automation.model;

import com.afunms.common.base.BaseVo;
/**
 * 
 * @descrition �Զ������ýڵ�MODEL,topo_node_telnetconfig ���Ӧ��model
 * @author wangxiangyong
 * @date Aug 26, 2014 11:21:32 AM
 */
public class NetCfgFileNode extends BaseVo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;// id
	private String ipaddress;// ip��ַ
	private String alias;// ����
	private String user;// �û�
	private String password;// ����
	private String suuser;// su�û�
	private String supassword;// su����
	private int port;// �˿�
//	private String defaultpromtp;// ϵͳ��ʾ����
//	private int enablevpn; // �Ƿ�����vpn������Ϣ�ɼ�
//	private int isSynchronized;// �Ƿ�ͬ�� 1ͬ�� 0��ͬ��
	private String deviceRender;// �豸�ṩ�� h3c cisco
//	private String threeA;// 3A��֤
//	private int encrypt;// �Ƿ���� 1���� 2������
	private String ostype;
	private int connecttype; // ��½��ʽ 0:telnet 1:ssh

	public String getOstype() {
		return ostype;
	}

	public void setOstype(String ostype) {
		this.ostype = ostype;
	}

	

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDeviceRender() {
		return deviceRender;
	}

	public void setDeviceRender(String deviceRender) {
		this.deviceRender = deviceRender;
	}

	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	

	public String getSupassword() {
		return supassword;
	}

	public void setSupassword(String supassword) {
		this.supassword = supassword;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}



	public int getConnecttype() {
		return connecttype;
	}

	public void setConnecttype(int connecttype) {
		this.connecttype = connecttype;
	}

	



	public String getSuuser() {
		return suuser;
	}

	public void setSuuser(String suuser) {
		this.suuser = suuser;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
}