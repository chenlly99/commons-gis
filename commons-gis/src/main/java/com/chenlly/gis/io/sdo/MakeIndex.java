package com.chenlly.gis.io.sdo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chenlly.gis.io.mif.MifReader;
import com.chenlly.gis.io.mif.MifSchema;
import com.chenlly.tools.util.DBINPUTUtil;
import com.chenlly.tools.util.DBUtil;

/**
 * @author ligang  ligang@mapbar.com
 *上午10:29:09
 */
public class MakeIndex {
	public static Connection conn = null;
	public static List<File> fileStartList = new ArrayList<File>();//建库文件
	public static Map<String,File> fileStartList2  = new HashMap<String, File> ();
	public static Map<String,String> addceng = new HashMap<String,String>();
	public static  String logf = "E:\\workspace\\YCDCtotal\\log.txt";
	public static List<String> citylist = new ArrayList<String>();
	public static List<String> tclist = new ArrayList<String>();
	public static List<String> noindexlist = new ArrayList<String>();
	public static int treadnum = 4;
	static String input  ="";
	public static FileWriter fwlog ;
	//public static String dbuser = "";
	
	public static void main(String[] args){
		MakeIndex mifdb = new MakeIndex();
		install();
		getStartFiles2(input);
		

			//建表
			for(int i=0;i<fileStartList.size();i++){
				try{
				String path = fileStartList.get(i).getPath();
				String name = fileStartList.get(i).getName();
				String tablename = name.substring(0,name.lastIndexOf("."));
				if((tablename.indexOf("POI")==0)){
					tablename = tablename.replace("POI", "P");
				}
				//判断此表是否需要建立索引，如果需要建立就建索引
				boolean isindex = true;
				for(int x=0;x<noindexlist.size();x++){
					if(tablename.indexOf(noindexlist.get(x))>=0){
						isindex = false;
					}
				}
				if(isindex){

				if((tablename.indexOf("guangdong1")>0)){
					tablename = tablename.replace("guangdong1", "guangdong");
					mifdb.makeIndex(path,tablename);
				}else if((tablename.indexOf("guangdong2")>0)){
					tablename = tablename.replace("guangdong2", "guangdong");
				}else if((tablename.indexOf("guangzhou")>0)){
					tablename = tablename.replace("guangzhou", "guangdong");
				}else {
					mifdb.makeIndex(path,tablename);
				}
				}else{
					System.out.println(tablename+"不需要索引");
				}
				}catch(Exception e){
					e.printStackTrace();
				}	
			}
				


		
	}
	
	
	
	public static void install(){
		DBUtil dbutil = DBUtil.getInstance();
		DBINPUTUtil	dbinpututil = DBINPUTUtil.getInstance();
		try{
		conn = DBFactory.getConn(dbutil.getDriver(), dbutil.getUrl(), dbutil.getUser(), dbutil.getPwd());
		}catch(Exception e){
			e.printStackTrace();
		}
		//提取要入库的省份名字
		try{
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
	
	
	
	/**
	 * 建索引
	 */
	public void makeIndex(String path,String table) throws Exception{
		DBUtil dbutil = DBUtil.getInstance();
		//建索引单独处理
		//查询索引，不存在就新建索引
		String sql = "select count(*) indexcount  from sys.user_indexes where table_owner='"+dbutil.getUser().toUpperCase()+"' and table_name='"+table.toUpperCase()+"' and index_type='DOMAIN'";
		Statement statc = conn.createStatement();
		ResultSet rs =  statc.executeQuery(sql);
		rs.next();
		int indexcount = rs.getInt("indexcount");
		rs.close();
		statc.close();
		if(indexcount<=0){
			sql = "create index "+table.toUpperCase()+"_SX on "+table.toUpperCase()+"(GEOLOC) indextype is MDSYS.SPATIAL_INDEX";
			System.out.println(sql);
			Statement stat = conn.createStatement();
			stat.executeUpdate(sql);
			stat.close();
			conn.commit();
		}else{
			sql = "drop index  "+table.toUpperCase()+"_SX";
			System.out.println(sql);
			Statement stat = conn.createStatement();
			stat.executeUpdate(sql);
			stat.close();
			conn.commit();
			sql = "create index "+table.toUpperCase()+"_SX on "+table.toUpperCase()+"(GEOLOC) indextype is MDSYS.SPATIAL_INDEX";
			System.out.println(sql);
			stat = conn.createStatement();
			stat.executeUpdate(sql);
			stat.close();
			conn.commit();
		}
		

	}
}
