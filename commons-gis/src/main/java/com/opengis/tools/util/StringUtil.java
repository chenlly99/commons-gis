package  com.opengis.tools.util;
/**  

 * Class StringUtil.java 

 * Description 

 * Company mapbar 

 * author Chenll E-mail: Chenll@mapbar.com

 * Version 1.0 

 * Date 2012-3-8 上午10:30:58

 */
public class StringUtil {
	
	public static String subStr(String str){
		if(str.length()<2){
			System.out.println("adminCode error!"+str);
		}
		return str.substring(0,2);
	}
}
