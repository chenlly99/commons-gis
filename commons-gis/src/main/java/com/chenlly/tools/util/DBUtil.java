package com.chenlly.tools.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**  

 * Class DBConfig.java 

 * Description 

 * Company mapbar 

 * author Chenll E-mail: Chenll@mapbar.com

 * Version 1.0 

 * Date 2012-3-1 ����10:09:59

 */
public class DBUtil {
	
	private String driver; //��
	
	private String url;  //url
	
	private String user; //�û���
	
	private String pwd;  //����
	
	
	
    private static  DBUtil instance = null;  
    
    public static synchronized DBUtil getInstance() {  
        if(instance == null ){  
        	instance = new DBUtil();  
        }  
        return instance;  
    }  
    
	public DBUtil(){
		InputStream in = null;
		try {
			 in = getClass().getClassLoader().getResourceAsStream("config/DB.properties");
			 Properties p = new Properties();
			 p.load(in);
			 driver =  p.getProperty("driver").trim();
			 url =  p.getProperty("url").trim();
			 user =  p.getProperty("user").trim();
			 pwd =  p.getProperty("pwd").trim();
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
	
	public String getDriver() {
		return driver;
	}
	public String getUrl() {
		return url;
	}
	public String getUser() {
		return user;
	}
	public String getPwd() {
		return pwd;
	}



	
}
