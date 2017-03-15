package  com.opengis.tools.util;

import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class Province {

	private String name;
	private String py;
	private String code;
	private Geometry geo;
	public  Map<String, Province> map = new HashMap<String, Province>();

	private  static Province instance = null;

	public static synchronized Province getInstance() {
		if (instance == null) {
			instance = new Province();
		}
		return instance;
	}

	public Province() {
		
	}
	
	/*
	
	*//**
	 * 数据库读取省界数据
	 * @param con
	 * @param provinSQL
	 *//*
	public void readProvinces(Connection con,String provinSQL) {
		SdoReader sr = new SdoReader(con, provinSQL);
		while (sr.hasNext()) {
			MifFeature item = sr.next();
			Province p = new Province();
			p.setName(item.getString(Constant.ADMIN_NAME));
			p.setCode(StringUtil.subStr(item.getString(Constant.ADMIN_CODE)));// 截取前两位
			p.setPy(item.getString(Constant.ADMIN_PY));
			p.setGeo(item.getGeometry());
			map.put(p.getCode(), p);
		}

	}
	
	*//**
	 * 文件读取省界数据
	 * @param file
	 *//*
	public void readProvinces(String file) {
		MifReader mr = new MifReader(file);
		while (mr.hasNext()) {
			MifFeature item = mr.next();
			Province p = new Province();
			p.setName(item.getString(Constant.ADMIN_NAME));
			p.setPy(item.getString(Constant.ADMIN_PY));
			p.setCode(StringUtil.subStr(item.getString(Constant.ADMIN_CODE)));// 截取前两位
			p.setGeo(item.getGeometry());
			map.put(p.getCode(), p);
		}
	}

	public String getNameByCode(String code) {
		String adminCode = StringUtil.subStr(code);
		if (map.containsKey(adminCode)) {
			return map.get(adminCode).getName();
		}
		return null;
	}
	
	public String getPyByCode(String code) {
		String adminCode = StringUtil.subStr(code);
		if (map.containsKey(adminCode)) {
			return map.get(adminCode).getPy();
		}
		return null;
	}

	public Geometry getGeometryByCode(String code) {
		String adminCode = StringUtil.subStr(code);
		if (map.containsKey(adminCode)) {
			return map.get(adminCode).getGeo();
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPy() {
		return py;
	}

	public void setPy(String py) {
		this.py = py;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Geometry getGeo() {
		return geo;
	}

	public void setGeo(Geometry geo) {
		this.geo = geo;
	}
	
	public Map<String, Province> getMap() {
		return map;
	}*/

}
