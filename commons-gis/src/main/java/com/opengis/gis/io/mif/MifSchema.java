package com.opengis.gis.io.mif;

import java.io.Serializable;

import com.opengis.gis.geo.Schema;



/**
 * 
 * @author chenlly(chenlly@126.com)
 * 
 */
public class MifSchema extends Schema implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String meta = "";
	private String delimiter = ",";
	private String data = "Data";

	public MifSchema() {
		StringBuffer buf = new StringBuffer();
		buf.append("Version 300").append("\r\n");
		buf.append("Charset \"WindowsSimpChinese\"").append("\r\n");
		buf.append("Delimiter \",\"").append("\r\n");
		buf.append("CoordSys Earth Projection 1, 0").append("\r\n");
		meta = buf.toString();
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(meta);
		buf.append("Columns " + attributeNames.size()).append("\r\n");
		for (int i = 0; i < attributeNames.size(); i++) {
			buf.append("  " + attributeNames.get(i));
			buf.append(" " + attributeTypes.get(i)).append("\r\n");
		}
		buf.append(data).append("\r\n");
		
		return buf.toString();
	}

}
