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
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.Properties;

public class ConfigReader
{
	private final Properties props;
	private final File propertiesFile;
	
	public File getPropertiesFile()
	{
		return propertiesFile;
	}
	
	public static final String TRUE = "TRUE";
	public static final String YES = "YES";

	public static final String CLUSTER_NAME = "CLUSTER_NAME";
	public static final String FASTA_TO_SPLIT_PATH = "FASTA_TO_SPLIT_PATH";
	public static final String SPLIT_FASTA_DIR = "SPLIT_FASTA_DIR";
	public static final String NUMBER_CLUSTERS = "NUMBER_CLUSTERS";
	public static final String BLAST_BINARY_DIR = "BLAST_BINARY_DIR";
	public static final String FASTA_DIR_TO_FORMAT = "FASTA_DIR_TO_FORMAT";
	public static final String SCRIPTS_DIR_FOR_BLAST_FORMAT="SCRIPTS_DIR_FOR_BLAST_FORMAT";
	public static final String BLAST_PRELIMINARY_STRING = "BLAST_PRELIMINARY_STRING";
	public static final String CLUSTER_BATCH_COMMAND = "CLUSTER_BATCH_COMMAND";
	public static final String CHMOD_STRING = "CHMOD_STRING";
	public static final String POLL_TIME = "POLL_TIME";
	public static final String FASTA_FILE_TO_FORMAT_FOR_BLAST_DB = "FASTA_FILE_TO_FORMAT_FOR_BLAST_DB";
	public static final String BLAST_QUERY_DIRECTORY = "BLAST_QUERY_DIRECTORY";
	public static final String SCRIPTS_DIR_FOR_BLAST_QUERY = "SCRIPTS_DIR_FOR_BLAST_QUERY";
	public static final String BLAST_OUTPUT_DIRECTORY = "BLAST_OUTPUT_DIRECTORY";
	public static final String BLAST_GATHERED_TOP_HITS_FILE = "BLAST_GATHERED_TOP_HITS_FILE";
	public static final String GTF_GATHERED_TOP_HITS_FILE = "GTF_GATHERED_TOP_HITS_FILE";
	public static final String JAVA_VM_ARGS= "JAVA_VM_ARGS"; 
	public static final String GENOME_TO_INTEGER_FILE = "GENOME_TO_INTEGER_FILE";
	public static final String KMER_TO_HAS_GENOME_FILE = "KMER_TO_HAS_GENOME_FILE";
	
	public static final String DSK_INPUT_DIRECTORY = "DSK_INPUT_DIRECTORY";
	public static final String DSK_OUTPUT_DIRECTORY = "DSK_OUTPUT_DIRECTORY";
	public static final String DSK_BINARY_DIRECTORY = "DSK_BINARY_DIRECTORY";
	public static final String DSK_SCRIPT_DIR = "DSK_SCRIPT_DIR";
	
	public static final String MIN_NUMBER_OF_DIFFERENT_KMERS = "MIN_NUMBER_OF_DIFFERENT_KMERS";
	
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
	
	public String getAProperty(String namedProperty) throws Exception
	{
		Object obj = props.get(namedProperty);

		if (obj == null)
			return null;
		
		String val = obj.toString();
			
		// allow statements like thisDir = $someOtherDir to avoid re-typing paths
		if( val.startsWith("$"))
		{
			obj = props.getProperty(val.substring(1));
			
			if( obj == null)
				return null;
			
			val = obj.toString();
			
		}
		
		return val;
	}
	
	
	public ConfigReader(File propertiesFile) throws Exception
	{
		this.propertiesFile = propertiesFile;
		InputStream in = new FileInputStream(propertiesFile);
		props = new Properties();
		props.load(in);
		in.close();
	}
}
