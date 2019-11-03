package org.fabri1983.eternity2.core.resourcereader;

import java.io.IOException;
import java.util.Properties;

public class AppPropertiesReader {

	public static final Properties readProperties() throws IOException {
		Properties properties = new Properties();
		String file = "application.properties";
		properties.load(AppPropertiesReader.class.getClassLoader().getResourceAsStream(file));
		return properties;
	}

	public static final String getProperty(Properties properties, String key) {
		String sysProp = System.getProperty(key);
		if (sysProp != null && !"".equals(sysProp))
			return sysProp;
		return properties.getProperty(key);
	}
	
}
