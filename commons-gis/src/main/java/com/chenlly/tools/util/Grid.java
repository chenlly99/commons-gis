package  com.chenlly.tools.util;

import java.math.BigDecimal;
import java.math.BigInteger;


/**  

 * Class Math.java 

 * Description 

 * Company mapbar 

 * author Chenll E-mail: Chenll@mapbar.com

 * Version 1.0 

 * Date 2012-12-11 下午02:39:25

 */
public class Grid {
	
	private static   double W1 ;
	
	private static   double H1 ;
	
	private static   double W2 ;
	
	private static   double H2 ;
	
	public static  void init(){
		W1 = divide(60,60,2,BigDecimal.ROUND_HALF_UP);
		H1 = divide(40,60,2,BigDecimal.ROUND_HALF_UP);
		W2 = divide(7.5,60,2,BigDecimal.ROUND_HALF_UP);
		H2 = divide(5,60,2,BigDecimal.ROUND_HALF_UP);
	}
	
	public static  double divide(double mem1,double mem2,int num,int round){
		BigDecimal   b   =   new   BigDecimal(mem2);  
		BigDecimal   bb   =   new   BigDecimal(mem1).divide(b,num,round);
		return bb.doubleValue();
	}
	
	public static  double multiply(double mem1,double mem2,int num,int round){
		BigDecimal   b   =   new   BigDecimal(mem1);  
		BigDecimal   bb   =   new   BigDecimal(mem2).multiply(b).setScale(num,round);
		return bb.doubleValue();
	}
	
	public static String getGridByCoor(double lon,double lat){
		init();
		double x0 = lon-60;
		double yy = divide(lat,H1,0,BigDecimal.ROUND_DOWN);
		double xx = divide(x0,W1,0,BigDecimal.ROUND_DOWN);
		
		double y1 = multiply(yy,H1,2,BigDecimal.ROUND_UP);
		double y2 = lat-y1;
		double y = divide(y2,H2,0,BigDecimal.ROUND_DOWN);
		
		double x1 = multiply(xx,W1,2,BigDecimal.ROUND_UP);;
		double x2 = x0-x1;
		double x = divide(x2,W2,0,BigDecimal.ROUND_DOWN);
		System.out.println(yy+""+xx+""+y+""+x);
		return yy+""+xx+""+y+""+x;
	}
	
	public static void main(String[] args){
		//445625
		getGridByCoor(116.63434979636529,29.55310608377573);
	}
}
