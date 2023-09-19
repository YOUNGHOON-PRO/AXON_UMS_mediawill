package com.pdf.convert;

import java.io.*;
import java.util.*;

public class chHtmlToPdfConfig 
{

	
	private Hashtable<String, String> configTable = null;
	private File configFile = null;
	private long lastLoadingTime = 0;
	
	public chHtmlToPdfConfig(String path)  
	{
		//String path = String.format("%s%c%s", Global.CONFIG_DIR, Global.PATH_GUBUN, Global.SYSTEM_CONFIG_FILE);
		configFile = new File(path);
		load();
	}
	
	public void load() 
	{
		FileInputStream in = null;
		Properties prop = null;
		Enumeration<Object> em = null;
		String key = null;
		String value = null;
		
		if (configTable == null) configTable = new Hashtable<String, String>();
			
		prop = new Properties();
		try {
			in = new FileInputStream(configFile);
			prop.load(in);
			in.close();
		} catch (IOException e) {
			System.out.println("[chHtmlToPdfConfig] load Error : "+e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					
				}
				in = null;
			}
		}
			
		try {
			
		em = prop.keys();
			while (em.hasMoreElements()) {
				key = ((String)em.nextElement());
				value = (String)(prop.remove(key));
			
					value = new String(value.getBytes("ISO-8859-1"),"utf-8");
				
				configTable.put(key.toUpperCase(), value);
			}
		} catch (UnsupportedEncodingException e) {
		}
		em = null;
		prop.clear();
		prop = null;
		
		
		lastLoadingTime = System.currentTimeMillis();
	}
	
	public long getLastLoadingTime()
	{
		return lastLoadingTime;
	}
	
	public String get(String key) 
	{
		String value = null;
		if (configTable == null) load();
		value = configTable.get(key.toUpperCase());
		return value;
	}
	
	public Integer getInt(String key) throws NumberFormatException
	{
		Integer value = 0;
		String valueStr = null;
		
		valueStr = get(key);
		if (valueStr == null) {
			value = 0;
		} else {
			try {
				value = Integer.parseInt(valueStr);
			} catch (Exception e) {
				value = 0;
			}
		}
		return value;
	}
	
	public boolean getBool(String key)
	{
		boolean value = false;
		String valueStr = null;
		
		valueStr = get(key);
		try {
			value = Boolean.valueOf(valueStr);
		} catch (Exception e) {
			value = false;
		}
		return value;
	}	
	
	public static void main(String[] args) throws IOException 
	{
		chHtmlToPdfConfig sc = new chHtmlToPdfConfig("D:\\업무용\\개발\\알바\\PDF\\config\\chhtmltopdf.prope2rties");
		System.out.println(sc.get("chhtmltopdf.file.path"));
	}
	
}

