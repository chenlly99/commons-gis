package com.opengis.gis.io.mif;

import java.util.ArrayList;
import java.util.List;

import com.opengis.gis.io.FileIterator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * 
 * @author chenlly(chenlly@126.com)
 * 
 */
class MifFeatureBuilder {
	private static GeometryFactory gf = new GeometryFactory();

	/**
	 * build Feature that geometry is Point,out geometry is Point
	 * 
	 * @param item
	 *            MifFeature
	 * @param mifLine
	 * @param mif
	 *            FileIterator
	 * @param mid
	 *            FileIterator
	 */
	static void pointFeature(MifFeature item, String mifLine, FileIterator mif,
			FileIterator mid) {
		String[] tokens = mifLine.substring(5).trim().split(" ");
		if (tokens.length < 2)
			return;
		Coordinate coord = new Coordinate(Double.valueOf(tokens[0].trim()),
				Double.valueOf(tokens[1].trim()));
		item.setGeometry(gf.createPoint(coord));

		readAttributes(item, mid);
		readSymbol(item, mif);
	}

	/**
	 * build Feature that geometry is Multipoint,out geometry is MultiPoint
	 * 
	 * @param item
	 *            MifFeature
	 * @param mifLine
	 * @param mif
	 *            FileIterator
	 * @param mid
	 *            FileIterator
	 */
	static void mpointFeature(MifFeature item, String mifLine,
			FileIterator mif, FileIterator mid) {
		int len = Integer.parseInt(mifLine.substring(10).trim());
		Coordinate[] coords = new Coordinate[len];
		for (int i = 0; i < len; i++) {
			String[] tokens = mif.next().trim().split(" ");
			coords[i] = new Coordinate(Double.valueOf(tokens[0].trim()),
					Double.valueOf(tokens[1].trim()));
		}
		item.setGeometry(gf.createMultiPoint(coords));

		readAttributes(item, mid);
		readSymbol(item, mif);
	}

	/**
	 * build Feature that geometry is Line,out geometry is LineString
	 * 
	 * @param item
	 *            MifFeature
	 * @param mifLine
	 * @param mif
	 *            FileIterator
	 * @param mid
	 *            FileIterator
	 */
	static void lineFeature(MifFeature item, String mifLine, FileIterator mif,
			FileIterator mid) {
		String[] tokens = mifLine.substring(4).trim().split(" ");
		Coordinate[] coords = new Coordinate[2];
		coords[0] = new Coordinate(Double.valueOf(tokens[0].trim()),
				Double.valueOf(tokens[1].trim()));
		coords[1] = new Coordinate(Double.valueOf(tokens[2].trim()),
				Double.valueOf(tokens[3].trim()));
		item.setGeometry(gf.createLineString(coords));

		readAttributes(item, mid);
		readSymbol(item, mif);
	}

	/**
	 * build Feature that geometry is Pline,out geometry is LineString
	 * 
	 * @param item
	 *            MifFeature
	 * @param mifLine
	 * @param mif
	 *            FileIterator
	 * @param mid
	 *            FileIterator
	 */
	static void plineFeature(MifFeature item, String mifLine, FileIterator mif,
			FileIterator mid) {
		int len = Integer.parseInt(mifLine.substring(5).trim());
		Coordinate[] coords = new Coordinate[len];
		for (int i = 0; i < len; i++) {
			String[] tokens = mif.next().trim().split(" ");
			coords[i] = new Coordinate(Double.valueOf(tokens[0].trim()),
					Double.valueOf(tokens[1].trim()));
		}
		item.setGeometry(gf.createLineString(coords));
		readAttributes(item, mid);
		readSymbol(item, mif);
	}

	/**
	 * build Feature that geometry is Pline Multiple,out geometry is
	 * MultiLineString
	 * 
	 * @param item
	 *            MifFeature
	 * @param mifLine
	 * @param mif
	 *            FileIterator
	 * @param mid
	 *            FileIterator
	 */
	static void mplineFeature(MifFeature item, String mifLine,
			FileIterator mif, FileIterator mid) {
		int num = Integer.parseInt(mifLine.substring(14).trim());
		LineString[] lines = new LineString[num];
		for (int i = 0; i < num; i++) {
			int len = Integer.parseInt(mif.next().trim());
			Coordinate[] coords = new Coordinate[len];
			for (int j = 0; j < len; j++) {
				String[] tokens = mif.next().trim().split(" ");
				coords[j] = new Coordinate(Double.valueOf(tokens[0].trim()),
						Double.valueOf(tokens[1].trim()));
			}
			lines[i] = gf.createLineString(coords);
		}
		item.setGeometry(gf.createMultiLineString(lines));

		readAttributes(item, mid);
		readSymbol(item, mif);
	}

	/**
	 * build Feature that geometry is Region,out geometry is Polygon
	 * 
	 * @param item
	 *            MifFeature
	 * @param mifLine
	 * @param mif
	 *            FileIterator
	 * @param mid
	 *            FileIterator
	 */
	static void regionFeature(MifFeature item, String mifLine,
			FileIterator mif, FileIterator mid) {
		int num = Integer.parseInt(mifLine.substring(6).trim());
		LinearRing shell = null;
		LinearRing[] holes = new LinearRing[num - 1];
		for (int i = 0; i < num; i++) {
			int len = Integer.parseInt(mif.next().trim());
			Coordinate[] coords = new Coordinate[len];
			for (int j = 0; j < len; j++) {
				String[] tokens = mif.next().trim().split(" ");
				coords[j] = new Coordinate(Double.valueOf(tokens[0].trim()),
						Double.valueOf(tokens[1].trim()));
			}
			if (i == 0) {
				shell = gf.createLinearRing(coords);
			} else {
				holes[i - 1] = gf.createLinearRing(coords);
			}
		}
		item.setGeometry(gf.createPolygon(shell, holes));
		readAttributes(item, mid);
		readSymbol(item, mif);
	}

	/**
	 * build Feature that geometry is none,out geometry is null
	 * 
	 * @param item
	 *            MifFeature
	 * @param mifLine
	 * @param mif
	 *            FileIterator
	 * @param mid
	 *            FileIterator
	 */
	static void noneFeature(MifFeature item, String mifLine, FileIterator mif,
			FileIterator mid) {
		readAttributes(item, mid);
	}

	private static void readSymbol(MifFeature item, FileIterator mif) {
		StringBuffer sb = new StringBuffer();
		while (mif.hasNext()) {
			String s = mif.next();
			if (s.lastIndexOf(")") > 0) {
				sb.append(s).append("\r\n");
			} else {
				mif.pushback(s);
				break;
			}
		}
		if (sb.length() > 0) {
			item.setSymbol(sb.substring(0, sb.length() - 2));
		}
	}

	private static void readAttributes(MifFeature item, FileIterator mid) {
		String line = mid.next();
		MifSchema mschema = (MifSchema) item.getSchema();
		String[] fields = null;
		/*if(line.indexOf("\"")>=0){
			fields = changeLine(line);
		}else{
			fields = line.split(mschema.getDelimiter());
		}*/
		//String[] fields = line.split(mschema.getDelimiter());

		//fields = getfields(line,mschema);
		fields = line.split(",");
 
		for (int i = 0; i < mschema.getAttributeCount(); i++) {
			if (i >= fields.length) {
				continue;
			}
			String type = mschema.getAttributeType(i).toLowerCase();
			if (type.startsWith("integer") || type.startsWith("smallint")) {
				item.setAttribute(i, Integer.valueOf(fields[i].trim()));
			} else if (type.startsWith("float") || type.startsWith("decimal")) {
				item.setAttribute(i, Double.valueOf(fields[i].trim()));
			} else if (type.startsWith("date") || type.startsWith("time")
					|| type.startsWith("datetime")) {
				item.setAttribute(i, Long.valueOf(fields[i].trim()));
			} else {
				String s = fields[i].trim();
				if(s.length()>1){
					if(s.indexOf("\"")>=0){
					item.setAttribute(i, s.substring(1, s.length() - 1));
					}else{
						item.setAttribute(i, s);	
					}
				} else {
					item.setAttribute(i, s);
				}
			}
		}

	}
	
	 public static String[] changeLine(String str) {
		    String[] str1 = str.split(",");
		    List<String> list = new ArrayList<String>();
		    for (int i = 0; i < str1.length; i++) {
		      if ((str1[i].startsWith("\"")) && (!str1[i].endsWith("\""))) {
		        str1[(i + 1)] = (str1[i] + "," + str1[(i + 1)]);
		      }
		      else {
		        list.add(str1[i]);
		      }
		    }
            String[] str2 = new String[list.size()];
            for(int i=0;i<list.size();i++){
            	str2[i] = list.get(i);
            }
		    return str2;
	}
	 
	 public static String[] getfields(String line , MifSchema mschema){
		 int zdzs = mschema.getAttributeCount();
		 String tmpline = line;
		 String[] zds = new String[zdzs];
		 for(int i=0;i<zds.length-1;i++){
			String zdtype =  mschema.getAttributeType(i);
			if(zdtype.toUpperCase().indexOf("CHAR")>=0){
				//带引号截取
				int firstyinhao = tmpline.indexOf("\"");
				tmpline = tmpline.substring(firstyinhao+1);
				int secondyinhao = tmpline.indexOf("\",");
				String zdvalue = tmpline.substring(0,secondyinhao);
				tmpline = tmpline.substring(secondyinhao+1);
				tmpline	=  tmpline.substring(1);
				zds[i] = zdvalue;
			}else{
				//不带引号截取
				int firstdouhao = tmpline.indexOf(",");
				String zdvalue = tmpline.substring(0,firstdouhao);
				tmpline = tmpline.substring(firstdouhao+1);
				zds[i] = zdvalue;
			}
		 }
		//最后一个字段
		 String zdtype =  mschema.getAttributeType(zdzs-1);
		 if(tmpline.endsWith(",")){
			 tmpline = tmpline.substring(0,tmpline.length()-1);
		 }
		 if(zdtype.toUpperCase().indexOf("CHAR")>=0){
			 int firstyinhao = tmpline.indexOf("\"");
			 tmpline = tmpline.substring(firstyinhao+1);
			 int secondyinhao = tmpline.indexOf("\"");
			 String zdvalue = tmpline.substring(0,secondyinhao);
			 tmpline = tmpline.substring(secondyinhao+1);
			 zds[zdzs-1] = zdvalue;
		 }else{
			 String zdvalue = tmpline;
			 zds[zdzs-1] = zdvalue;
		 }
		// System.out.println(line);
		 return zds;
	 }


}
