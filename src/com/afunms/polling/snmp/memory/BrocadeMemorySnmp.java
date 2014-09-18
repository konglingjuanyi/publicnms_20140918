package com.afunms.polling.snmp.memory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.afunms.common.util.CheckEventUtil;
import com.afunms.common.util.ShareData;
import com.afunms.common.util.SnmpUtils;
import com.afunms.common.util.SysLogger;
import com.afunms.indicators.model.NodeDTO;
import com.afunms.indicators.model.NodeGatherIndicators;
import com.afunms.indicators.util.NodeUtil;
import com.afunms.monitor.executor.base.SnmpMonitor;
import com.afunms.monitor.item.base.MonitoredItem;
import com.afunms.polling.PollingEngine;
import com.afunms.polling.base.Node;
import com.afunms.polling.node.Host;
import com.afunms.polling.om.Memorycollectdata;
import com.afunms.system.util.TimeGratherConfigUtil;
import com.afunms.topology.model.HostNode;
import com.gatherResulttosql.NetHostMemoryRtsql;
import com.gatherResulttosql.NetmemoryResultTosql;

public class BrocadeMemorySnmp extends SnmpMonitor {

	java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 
	 */
	public BrocadeMemorySnmp() {
	}

	public void collectData(Node node, MonitoredItem item) {

	}

	public void collectData(HostNode node) {

	}

	public Hashtable collect_Data(NodeGatherIndicators nodeGatherIndicators) {
		// yangjun
		Hashtable returnHash = new Hashtable();
		Vector memoryVector = new Vector();
		List memoryList = new ArrayList();
		Host node = (Host) PollingEngine.getInstance().getNodeByID(Integer.parseInt(nodeGatherIndicators.getNodeid()));
		if (node == null)
			return returnHash;
		// �ж��Ƿ��ڲɼ�ʱ�����
		if (ShareData.getTimegatherhash() != null) {
			if (ShareData.getTimegatherhash().containsKey(node.getId() + ":equipment")) {
				TimeGratherConfigUtil timeconfig = new TimeGratherConfigUtil();
				int _result = 0;
				_result = timeconfig.isBetween((List) ShareData.getTimegatherhash().get(node.getId() + ":equipment"));
				if (_result == 1) {
					// SysLogger.info("########ʱ�����: ��ʼ�ɼ�
					// "+node.getIpAddress()+" PING������Ϣ##########");
				} else if (_result == 2) {
					// SysLogger.info("########ȫ��: ��ʼ�ɼ� "+node.getIpAddress()+"
					// PING������Ϣ##########");
				} else {
					SysLogger.info("######## " + node.getIpAddress() + " ���ڲɼ��ڴ�ʱ�����,�˳�##########");
					// ���֮ǰ�ڴ��в����ĸ澯��Ϣ
					try {
						// ���֮ǰ�ڴ��в������ڴ�澯��Ϣ
						NodeDTO nodedto = null;
						NodeUtil nodeUtil = new NodeUtil();
						nodedto = nodeUtil.creatNodeDTOByHost(node);
						CheckEventUtil checkutil = new CheckEventUtil();
						checkutil.deleteEvent(node.getId() + "", nodedto.getType(), nodedto.getSubtype(), "memory", null);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return returnHash;
				}

			}
		}
		try {
			Calendar date = Calendar.getInstance();

			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				com.afunms.polling.base.Node snmpnode = (com.afunms.polling.base.Node) PollingEngine.getInstance().getNodeByIP(node.getIpAddress());
				Date cc = date.getTime();
				String time = sdf.format(cc);
				snmpnode.setLastTime(time);
			} catch (Exception e) {

			}
			// -------------------------------------------------------------------------------------------�ڴ�
			// start
			try {
				String temp = "0";
				String memtype = "";// ���������� memp �����ٷֱȣ�memsize ������С
				// String[][] valueArray = null;
				// String[] oids = new String[] { ".1.3.6.1.4.1.2021.4.5",//
				// hwMemSize
				// ".1.3.6.1.4.1.2021.4.11"// hwMemFree
				// };
				// memtype = "memsize";
				String[] oids = new String[] { "1.3.6.1.4.1.1588.2.1.1.1.26.6" };//
				if (node.getSysOid().startsWith("1.3.6.1.4.1.1588.")) {
					oids = new String[] { "1.3.6.1.4.1.1588.2.1.1.1.26.6"
					// �ڴ�������
					};
					
				}
				memtype = "memp";
				String[][] valueArray = SnmpUtils.getTableData(node.getIpAddress(), node.getCommunity(), oids, node.getSnmpversion(), 3, 1000 * 30);
				// valueArray =
				// SnmpUtils.getTemperatureTableData(node.getIpAddress(),
				// node.getCommunity(), oids, node.getSnmpversion(),
				// node.getSecuritylevel(), node.getSecurityName(),
				// node.getV3_ap(), node.getAuthpassphrase(),
				// node.getV3_privacy(),
				// node.getPrivacyPassphrase(), 3, 1000 * 30);
				int allvalue = 0;

				int flag = 0;
				if (valueArray != null && valueArray.length > 0) {
					// ���ݲ�ͬ���������ж�
					if (memtype.equals("memp")) {
						for (int i = 0; i < valueArray.length; i++) {
							String _value = valueArray[i][0];
							String index = i+"";
							int value = 0;
							try {
								value = Integer.parseInt(_value);
							} catch (Exception e) {
							}
							try {
								allvalue = allvalue + value;
								// SysLogger.info(host.getIpAddress()+" �ڴ棺
								// "+Integer.parseInt(value+"")+"
								// ���ڴ�:"+allvalue);
							} catch (Exception e) {
								SysLogger.error("JuniperMemorySnmp error", e);
							}
							if (value > 0) {
								flag = flag + 1;
								List alist = new ArrayList();
								alist.add(index);
								alist.add(_value);
								// �ڴ�
								memoryList.add(alist);
								Memorycollectdata memorycollectdata = new Memorycollectdata();
								memorycollectdata.setIpaddress(node.getIpAddress());
								memorycollectdata.setCollecttime(date);
								memorycollectdata.setCategory("Memory");
								memorycollectdata.setEntity("Utilization");
								memorycollectdata.setSubentity(index);
								memorycollectdata.setRestype("dynamic");
								memorycollectdata.setUnit("%");
								memorycollectdata.setThevalue(_value + "");
								// SysLogger.info(host.getIpAddress()+"
								// ������"+index+" �ڴ棺
								// "+Integer.parseInt(_value+""));
								memoryVector.addElement(memorycollectdata);
							}
						}

					}

					if (memtype.equals("memsize")) {
						for (int i = 0; i < valueArray.length; i++) {
							String sizevalue = valueArray[i][1];
							String freevalue = valueArray[i][0];
							String index = i + "";
							float value = 0.0f;
							String usedperc = "0";
							if (Long.parseLong(sizevalue) > 0)
								value = (Long.parseLong(sizevalue) - Long.parseLong(freevalue)) * 100 / (Long.parseLong(sizevalue));

							if (value > 0) {
								int intvalue = Math.round(value);
								allvalue = allvalue + intvalue;
								// SysLogger.info(host.getIpAddress()+" �ڴ棺
								// "+Integer.parseInt(intvalue+"")+"
								// ���ڴ�:"+allvalue);
								flag = flag + 1;
								List alist = new ArrayList();
								alist.add("");
								alist.add(usedperc);
								// �ڴ�
								memoryList.add(alist);
								Memorycollectdata memorycollectdata = new Memorycollectdata();
								memorycollectdata.setIpaddress(node.getIpAddress());
								memorycollectdata.setCollecttime(date);
								memorycollectdata.setCategory("Memory");
								memorycollectdata.setEntity("Utilization");
								memorycollectdata.setSubentity(index);
								memorycollectdata.setRestype("dynamic");
								memorycollectdata.setUnit("%");
								memorycollectdata.setThevalue(intvalue + "");
								// SysLogger.info(host.getIpAddress()+" �ڴ棺
								// "+Integer.parseInt(intvalue+""));
								memoryVector.addElement(memorycollectdata);
							}
						}
					}

//					if (flag > 0)
//						usedvalueperc = allvalue / flag;
				}

			} catch (Exception e) {
				SysLogger.error("BrocadeMemorySnmp collectdata error1", e);
			}
			// -------------------------------------------------------------------------------------------�ڴ�
			// end
		} catch (Exception e) {
			SysLogger.error("BrocadeMemorySnmp collectdata error2", e);
		}

		if (!(ShareData.getSharedata().containsKey(node.getIpAddress()))) {
			Hashtable ipAllData = new Hashtable();
			if (ipAllData == null)
				ipAllData = new Hashtable();
			if (memoryVector != null && memoryVector.size() > 0)
				ipAllData.put("memory", memoryVector);
			ShareData.getSharedata().put(node.getIpAddress(), ipAllData);
		} else {
			if (memoryVector != null && memoryVector.size() > 0)
				((Hashtable) ShareData.getSharedata().get(node.getIpAddress())).put("memory", memoryVector);

		}
		returnHash.put("memory", memoryVector);

		Hashtable collectHash = new Hashtable();
		collectHash.put("memory", memoryVector);

		try {
			if (memoryVector != null && memoryVector.size() > 0) {
				int thevalue = 0;
				for (int i = 0; i < memoryVector.size(); i++) {
					Memorycollectdata memorycollectdata = (Memorycollectdata) memoryVector.get(i);
					if ("Utilization".equals(memorycollectdata.getEntity())) {
						if (Integer.parseInt(memorycollectdata.getThevalue()) > thevalue) {
							thevalue = Integer.parseInt(memorycollectdata.getThevalue());
						}
					}
				}
				CheckEventUtil checkutil = new CheckEventUtil();
				checkutil.updateData(node, nodeGatherIndicators, thevalue + "", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			SysLogger.error("j �ڴ� �澯����", e);
		}
		memoryVector = null;
		// �Ѳɼ��������sql
		NetmemoryResultTosql tosql = new NetmemoryResultTosql();
		tosql.CreateResultTosql(returnHash, node.getIpAddress());
		String runmodel = PollingEngine.getCollectwebflag();// �ɼ������ģʽ
		if (!"0".equals(runmodel)) {
			// �ɼ�������Ƿ���ģʽ,����Ҫ����������д����ʱ����
			NetHostMemoryRtsql totempsql = new NetHostMemoryRtsql();
			totempsql.CreateResultTosql(returnHash, node);
		}
		return returnHash;
	}

}
