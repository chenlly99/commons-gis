package  com.opengis.tools.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * 计算球面距离
 * @author chenll
 *
 */
public class Distance {
	/**
	 * 计算两个经纬度之间的长度(是长度，不是距离)
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	public static double getDistance(double lat1, double lng1, double lat2,double lng2) {
		if(lat1<=0D || lng1<=0D || lat2<=0D || lng2<=0D){
			throw new RuntimeException("[经纬度值必须大于0]");
		}
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000.0;
		return s;
	}
		
	/**
	 * 计算两个经纬度之间的长度(是长度，不是距离)
	 * @param lat1
	 * @param lng1
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	public static double getDistance(String point1,String point2){
		if(point1==null || "".equals(point1)||point2==null || "".equals(point2)){
			throw new RuntimeException("[经纬度值不能为空]");
		}
		String[] pointArr1 = point1.split(" ");
		String[] pointArr2 = point2.split(" ");
		double lng1 = Double.parseDouble(pointArr1[0]);  //x1
		double lng2 = Double.parseDouble(pointArr2[0]);  //x2
		double lat1 = Double.parseDouble(pointArr1[1]);   //y1
		double lat2 = Double.parseDouble(pointArr2[1]);   //y2
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000.0;
		return s;
	}
	
	//计算一个路段的长度
	public static double getLinkDistance(Coordinate[] coords) {
		double sum = 0;
		for (int i = 0; i < coords.length - 1; i++) {
			Coordinate first = coords[i];
			Coordinate end = coords[i+1];
			sum = sum + getDistance(first.y,first.x,end.y,end.x);
		}
		return sum;
	}
	
	private static double EARTH_RADIUS = 6378137.0; // 单位m
	
	private static double rad(double d) {
		return d * Math.PI / 180.0;
	}
	
	public static  double WGS84LongAxle = 6378137; //WGS84地球椭球体的长半轴

    public static  double WGS84ShortAxle = 6356752.3142;

    //WGS84椭球体经度椭圆的周长
    public static  double CircumOfLongi = 2 * Math.PI * WGS84ShortAxle + 4 * (WGS84LongAxle - WGS84ShortAxle);

    /// <summary>
    /// 获取指定参考点范围的大地距离对应的坐标差
    /// 本函数完全按照WGS84定义地球椭球体计算
    /// </summary>
    /// <param name="SphereDis">大地距离</param>
    /// <param name="ReferencePt">参考点</param>
    /// <param name="IsLongtitude">true--经度方向, false--纬度方向</param>
    /// <returns>经纬坐标差，单位“度*100000”</returns>
    public static double GetCoordinateWideFromDistance(double SphereDis, Point ReferencePt, boolean IsLongtitude)
    {
        double RefCorner = ReferencePt.getY() / 100000; //参考点的纬度角

        //该角度处横割圆的半径
        double RefRingR = Math.sqrt(Math.pow(WGS84LongAxle * Math.cos(RefCorner), 2) + Math.pow(WGS84ShortAxle * Math.sin(RefCorner), 2));

        double CoordWide = 0.0;

        if (IsLongtitude) //经度方向
        {
            double LongitudeDis = 2 * Math.PI * RefRingR / 360; //每度的经度方向长度=该点所在圆周长/360
            CoordWide = SphereDis / LongitudeDis * 100000;
        }
        else//纬度方向
        {
            double LatitudeDis = CircumOfLongi / 360; //近似参考点处单位纬度的长度=经度方向椭圆周长/360
            CoordWide = SphereDis / LatitudeDis * 100000;
        }

        return CoordWide;
    }

    
    public static void main(String[] args) {
    	Coordinate coord = new Coordinate();
    	coord.x=115.73334;
    	coord.y = 39.57573996;
    	GeometryFactory gf = new GeometryFactory();
    	Geometry point = gf.createPoint(coord);
    	//0.0001796
    	System.out.println(GetCoordinateWideFromDistance(40,(Point)point,true));
    	/*//0.0001798
    	System.out.println(GetCoordinateWideFromDistance(20,(Point)point,false));*/
    	
    	//测试
    	Coordinate coord2 = new Coordinate();
    	coord2.x = coord.x *100000 + 35.93261138361853;
    	coord2.y = coord.y *100000 + 35.93261138361853;
    	System.out.println(getDistance(coord.y*100000, coord.x*100000, coord2.y, coord2.x)/100000);
    	
    }
}
