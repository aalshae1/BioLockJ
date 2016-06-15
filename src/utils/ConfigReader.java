/** 
 * Author:  anthony.fodor@gmail.com    
 * This code is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version,
* provided that any use properly credits the author.
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details at http://www.gnu.org * * */


package utils;

import java.io.File;
import java.io.InputStream;

import java.util.Properties;

public class ConfigReader
{
	private final Properties props;
	private final File propertiesFile;
	
	public static final String TRUE = "TRUE";
	public static final String YES = "YES";

	public static final String JAVA_BASE_DIR = "JAVA_BASE_DIR";
	public static final String SCRIPT_BASE_DIR = "SCRIPT_BASE_DIR";
	public static final String BLAST_DIR = "BLAST_DIR";
	public static final String QUERY_SEQUENCE = "QUERY_SEQUENCE";
	public static final String CARDS_DATABASE_FASTA = "CARDS_DATABASE_FASTA";
	
	public boolean isSetToTrue(String namedProperty)
	{
		Object obj = props.get(namedProperty);

		if (obj == null)
			return false;

		if (obj.toString().equalsIgnoreCase(TRUE)
				|| obj.toString().equalsIgnoreCase(YES))
			return true;

		return false;
	}
	
	private String getAProperty(String namedProperty) throws Exception
	{
		Object obj = props.get(namedProperty);

		if (obj == null)
			throw new Exception("Error!  Could not find " + namedProperty
					+ " in " + this.propertiesFile);

		return obj.toString();
	}
	
	public String getBlastDirectory() throws Exception
	{
		return getAProperty(BLAST_DIR);
	}
	
	public ConfigReader(File propertiesFile) throws Exception
	{
		this.propertiesFile = propertiesFile;
		
		Object o = new Object();

		InputStream in = o.getClass().getClassLoader()
				.getSystemResourceAsStream(propertiesFile.getAbsolutePath());

		if (in == null)
			throw new Exception("Error!  Could not find " + propertiesFile.getAbsolutePath()
					+ " anywhere on the current classpath");

		props = new Properties();
		props.load(in);

	}
}
