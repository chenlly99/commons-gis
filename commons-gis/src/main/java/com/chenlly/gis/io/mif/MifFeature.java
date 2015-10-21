package com.chenlly.gis.io.mif;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.chenlly.gis.geo.Feature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * @author chenlly(chenlly@126.com)
 * 
 */
public class MifFeature extends Feature implements Serializable {
	private static final long serialVersionUID = 1L;
	private String symbol = "";

	public MifFeature(MifSchema schema) {
		super(schema);
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * geometry to mif
	 * 
	 * @return
	 */
	public String toMifString() {
		StringBuffer sb = new StringBuffer();
		if (this.geometry != null && !this.geometry.isEmpty()) {
			String gType = this.geometry.getGeometryType();
			if (gType.equals("Point")) {
				sb.append("Point ").append(this.geometry.getCentroid().getX())
						.append(" ");
				sb.append(this.geometry.getCentroid().getY()).append("\r\n");
				if (symbol.isEmpty()) {
					sb.append("    Symbol (35,0,12)");
				} else {
					sb.append(symbol);
				}
			} else if (gType.equals("MultiPoint")) {
				Coordinate[] coords = this.geometry.getCoordinates();
				sb.append("Multipoint ").append(coords.length).append("\r\n");
				for (int i = 0; i < coords.length; i++) {
					sb.append(coords[i].x).append(" ").append(coords[i].y)
							.append("\r\n");
				}
				if (symbol.isEmpty()) {
					sb.append("    Symbol (35,0,12)");
				} else {
					sb.append(symbol);
				}
			} else if (gType.equals("LineString")) {
				Coordinate[] coords = this.geometry.getCoordinates();
				sb.append("Pline ").append(coords.length).append("\r\n");
				for (int i = 0; i < coords.length; i++) {
					sb.append(coords[i].x).append(" ").append(coords[i].y)
							.append("\r\n");
				}
				if (symbol.isEmpty()) {
					sb.append("    Pen (1,2,0)");
				} else {
					sb.append(symbol);
				}
			} else if (gType.equals("MultiLineString")) {
				int num = this.geometry.getNumGeometries();
				sb.append("Pline Multiple ").append(num).append("\r\n");
				for (int k = 0; k < num; k++) {
					Coordinate[] coords = this.geometry.getGeometryN(k)
							.getCoordinates();
					sb.append("  ").append(coords.length).append("\r\n");
					for (int j = 0; j < coords.length; j++) {
						sb.append(coords[j].x).append(" ").append(coords[j].y)
								.append("\r\n");
					}
				}
				if (symbol.isEmpty()) {
					sb.append("    Pen (1,2,0)");
				} else {
					sb.append(symbol);
				}
			} else if (gType.equals("Polygon")) {
				Polygon poly = (Polygon) this.geometry;
				sb.append("Region ").append(poly.getNumInteriorRing() + 1)
						.append("\r\n");
				// Shell
				Coordinate[] coords = poly.getExteriorRing().getCoordinates();
				sb.append("  ").append(coords.length).append("\r\n");
				for (int i = 0; i < coords.length; i++) {
					sb.append(coords[i].x).append(" ").append(coords[i].y)
							.append("\r\n");
				}
				// Holes
				for (int k = 0; k < poly.getNumInteriorRing(); k++) {
					coords = poly.getInteriorRingN(k).getCoordinates();
					sb.append("  ").append(coords.length).append("\r\n");
					for (int j = 0; j < coords.length; j++) {
						sb.append(coords[j].x).append(" ").append(coords[j].y)
								.append("\r\n");
					}
				}
				if (symbol.isEmpty()) {
					sb.append("    Pen (1,2,0)").append("\r\n");
					sb.append("    Brush (1,0,16777215)");
				} else {
					sb.append(symbol);
				}
			} else if (gType.equals("MultiPolygon")) {
				MultiPolygon mpoly = (MultiPolygon) this.geometry;
				int num = this.geometry.getNumGeometries();
				int count = 0;
				for (int i = 0; i < num; i++) {
					Polygon poly = (Polygon) mpoly.getGeometryN(i);
					count += poly.getNumInteriorRing() + 1;
				}

				sb.append("Region ").append(count).append("\r\n");
				for (int m = 0; m < num; m++) {
					Polygon poly = (Polygon) mpoly.getGeometryN(m);

					// Shell
					Coordinate[] coords = poly.getExteriorRing()
							.getCoordinates();
					sb.append("  ").append(coords.length).append("\r\n");
					for (int i = 0; i < coords.length; i++) {
						sb.append(coords[i].x).append(" ").append(coords[i].y)
								.append("\r\n");
					}
					// Holes
					for (int k = 0; k < poly.getNumInteriorRing(); k++) {
						coords = poly.getInteriorRingN(k).getCoordinates();
						sb.append("  ").append(coords.length).append("\r\n");
						for (int j = 0; j < coords.length; j++) {
							sb.append(coords[j].x).append(" ")
									.append(coords[j].y).append("\r\n");
						}
					}
				}
				if (symbol.isEmpty()) {
					sb.append("    Pen (1,2,0)").append("\r\n");
					sb.append("    Brush (1,0,16777215)");
				} else {
					sb.append(symbol);
				}
			} else if (gType.equals("GeometryCollection")) {
				GeometryCollection gc = (GeometryCollection) this.geometry;
				Map<String,Integer> map = getTyepNum(gc);
				if(map.containsKey("Polygon") || map.containsKey("MultiPolygon")){
					int count = 0; 
					for (int h = 0; h < gc.getNumGeometries(); h++) { 
			            Geometry expected = (Geometry) gc.getGeometryN(h); 
			            if(expected.getGeometryType().equals("Polygon")||expected.getGeometryType().equals("MultiPolygon")){
							int num = expected.getNumGeometries();
							for (int i = 0; i < num; i++) {
								Polygon poly = (Polygon) expected.getGeometryN(i);
								count += poly.getNumInteriorRing() + 1;
							}
			            }
					}
					sb.append("Region ").append(count).append("\r\n");
					for (int h = 0; h < gc.getNumGeometries(); h++) { 
			            Geometry expected = (Geometry) gc.getGeometryN(h); 
			            if(expected.getGeometryType().equals("Polygon")||expected.getGeometryType().equals("MultiPolygon")){
			            	for(int l = 0; l<expected.getNumGeometries();l++){
			            		Polygon poly = (Polygon) expected.getGeometryN(l);
				            	// Shell
								Coordinate[] coords = poly.getExteriorRing().getCoordinates();
								sb.append("  ").append(coords.length).append("\r\n");
								for (int i = 0; i < coords.length; i++) {
									sb.append(coords[i].x).append(" ").append(coords[i].y)
											.append("\r\n");
								}
								// Holes
								for (int k = 0; k < poly.getNumInteriorRing(); k++) {
									coords = poly.getInteriorRingN(k).getCoordinates();
									sb.append("  ").append(coords.length).append("\r\n");
									for (int j = 0; j < coords.length; j++) {
										sb.append(coords[j].x).append(" ")
												.append(coords[j].y).append("\r\n");
									}
								}
			            	}
			            }
					}
					if (symbol.isEmpty()) {
						 sb.append("    Pen (1,2,0)").append("\r\n");
						 sb.append("    Brush (1,0,16777215)");
					} else {
						sb.append(symbol);
					}
					// return sb.toString();
				} else if(map.containsKey("LineString") || map.containsKey("MultiLineString")){
					int count = 0; 
					int geoNum = 0;
					for (int h = 0; h < gc.getNumGeometries(); h++) { 
			            Geometry expected = (Geometry) gc.getGeometryN(h); 
			            if(expected.getGeometryType().equals("LineString")||expected.getGeometryType().equals("MultiLineString")){
			            	int num = expected.getNumGeometries();
			            	geoNum +=num;
			            	for(int i = 0; i<num;i++){
			            		Coordinate[] coords = expected.getGeometryN(i).getCoordinates();
			            		count+=coords.length;
			            	}
			            }
					}					
					
					if(map.get("LineString")==1 && !map.containsKey("MultiLineString")){
						sb.append("Pline ").append(count).append("\r\n");
					} else {
						sb.append("Pline Multiple ").append(geoNum).append("\r\n");
					}
					for (int h = 0; h < gc.getNumGeometries(); h++) { 
			            Geometry expected = (Geometry) gc.getGeometryN(h); 
			            if(expected.getGeometryType().equals("LineString")||expected.getGeometryType().equals("MultiLineString")){
			            	if(map.get("LineString")==1 && !map.containsKey("MultiLineString")){
								Coordinate[] coords = expected.getCoordinates();
								for (int i = 0; i < coords.length; i++) {
									sb.append(coords[i].x).append(" ").append(coords[i].y).append("\r\n");
								}
							} else {
								for (int k = 0; k < expected.getNumGeometries(); k++) {
									Coordinate[] coords = expected.getGeometryN(k).getCoordinates();
									sb.append("  ").append(coords.length).append("\r\n");
									for (int j = 0; j < coords.length; j++) {
										sb.append(coords[j].x).append(" ").append(coords[j].y)
												.append("\r\n");
									}
								}
							}
			            }
					}
					if (symbol.isEmpty()) {
						sb.append("    Pen (1,2,0)");
					} else {
						sb.append(symbol);
					}
					// return sb.toString();
				} else  if(map.containsKey("Point") || map.containsKey("MultiPoint")){
					int count = 0; 
					for (int h = 0; h < gc.getNumGeometries(); h++) { 
			            Geometry expected = (Geometry) gc.getGeometryN(h); 
			            if(expected.getGeometryType().equals("Point")||expected.getGeometryType().equals("Multipoint")){
			            	count+=expected.getCoordinates().length;
			            }
					}
					sb.append("Multipoint ").append(count).append("\r\n");
					for (int h = 0; h < gc.getNumGeometries(); h++) { 
			            Geometry expected = (Geometry) gc.getGeometryN(h); 
			            if(expected.getGeometryType().equals("Point")||expected.getGeometryType().equals("Multipoint")){
			            	Coordinate[] coords = expected.getCoordinates();
			            	for(int k = 0;k<coords.length;k++){
			            		sb.append(coords[k].x).append(" ").append(coords[k].y).append("\r\n");
			            	}
			            }
					}
					if (symbol.isEmpty()) {
						sb.append("    Symbol (35,0,12)");
					} else {
						sb.append(symbol);
					}
					// return sb.toString();
				} else { // TODO 
					sb.append("NONE");
					System.out.println("----- not convert geometry to mif: "
							+ gType + "  " + this.geometry);
				}
				// return sb.toString();
			} else { // TODO
				sb.append("NONE");
				System.out.println("----- not convert geometry to mif: "
						+ gType + "  " + this.geometry);
			}			
		} else {
			sb.append("NONE");
		}
		return sb.toString();
	}
	
	private Map<String,Integer> getTyepNum(GeometryCollection gcpoly){
		Map<String,Integer> map = new HashMap<String,Integer>();
		for (int h = 0; h < gcpoly.getNumGeometries(); h++) { 
			 Geometry expected = (Geometry) gcpoly.getGeometryN(h); 
			 String type = expected.getGeometryType();
			 if(!map.containsKey(type)){
				 map.put(type, 1);
			 } else {
				 map.put(type, map.get(type)+1);
			 }
		}
		return map;
	}
	/**
	 * attributes to mid
	 * 
	 * @return
	 */
	public String toMidString() {
		StringBuffer sb = new StringBuffer();
		MifSchema mschema = (MifSchema) this.getSchema();
		for (int i = 0; i < mschema.getAttributeCount(); i++) {
			String type = mschema.getAttributeType(i).toLowerCase();
			if (type.startsWith("integer") || type.startsWith("smallint")) {
				sb.append(this.getString(i)).append(",");
			} else if (type.startsWith("float") || type.startsWith("decimal")) {
				sb.append(this.getString(i)).append(",");
			} else if (type.startsWith("date") || type.startsWith("time")
					|| type.startsWith("datetime")) {
				sb.append(this.getString(i)).append(",");
			} else {
				sb.append("\"");
				sb.append(this.getString(i)).append("\",");
			}
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		} else {
			return sb.toString();
		}
	}

}
