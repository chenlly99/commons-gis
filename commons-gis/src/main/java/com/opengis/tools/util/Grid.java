package  com.opengis.tools.util;



/**  

 * Class Math.java 

 * Description 

 * Company mapbar 

 * author Chenll E-mail: Chenll@mapbar.com

 * Version 1.0 

 * Date 2012-12-11 下午02:39:25

 */
public class Grid {
	
	/**
	 * 通过经纬度计算四维mapid
	 */
	public static String getMapID(double longitude, double latitude) {
		double x = longitude;
		double y = latitude;
		String _x = String.format("%.2f", x);
		String _y = String.format("%.2f", y);
		Double dx = Double.parseDouble(_x);
		Double dy = Double.parseDouble(_y);
		int first = (int)(dy*1.5);
		int second = (int)(dx-60);
		int third = (int) (((dy*3600)/300)%8); //(dy*8*1.5%8)
		int fourth = (int) (((dx*3600) /450)%8); //(dx*8%8)
		return first+""+second+""+third+""+fourth;
	}
	
	public static String getMinMaxLonLat(String mapid){
		if(mapid==null || mapid.length()!=6) {
			System.out.println("Error:"+"mapid length not eq 6");
		}
		int m1 = Integer.parseInt(mapid.substring(0,1));
		int m2 = Integer.parseInt(mapid.substring(1,2));
		int m3 = Integer.parseInt(mapid.substring(2,3));
		int m4 = Integer.parseInt(mapid.substring(3,4));
		int m5 = Integer.parseInt(mapid.substring(4,5));
		int m6 = Integer.parseInt(mapid.substring(5,6));
		double xMin = Arith.div((m3*10+m4+60)*3600+m6*450,3600,6);
		double xMax = Arith.div((m3*10+m4+60)*3600+(m6+1)*450,3600,6);
		double yMin = Arith.div((m1*10+m2)*2400+m5*300,3600,6);
		double yMax = Arith.div((m1*10+m2)*2400+(m5+1)*300,3600,6);
		return xMin+","+yMin+";"+xMax+","+yMax;
	}
	

	
	/**
	 * 计算相邻8个网格的mapID
	 * @param mapID
	 * @return
	 */
	public static String adjoinMapID(String mapID) {
		int yInteger =  Integer.parseInt(mapID.substring(0,2)); //first longitude
		int xInteger = Integer.parseInt(mapID.substring(2,4)); //second latitude
		int yDecimals = Integer.parseInt(mapID.substring(4,5)); //third longitude
		int xDecimals = Integer.parseInt(mapID.substring(5,6));; //fourth latitude
		int yIntegerDown = yInteger;
		int yIntegerUp = yInteger;
		int xIntegerLeft = xInteger;
		int xIntegerRight = xInteger;
		int yDecimalsDown = yDecimals - 1;
		int yDecimalsUp = yDecimals + 1;
		int xDecimalsLeft = xDecimals - 1;
		int xDecimalsRight = xDecimals + 1;

		if (yDecimals == 0) {
			yDecimalsDown = 7;
			yIntegerDown = yInteger - 1;
		}
		if (yDecimals == 7) {
			yDecimalsUp = 0;
			yIntegerUp = yInteger + 1;
		}
		if (xDecimals == 0) {
			xDecimalsLeft = 7;
			xIntegerLeft = xInteger - 1;
		}
		if (xDecimals == 7) {
			xDecimalsRight = 0;
			xIntegerRight = xInteger + 1;
		}
		String mapIDLeftUp = yIntegerUp + "" + xIntegerLeft + "" + yDecimalsUp + "" + xDecimalsLeft; //左上
		String mapIDUp = yIntegerUp + "" + xInteger + "" + yDecimalsUp + "" + xDecimals; //上
		String mapIDRightUp = yIntegerUp + "" + xIntegerRight + "" + yDecimalsUp + "" + xDecimalsRight; //右上
		String mapIDRight = yInteger + "" + xIntegerRight + "" + yDecimals + "" + xDecimalsRight; //右
		String mapIDRightDown = yIntegerDown + "" + xIntegerRight + "" + yDecimalsDown + "" + xDecimalsRight; //右下
		String mapIDDown = yIntegerDown + "" + xInteger + "" + yDecimalsDown + "" + xDecimals; //下
		String mapIDLeftDown = yIntegerDown + "" + xIntegerLeft + "" + yDecimalsDown + "" + xDecimalsLeft; //左下 
		String mapIDLeft = yInteger + "" + xIntegerLeft + "" + yDecimals + "" + xDecimalsLeft; //左
		return mapIDLeftUp+","+mapIDUp+","+mapIDRightUp+","+mapIDRight+","+mapIDRightDown+","+mapIDDown+","+mapIDLeftDown+","+mapIDLeft;
	}
	
	
	public static void main(String[] args){
		//445625
		System.out.println(getMinMaxLonLat("545956"));
	}
}
