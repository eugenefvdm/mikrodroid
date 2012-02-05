package com.mikrodroid.router;

/**
 * ConfigItem consists of name/value configuration pairs
 * 
 * @author eugene
 *
 */
public class ConfigItem {
	
	public String name;
	String value;

	public void setName(String s) {
		this.name = s;
	}
	
	public String getName() {
		return name;
	}

	public void setValue(String s) {
		this.value = s;
	}
	
	public String getValue() {
		return value;
	}
	
	/**
	 * Override Equals to compare object (called in list contains)
	 * See http://stackoverflow.com/questions/185937/overriding-the-java-equals-method-quirk
	 * I had to implement this when using the contains(c) method to see if the parameter is already a favourite
	 */
	@Override
	public boolean equals(Object other) {
	    if (other == null) return false;
	    if (other == this) return true;
	    if (this.getClass() != other.getClass())return false;
	    ConfigItem otherMyClass = (ConfigItem)other;
	    if (otherMyClass.name.equals(this.name)) {	    	
	    	return true;	    	
	    } else {	    	
	    	return false;	    	
	    }
	    
	}
	
}
