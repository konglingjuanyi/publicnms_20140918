package com.afunms.automation.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import com.afunms.automation.util.diff_match_patch.Diff;


/**
 * 
* @Company: ��������ɷݹ�˾
* @ClassName: ReaderFileLine 
* @Description: ���ж�ȡ�ļ� 
* @author Hexinlin 
* @date 2014-6-4 ����11:30:24
 */
public class ReaderFileLine {

	/**
	 * 
	* @Title: getFileContent 
	* @Description: ��ȡ�ļ���������ҳ����� 
	* @param @param file
	* @param @return
	* @param @throws Exception 
	* @return String 
	* @throws
	 */
	public static String getFileContent(File file) throws Exception {
		StringBuilder sb = new StringBuilder();
		System.out.println("============111111111111=======");
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader reader = new BufferedReader(read);
			String line;
			while((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("<br>");
			}
		
		return sb.toString();
	}

	/**
	 * 
	* @Title: getDiffFileContent 
	* @Description: ��ȡ�ļ����ݣ����ڱȽ� 
	* @param @param file
	* @param @return
	* @param @throws Exception 
	* @return String 
	* @throws
	 */
	public static String getDiffFileContent(File file) throws Exception {
		StringBuilder sb = new StringBuilder();
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader reader = new BufferedReader(read);
			String line;
			while((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
	}
	/**
	 * 
	* @Title: diffText 
	* @Description: �Ա������ı��Ĳ�֮ͬ������ʹ��html��ǩ���б�ע 
	* @param  path1
	* @param  path2
	* @throws Exception 
	* @return String[] 
	 */
	public static String [] diffText(File file1,File file2) throws Exception {
		diff_match_patch dmp = new diff_match_patch();
		String text1 = ReaderFileLine.getDiffFileContent(file1);
		String text2 = ReaderFileLine.getDiffFileContent(file2);;
		
		LinkedList<Diff> diffs = dmp.diff_main(text1, text2);
		dmp.diff_cleanupSemantic(diffs);
		String htmls[] = dmp.diff_Html(diffs);
		return htmls;
	}
	
}