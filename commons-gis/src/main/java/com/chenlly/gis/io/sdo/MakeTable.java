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
 *下午5:58:17
 */
public class MakeTable {
	public static Connection conn = null;
	public static List<File> fileStartList = new ArrayList<File>();//建库文件
	public static Map<String,File> fileStartList2  = new HashMap<String, File> ();
	public static Map<String,String> addceng = new HashMap<String,String>();
	public static  String logf = "E:\\workspace\\YCDCtotal\\log.txt";
	public static List<String> citylist = new ArrayList<String>();
	public static List<String> tclist = new ArrayList<String>();
	public static int treadnum = 4;
	static String input  ="";
	public static FileWriter fwlog ;
	public static String dbuser = "";
	

	public static void main(String[] args){
		MakeTable mifdb = new MakeTable();
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
				
				if((tablename.indexOf("guangdong1")>0)){
					tablename = tablename.replace("guangdong1", "guangdong");
					mifdb.maketable(path,tablename);
				}else if((tablename.indexOf("guangdong2")>0)){
					tablename = tablename.replace("guangdong2", "guangdong");
				}else if((tablename.indexOf("guangzhou")>0)){
					tablename = tablename.replace("guangzhou", "guangdong");
				}else {
					mifdb.maketable(path,tablename);
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
	 * 建表
	 */
	public void maketable(String path,String table) throws Exception{
		DBUtil dbutil = DBUtil.getInstance();
		DBINPUTUtil	dbinpututil = DBINPUTUtil.getInstance();
		//读取文件识别字段
		MifReader distReader = new MifReader(path);
		MifSchema mifschema = distReader.getSchema();
		int zds = mifschema.getAttributeCount();
        if(table.toLowerCase().indexOf("province")>=0){
        	System.out.println("--");
        }
		StringBuffer sqlb = new StringBuffer("create table "+table.toUpperCase()+"(\n");
		for(int i=0;i<zds;i++){
			String zdname = mifschema.getAttributeName(i).toUpperCase();
			String zdtype = mifschema.getAttributeType(zdname.toUpperCase()).toUpperCase();
			if(zdtype.startsWith("CHAR")){
				zdtype = zdtype.replace("CHAR", "VARCHAR2");
			}else if(zdtype.startsWith("INTEGER")){
				zdtype = "NUMBER";
			}else if(zdtype.startsWith("FLOAT")){
				zdtype = "NUMBER";
			}else if(zdtype.startsWith("DECIMAL")){
				zdtype = zdtype.replace("DECIMAL", "NUMBER");
			}
			sqlb.append(zdname+"  "+zdtype+",\n");

		}
		sqlb.append("GEOLOC"+"  MDSYS.SDO_GEOMETRY"+"\n");
		 
		sqlb.append(")\n");
		System.out.println(sqlb.toString());
		String sql = sqlb.toString();
		Statement stat = conn.createStatement();
		stat.executeUpdate(sql);
		stat.close();
		conn.commit();
		
		//判断user_sdo_geom_metadata里面是否有记录，没有加入一条然后才能建索引
/*		INSERT INTO user_sdo_geom_metadata VALUES (
			    'mylake',    //---表名
			    'shape',    //---字段名
			    MDSYS.SDO_DIM_ARRAY(   
			        MDSYS.SDO_DIM_ELEMENT('X', 0, 100, 0.05),    //---X维最小，最大值和容忍度。
			        MDSYS.SDO_DIM_ELEMENT('Y', 0, 100, 0.05)    //---Y维最小，最大值和容忍度
			    ),
			    NULL    //---坐标系，缺省为笛卡尔坐标系
			);*/
		
		String sqlc = "SELECT count(*) countjl FROM mdsys.SDO_GEOM_METADATA_TABLE S where sdo_owner='"+dbuser.toUpperCase()+"'AND S.SDO_TABLE_NAME='"+table.toUpperCase()+"'";
		Statement statc = conn.createStatement();
		ResultSet rs =  statc.executeQuery(sqlc);
		rs.next();
		int countjl = rs.getInt("countjl");
		rs.close();
		statc.close();  
		if(countjl<=0){
			stat = conn.createStatement();
			sqlb = new StringBuffer("");
			sqlb.append("insert into mdsys.SDO_GEOM_METADATA_TABLE(sdo_owner,sdo_table_name,sdo_column_name,SDO_DIMINFO,sdo_srid) values('"+dbutil.getUser().toUpperCase()+"','"+table.toUpperCase()+"','GEOLOC', MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', -180, 180, 0.0011119487),MDSYS.SDO_DIM_ELEMENT('Y', -90, 90, 0.0011119487)),'8307')");
			sql = sqlb.toString();
			System.out.println(sqlb.toString());
			stat.executeUpdate(sql);
			stat.close();
		}
		
		stat = conn.createStatement();
		//建索引单独处理
/*		sqlb = new StringBuffer("");
		sqlb.append("create index "+table+"_SX on "+table+"(GEOLOC) indextype is MDSYS.SPATIAL_INDEX");
		sql = sqlb.toString();
		System.out.println(sqlb.toString());
		stat.executeUpdate(sql);
		stat.close();*/
		conn.commit();

	}

}