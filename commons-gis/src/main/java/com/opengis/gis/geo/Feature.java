package com.opengis.gis.geo;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author chenlly(chenlly@126.com)
 * 
 */
public abstract class Feature implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected Object[] attributes;
	protected Geometry geometry;
	protected Schema schema;

	public Feature(Schema schema) {
		this.schema = schema;
		attributes = new Object[schema.getAttributeCount()];
	}
	
	/**
	 * 此方法存在bug,添加字段必须是克隆出来的对象，否则字段会出现无限次的追加
	 * @deprecated
	 * @param attributeName
	 * @param attributeType
	 * @param newValue
	 */
	public void add(String attributeName,String attributeType,Object newValue){
		if(!schema.hasAttribute(attributeName)){
			//添加新属性
			schema.addAttribute(attributeName, attributeType);
		}
		Object[] obj = new Object[schema.getAttributeCount()];
		System.arraycopy(attributes, 0, obj, 0, attributes.length);
		obj[schema.getAttributeCount()-1] = newValue;
		attributes = obj;
	}

	public Schema getSchema() {
		return schema;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public void setAttribute(String attributeName, Object newAttribute) {
		setAttribute(schema.getAttributeIndex(attributeName), newAttribute);
	}
	
	public void setAttribute(int attributeIndex, Object newAttribute) {
		attributes[attributeIndex] = newAttribute;
	}

	public Object getAttribute(String attributeName) {
		return getAttribute(schema.getAttributeIndex(attributeName));
	}

	public Object getAttribute(int attributeIndex) {
		return attributes[attributeIndex];
	}

	public Object[] getAttributes() {
		return attributes;
	}

	public String getString(int attributeIndex) {
		Object result = getAttribute(attributeIndex);
		if (result != null)
			return result.toString();
		else
			return "";
	}

	public String getString(String attributeName) {
		Object result = getAttribute(attributeName);
		if (result != null)
			return result.toString();
		else
			return "";
	}
	
	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
}
