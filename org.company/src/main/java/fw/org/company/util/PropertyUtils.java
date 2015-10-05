package fw.org.company.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public final class PropertyUtils {

	public static String getPropertyByName(String propertyName) {
		String propertyValue = null;
		
		//to load application's properties, we use this class
	    Properties mainProperties = new Properties();
		try {
			FileInputStream file;
			String path = "./project.properties";
	
			//load the file handle for main.properties
			file = new FileInputStream(path);
	
			//load all the properties from this file
			mainProperties.load(file);
	
			//we have loaded the properties, so close the file handle
			file.close();
	
			//retrieve the property we are intrested, the app.version
			propertyValue = mainProperties.getProperty(propertyName);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return propertyValue;
	}

	public static String[] getPropertyValueAsArray(String propertyName) {
	
		String propertyValue = getPropertyByName(propertyName);
		return StringUtils.split(propertyValue, " ");
	}

}
