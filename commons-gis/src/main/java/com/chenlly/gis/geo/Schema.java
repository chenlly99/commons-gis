package com.chenlly.gis.geo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author chenlly(chenlly@126.com)
 * 
 */
public abstract class Schema implements Serializable{
	private static final long serialVersionUID = 1L;
	

	protected List<String> attributeNames;
	public List<String> getAttributeNames() {
		return attributeNames;
	}

	public List<String> getAttributeTypes() {
		return attributeTypes;
	}

	protected List<String> attributeTypes;

	public Schema() {
		attributeNames = new ArrayList<String>();
		attributeTypes = new ArrayList<String>();
	}

	public int getAttributeCount() {
		return attributeNames.size();
	}

	public int getAttributeIndex(String attributeName) {
		int index = -1;
		for(int i = 0; i<attributeNames.size(); i++){
			String name = attributeNames.get(i).toUpperCase();
			if(name.equals(attributeName)){
				index = i;
			}
		}
		
		if (index < 0)
			throw new IllegalArgumentException("Unrecognized attribute name: "
					+ attributeName);
		return index;
	}

	public boolean hasAttribute(String attributeName) {
		boolean flag = false;
		for(int i = 0; i<attributeNames.size(); i++){
			String name = attributeNames.get(i).toUpperCase();
			if(name.equals(attributeName)){
				flag = true;
				break;
			}
		}
		return flag;
	}
	
	public String getAttributeName(int attributeIndex) {
        return attributeNames.get(attributeIndex);
    }
	
	public String getAttributeType(int attributeIndex) {
        return attributeTypes.get(attributeIndex);
    }
	
	public String getAttributeType(String attributeName) {
        return this.getAttributeType(this.getAttributeIndex(attributeName));
    }
	
	public void addAttribute(String attributeName, String attributeType) {
		attributeNames.add(attributeName);
		attributeTypes.add(attributeType);
	}
}
