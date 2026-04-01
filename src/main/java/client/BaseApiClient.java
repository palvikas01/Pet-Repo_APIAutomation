package client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.specification.RequestSpecification;

public class BaseApiClient {
    public static Properties prop;

    protected final Logger log = LoggerFactory.getLogger(getClass());
    public BaseApiClient(){
          try {
			prop = new Properties();
			FileInputStream ip = new FileInputStream(System.getProperty("user.dir")+ "\\src\\main\\resources\\config.properties");
			prop.load(ip);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    
    }

   public static String initialization(){
		String baseURL = prop.getProperty("baseUrl");
        return baseURL;
   }

}
