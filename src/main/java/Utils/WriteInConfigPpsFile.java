/**
 * 
 */
package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author nitish kumar
 *
 */
public class WriteInConfigPpsFile {

	/**
	 * Setting up key and data pair value in config.properties file in product repo
	 * @param key
	 * @param data
	 */
	public void WritePropertiesFile(String key, String data) {
		FileOutputStream fileOut = null;
		FileInputStream fileIn = null;
		try {
			Properties configProperty = new Properties();
			File file = new File(System.getProperty("user.dir") + File.separator + "Parameters" + File.separator + "Config.properties");
			fileIn = new FileInputStream(file);
			configProperty.load(fileIn);
			configProperty.setProperty(key, data);
			fileOut = new FileOutputStream(file);
			configProperty.store(fileOut, "sample properties");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				fileOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		
		//args :- These are the Parameters being passed from Jenkins
	
		WriteInConfigPpsFile writeInConfig = new WriteInConfigPpsFile();
		writeInConfig.WritePropertiesFile("EmailId", args[0]);
		writeInConfig.WritePropertiesFile("RemoteAddress", args[1]);
		writeInConfig.WritePropertiesFile("RunType", args[2]);
		writeInConfig.WritePropertiesFile("AreaName", args[3]);
		writeInConfig.WritePropertiesFile("Environment", args[4]);
	}
}
