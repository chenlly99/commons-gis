package com.chenlly.gis.io.sdo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chenlly.tools.util.DBINPUTUtil;
import com.chenlly.tools.util.DBUtil;

/**
 * @author ligang  ligang@mapbar.com
 *下午5:49:11
 */
public class BatMark {
	
	public static Connection conn = null;
	public static List<File> fileStartList = new ArrayList<File>();//建库文件
	public static Map<String,File> fileStartList2  = new HashMap<String, File> ();
	public static Map<String,String> addceng = new HashMap<String,String>();
	public static  String logf = "E:\\workspace\\YCDCtotal\\log.txt";
	public static List<String> citylist = new ArrayList<String>();
	public static List<String> tclist = new ArrayList<String>();
	public static int treadnum = 5;
	static String input  ="\\\\192.168.9.104\\table\\data\\input";
	public static FileWriter fwlog ;
	static String tablename = "";
	static String batout = "E:\\workspace\\YCDCtotal\\data8";
	
	
	public static void main(String[] args){
		BatMark mifdb = new BatMark();
		if(args.length>=1&&args[0]!=null){
			batout = args[0];
			input = args[1];
		}
		install();
		getStartFiles2(input);
		List<String> listinputf = new ArrayList<String>();
		for(int i=0;i<fileStartList.size();i++){
			String path = fileStartList.get(i).getPath();
			String name = fileStartList.get(i).getName();

			String tablename = mifName2tableName(name);
           // System.out.println(tablename+"-----"+fileStartList2.get(tablename));
				listinputf.add(path+" "+tablename);
				if(fileStartList2.containsKey(tablename)){
					File f = fileStartList2.get(tablename);
					listinputf.add(f.getPath()+" "+tablename);
				}
			

		}
		
/*		for(Object o : fileStartList2.keySet()){
			File f = fileStartList2.get(o);
			String tmp3 = o.toString();
			String tablename = tmp3;
			listinputf.add(f.getPath()+" "+tablename);
		}*/
		
		int size = listinputf.size();
		int ms = 0;
		if(size%treadnum==0){
			ms = size/treadnum;
		}else{
			ms = size/treadnum-1;
		}
			
		try{
			String batml1 = "set classpath=.;./lib/commons-lang-2.1.jar;./lib/jts-1.12.jar;./lib/jtsio-1.12.jar;./lib/junit-3.8.1.jar;./lib/log4j-1.2.9.jar;./lib/mapbar-tech-tool-common-MidMifCheck-release-1.0.1-20120705.jar;./lib/mapbar-tech-tool-gengxin-DCtotal-release-1.0.3-20120606.jar;./lib/ojdbc14-10.1.0.2.0.jar;./lib/sdoapi.jar;./lib/sdoutl.jar;./lib/stax-api-1.0.1.jar\r\n";
			String javaml = "";//"java -Xmx1024m  com.chenlly.tools.db.MIF2DB ";
			String jw = "pause;";


			int j=1;
			FileOutputStream fos = new FileOutputStream(batout+"\\run1.bat");
			OutputStreamWriter osw = new OutputStreamWriter(fos, "GBK");
		for(int i=0;i<listinputf.size();i++){
			String tmp = listinputf.get(i);
			javaml += "java -Xmx1024m  com.chenlly.gis.io.sdo.MIF2DB "+tmp+"\r\n";
			if(i>0&&i%ms==0){
				osw.write(batml1);
				osw.write(javaml);
				osw.write(jw);
				osw.flush();
				javaml = "";
				j++;
				fos = new FileOutputStream(batout+"\\run"+j+".bat");
				osw = new OutputStreamWriter(fos, "GBK");
				
			}
			//osw.write(tmp+"\r\n");
	
		}
		osw.write(batml1);
		osw.write(javaml);
		osw.write(jw);
		osw.flush();
		osw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	
	
	public static void install(){
		DBUtil dbutil = DBUtil.getInstance();
		try{
		conn = DBFactory.getConn(dbutil.getDriver(), dbutil.getUrl(), dbutil.getUser(), dbutil.getPwd());
		}catch(Exception e){
			e.printStackTrace();
		}
		//提取要入库的省份名字
		try{
		DBINPUTUtil	dbinpututil = DBINPUTUtil.getInstance();
		FileInputStream fis = new FileInputStream(new File(dbinpututil.getDbcity()));
		InputStreamReader isr = new InputStreamReader(fis,"GBK");
		BufferedReader br = new BufferedReader(isr);
		String line = "";
		while((line = br.readLine())!=null){
			if(!line.startsWith("#")){
				citylist.add(line);
			}
		}
		br.close();
		isr.close();
		fis.close();
		
		fis = new FileInputStream(new File(dbinpututil.getTcfils()));
		isr = new InputStreamReader(fis,"GBK");
		br = new BufferedReader(isr);
		line = "";
		while((line = br.readLine())!=null){
			if(!line.startsWith("#")){
				tclist.add(line);
			}
		}
		br.close();
		isr.close();
		fis.close();
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
		DBINPUTUtil	dbinpututil = DBINPUTUtil.getInstance();
		
		String addfilestr = dbinpututil.getAddFile();
		String[] addfiles1 = addfilestr.split(",");
		for(int i=0;i<addfiles1.length;i++){
			String temp1 = addfiles1[i];
			String[] tmp2 = temp1.split("->");
			addceng.put(tmp2[0], tmp2[1]);
		}
		
		input = dbinpututil.getInput();
	}
	
	
	public static  void getStartFiles2(String input){
		for(int i=0;i<tclist.size();i++){
			String tc = tclist.get(i);
			if(!tc.startsWith("#")){
				if(tc.indexOf("$city$")>=0){//遍历省份名
					for(int j=0;j<citylist.size();j++){
						String city = citylist.get(j);
						String tc2 = tc.replace("$city$", city);
						
						//如果不是H层和Hname
						File file2 = new File(input+"\\"+tc2);
						String fname = new File(input+"\\"+tc2).getName();
						boolean ish = false;
						for(Object o : addceng.keySet()){
						    if(fname.indexOf(o.toString())==0){
						    	String value1 = addceng.get(o);//被加入的层头，
						    	int head1 = o.toString().length();
						    	String foot  = fname.substring(head1);
						    	fileStartList2.put(value1+foot, file2);//追加文件 被加入的文件名 ：文件
						    	ish = true;
						    	break;
						    }
						} 
						if(!ish){
							fileStartList.add(file2);
						}
					
						//fileStartList.add(new File(input+"\\"+tc2));
					}
				}else{
					fileStartList.add(new File(input+"\\"+tc));
				}
			}
		}
		
		
	}
	
	public static String mifName2tableName(String mifName){
		String tablename = mifName.substring(0,mifName.lastIndexOf("."));
		//处理广州广东(见到guangdong1 guangdong2 guangzhou改成guangdong)
		if((tablename.indexOf("POI")==0)){
			tablename = tablename.replace("POI", "P");
		}
		if((tablename.indexOf("guangdong1")>0)){
			tablename = tablename.replace("guangdong1", "guangdong");
		}else if((tablename.indexOf("guangdong2")>0)){
			tablename = tablename.replace("guangdong2", "guangdong");
		}else if((tablename.indexOf("guangzhou")>0)){
			tablename = tablename.replace("guangzhou", "guangdong");
		}
		return tablename;
	}
}