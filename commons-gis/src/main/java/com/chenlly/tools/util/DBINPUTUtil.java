package  com.chenlly.tools.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author ligang  ligang@mapbar.com
 *下午4:06:19
 *用于mif入库
 */
public class DBINPUTUtil {
	private String tcfils;  //图层配置文件
	private String dbcity;  //省份配置文件
	private String addFile;  //追加图层
	private String input;  //图层根目录
	private String noindex;  //不用建索引的图层
	private int treadnum = 7;

	
	  private static  DBINPUTUtil instance = null;  
	    
	    public static synchronized DBINPUTUtil getInstance() {
	        if(instance == null ){  
	        	instance = new DBINPUTUtil();  
	        }  
	        return instance;  
	    }
	    
	    
	    public DBINPUTUtil(){
			InputStream in = null;
			try {
				 in = getClass().getClassLoader().getResourceAsStream("config/DBINPUTConfig.properties");
				 Properties p = new Properties();
				 p.load(in);
				 tcfils =  p.getProperty("tcfils").trim();
				 dbcity =  p.getProperty("dbcity").trim();
				 addFile =  p.getProperty("addFile").trim();
				 input =  p.getProperty("input").trim();
				 noindex =  p.getProperty("noindex").trim();
				 treadnum =  Integer.parseInt(p.getProperty("treadnum").trim()) ;
				 
				} catch (IOException e) {
					throw new RuntimeException("load db config error", e);
				} finally {
					if(in!=null){
						try {
							in.close();
						} catch (IOException e) {
							throw new RuntimeException("close inputStream error", e);
						}
					}
				}
		}


		public String getTcfils() {
			return tcfils;
		}


		public void setTcfils(String tcfils) {
			this.tcfils = tcfils;
		}


		public String getDbcity() {
			return dbcity;
		}


		public void setDbcity(String dbcity) {
			this.dbcity = dbcity;
		}


		public String getAddFile() {
			return addFile;
		}


		public void setAddFile(String addFile) {
			this.addFile = addFile;
		}


		public String getInput() {
			return input;
		}


		public void setInput(String input) {
			this.input = input;
		}


		public String getNoindex() {
			return noindex;
		}


		public void setNoindex(String noindex) {
			this.noindex = noindex;
		}


		public int getTreadnum() {
			return treadnum;
		}


		public void setTreadnum(int treadnum) {
			this.treadnum = treadnum;
		}


		public static void setInstance(DBINPUTUtil instance) {
			DBINPUTUtil.instance = instance;
		}
	    
	    
	    
	    
}
