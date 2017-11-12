package se.cenote.budget.dao;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Properties;

public class StoreManagerFactory {
	
	public static final String KEY_CLASS_NAME = "store.classname";
	public static final String KEY_DIRECTORY_PATH = "store.directory";
	
	private static final String DEFAULT_CLASS_NAME = "se.cenote.budget.dao.fs.StoreManagerImpl";
	private static final String DEFAULT_DIRECTORY_PATH = "src/test/resources/";
	
	private Properties props;
	
	public StoreManagerFactory(Properties props) {
		this.props = props;
	}
	
	public StoreManager getStoreManager(){
		StoreManager storeMgr = null;
		try{
			String className = props.getProperty(KEY_CLASS_NAME, DEFAULT_CLASS_NAME);
			
			String path = props.getProperty(KEY_DIRECTORY_PATH, DEFAULT_DIRECTORY_PATH);
			File dir = new File(path);
			
			Class<?> clazz = Class.forName(className);
			
			Constructor<?> constructor = clazz.getConstructor(File.class);
			storeMgr = (StoreManager)constructor.newInstance(dir);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return storeMgr;
	}

}
