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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import bioLockJ.BioLockJUtils;

public class ConfigReader
{
	private final Properties props;
	private final File propertiesFile;
	private String runTimeStamp;
	
	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_kkmmss");
	
	
	public File getPropertiesFile()
	{
		return propertiesFile;
	}
	
	public static final String TRUE = "TRUE";
	public static final String FALSE= "FALSE";

	public static final String CLUSTER_NAME = "CLUSTER_NAME";
	public static final String BLAST_PRELIMINARY_STRING = "BLAST_PRELIMINARY_STRING";
	public static final String NUMBER_CLUSTERS = "NUMBER_CLUSTERS";
	public static final String CLUSTER_BATCH_COMMAND = "CLUSTER_BATCH_COMMAND";
	public static final String CLUSTER_PARAMS = "CLUSTER_PARAMS";
	public static final String CHMOD_STRING = "CHMOD_STRING";
	public static final String NUMBER_OF_JOBS_PER_CORE="NUMBER_OF_JOBS_PER_CORE";
	public static final String POLL_TIME = "POLL_TIME";
	
	public static final String FASTA_TO_SPLIT_PATH = "FASTA_TO_SPLIT_PATH";
	public static final String SPLIT_FASTA_DIR = "SPLIT_FASTA_DIR";
	public static final String BLAST_BINARY_DIR = "BLAST_BINARY_DIR";
	public static final String FASTA_DIR_TO_FORMAT = "FASTA_DIR_TO_FORMAT";
	public static final String FASTA_FILE_TO_FORMAT_FOR_BLAST_DB = "FASTA_FILE_TO_FORMAT_FOR_BLAST_DB";
	public static final String BLAST_QUERY_DIRECTORY = "BLAST_QUERY_DIRECTORY";
	public static final String BLAST_GATHERED_TOP_HITS_FILE = "BLAST_GATHERED_TOP_HITS_FILE";
	public static final String GTF_GATHERED_TOP_HITS_FILE = "GTF_GATHERED_TOP_HITS_FILE";
	public static final String JAVA_VM_ARGS= "JAVA_VM_ARGS"; 
	public static final String GENOME_TO_INTEGER_FILE = "GENOME_TO_INTEGER_FILE";
	public static final String KMER_TO_HAS_GENOME_FILE = "KMER_TO_HAS_GENOME_FILE";
	
	public static final String DSK_INPUT_DIRECTORY = "DSK_INPUT_DIRECTORY";
	public static final String DSK_BINARY_DIRECTORY = "DSK_BINARY_DIRECTORY";
	public static final String MIN_NUMBER_OF_DIFFERENT_KMERS = "MIN_NUMBER_OF_DIFFERENT_KMERS";
	
	public static final String STRAIN_METADATA_FILE = "STRAIN_METADATA_FILE";
	public static final String FISHER_PVALUES_OUTPUT_FILE = "FISHER_PVALUES_OUTPUT_FILE";
	public static final String FISHER_CONDITION_1 = "FISHER_CONDITION_1";
	public static final String FISHER_CONDITION_2 = "FISHER_CONDITION_2";
	public static final String FISHER_GTF_OUTPUT_FILE = "FISHER_GTF_OUTPUT_FILE";
	
	public static final String REFERENCE_GENOME="REFERENCE_GENOME";
	public static final String GC_CONTENT_IGV_OUTPUT_FILE = "GC_CONTENT_IGV_OUTPUT_FILE";
	public static final String GC_CONTENT_WINDOW_SIZE = "GC_CONTENT_WINDOW_SIZE";
	public static final String GC_CONTENT_STEP_SIZE = "GC_CONTENT_STEP_SIZE";
	
	public static final String OUTPUT_QUERY_COORDINATES_TO_GTF="OUTPUT_QUERY_COORDINATES_TO_GTF";
	
	// should be either prot or nucl
	public static final String BLAST_DB_TYPE = "BLAST_DB_TYPE";
	public static final String BLAST_ALL_COMMAND="BLAST_ALL_COMMAND";
	
	public static final String INPUT_GTF_FILE="INPUT_GTF_FILE";
	public static final String OUTPUT_GTF_FILE="OUTPUT_GTF_FILE";
	
	public static final String MBGD_EXTENDED_PATH="MBGD_EXTENDED_PATH";
	public static final String CONSERVED_KMER_FOR_REFERENCE_OUPUT_FILE="CONSERVED_KMER_FOR_REFERENCE_OUPUT_FILE";
	
	public static final String PATH_TO_RDP_JAR="PATH_TO_RDP_JAR";
	public static final String PATH_TO_INPUT_RDP_FASTA_DIRECTORY="PATH_TO_INPUT_RDP_FASTA_DIRECTORY";
	public static final String RDP_THRESHOLD = "RDP_THRESHOLD";
	
	
	public static final String PATH_TO_KRAKEN_DATABASE = "PATH_TO_KRAKEN_DATABASE";
	public static final String PATH_TO_KRAKEN_BINARY = "PATH_TO_KRAKEN_BINARY";
	
	public static final String PROJECT_NAME = "PROJECT_NAME"; 
	public static final String RUN_TIMESTAMP = "RUN_TIMESTAMP";
	public static final String LOG_FILE = "LOG_FILE"; 
	
	public static final String PATH_TO_BLJ_ROOT = "PATH_TO_BLJ_ROOT"; 
	public static final String PATH_TO_PROJECT_DIR = "PATH_TO_PROJECT_DIR"; 
	public static final String PATH_TO_SCRIPT_DIR = "PATH_TO_SCRIPT_DIR"; 
	public static final String PATH_TO_SUMMARY_DIR = "PATH_TO_SUMMARY_DIR"; 
	public static final String PATH_TO_OUTPUT_DIR = "PATH_TO_OUTPUT_DIR"; 
	
	
	
	public String getAProperty(String namedProperty)
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
				
		String bljRoot = getBLJRoot();
		String projectDir = createProjectDir(bljRoot);
		
		props.setProperty(RUN_TIMESTAMP, runTimeStamp);
		props.setProperty(PATH_TO_BLJ_ROOT, bljRoot);
		props.setProperty(PATH_TO_PROJECT_DIR, projectDir);
		props.setProperty(PATH_TO_OUTPUT_DIR, createSubDir(projectDir, PATH_TO_OUTPUT_DIR, "output"));
		props.setProperty(PATH_TO_SCRIPT_DIR, createSubDir(projectDir, PATH_TO_SCRIPT_DIR, "scripts"));
		props.setProperty(PATH_TO_SUMMARY_DIR, createSubDir(projectDir, PATH_TO_SUMMARY_DIR, "summary"));
		props.setProperty(LOG_FILE, getLogName(projectDir));
		
		
		verifyProjectDirs();
	}
	
	
	public HashMap<String, String> getProperties()
	{
		HashMap<String, String> map = new HashMap<String, String>();
		Iterator<String> it = props.stringPropertyNames().iterator();
		while(it.hasNext()){
			String key = it.next();
			map.put(key, props.getProperty(key));
		}
		return map;
	}
	
	private void verifyProjectDirs() throws Exception
	{
		BioLockJUtils.requireExistingDirectory(this, ConfigReader.PATH_TO_BLJ_ROOT);
		BioLockJUtils.requireExistingDirectory(this, ConfigReader.PATH_TO_PROJECT_DIR);
		BioLockJUtils.requireExistingDirectory(this, ConfigReader.PATH_TO_OUTPUT_DIR);
		BioLockJUtils.requireExistingDirectory(this, ConfigReader.PATH_TO_SUMMARY_DIR);
		BioLockJUtils.requireExistingDirectory(this, ConfigReader.PATH_TO_SCRIPT_DIR);
	}
	
	private String getLogName(String projectDir) throws Exception
	{
		return projectDir +  BioLockJUtils.requireString(this, PROJECT_NAME) + ".log";
	}
	
	private String createSubDir(String parentDir, String propName, String subDirPath)
	{ 
		String subDir = getAProperty(propName);
		if(subDir==null)
		{
			subDir = parentDir + subDirPath;
		}
		
		File dir = new File(subDir);
		if(!dir.exists())
		{
			dir.mkdir();
		}
		
		return dir.getAbsolutePath() + File.separator;
	} 
	
	private String createProjectDir(String bljRoot) throws Exception
	{
		
		String projectName = BioLockJUtils.requireString(this, PROJECT_NAME);
		String pathToProj = getAProperty(ConfigReader.PATH_TO_PROJECT_DIR);
		if(pathToProj==null)
		{
			pathToProj = bljRoot + "projects"; 
		}
		else if (pathToProj.trim().endsWith(File.separator))
		{
			pathToProj = BioLockJUtils.removeLastChar(pathToProj);
		}

		pathToProj = pathToProj + File.separator + projectName;
		
		File projectDir = null;
		while(projectDir == null || projectDir.exists())
		{
			runTimeStamp = DATE_FORMAT.format(new Date());
			projectDir = new File(pathToProj + "_" + runTimeStamp);
			if(projectDir.exists()) Thread.sleep(1000);
		}
		
		projectDir.mkdirs();
		return projectDir.getAbsolutePath() + File.separator;
	}
	
	
	
	
	
	private static String getBLJRoot() throws IOException, URISyntaxException
	{
		URL u = ConfigReader.class.getProtectionDomain().getCodeSource().getLocation();
		File f = new File(u.toURI());
		return f.getParent() + File.separator;
	}
	

}
