/** 
 * @UNCC Fodor Lab
 * @author Anthony Fodor
 * @email anthony.fodor@gmail.com 
 * @date Feb 9, 2017
 * @disclaimer 	This code is free software; you can redistribute it and/or
 * 				modify it under the terms of the GNU General Public License
 * 				as published by the Free Software Foundation; either version 2
 * 				of the License, or (at your option) any later version,
 * 				provided that any use properly credits the author.
 * 				This program is distributed in the hope that it will be useful,
 * 				but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 				MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * 				GNU General Public License for more details at http://www.gnu.org * 
 */
package bioLockJ;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

public class ConfigReader
{
	private Properties props;
	private File propertiesFile;
	private Metadata metadata;
	private String runTimeStamp;

	protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyyMMdd_kkmmss" );

	public static final String PROJECT_NAME = "PROJECT_NAME";
	public static final String METADATA_FILE = "METADATA_FILE";
	public static final String METADATA_DESCRIPTOR = "METADATA_DESCRIPTOR";
	public static final String METADATA_DELIMITER = "METADATA_DELIMITER";
	public static final String METADATA_NULL_VALUE = "METADATA_NULL_VALUE";
	public static final String METADATA_COMMENT = "METADATA_COMMENT";

	public static final String PATH_TO_PROJECT_DIR = "PATH_TO_PROJECT_DIR";

	public static final String EMAIL_FROM = "EMAIL_FROM";
	public static final String EMAIL_TO = "EMAIL_TO";
	public static final String EMAIL_PASSWORD = "EMAIL_PASSWORD";

	// OPERATION FLAGS
	public static final String COPY_INPUT_FLAG = "COPY_INPUT_FLAG";
	public static final String EXIT_ON_ERROR_FLAG = "EXIT_ON_ERROR_FLAG";

	// STANDARD OPTIONAL PROPERTIES
	public static final String CLUSTER_NAME = "CLUSTER_NAME";
	public static final String CLUSTER_BATCH_COMMAND = "CLUSTER_BATCH_COMMAND";
	public static final String CLUSTER_PARAMS = "CLUSTER_PARAMS";
	public static final String CHMOD_STRING = "CHMOD_STRING";
	public static final String NUMBER_OF_JOBS_PER_CORE = "NUMBER_OF_JOBS_PER_CORE";
	public static final String POLL_TIME = "POLL_TIME";

	// PROP VALUE STRINGS
	public static final String LOG_FILE = "LOG_FILE";
	public static final String TRUE = "TRUE";
	public static final String FALSE = "FALSE";

	public static final String BLAST_PRELIMINARY_STRING = "BLAST_PRELIMINARY_STRING";

	public static final String FASTA_TO_SPLIT_PATH = "FASTA_TO_SPLIT_PATH";
	public static final String SPLIT_FASTA_DIR = "SPLIT_FASTA_DIR";
	public static final String BLAST_BINARY_DIR = "BLAST_BINARY_DIR";
	public static final String FASTA_DIR_TO_FORMAT = "FASTA_DIR_TO_FORMAT";
	public static final String FASTA_FILE_TO_FORMAT_FOR_BLAST_DB = "FASTA_FILE_TO_FORMAT_FOR_BLAST_DB";
	public static final String BLAST_QUERY_DIRECTORY = "BLAST_QUERY_DIRECTORY";
	public static final String BLAST_GATHERED_TOP_HITS_FILE = "BLAST_GATHERED_TOP_HITS_FILE";
	public static final String GTF_GATHERED_TOP_HITS_FILE = "GTF_GATHERED_TOP_HITS_FILE";

	public static final String REFERENCE_GENOME = "REFERENCE_GENOME";
	public static final String GC_CONTENT_IGV_OUTPUT_FILE = "GC_CONTENT_IGV_OUTPUT_FILE";
	public static final String GC_CONTENT_WINDOW_SIZE = "GC_CONTENT_WINDOW_SIZE";
	public static final String GC_CONTENT_STEP_SIZE = "GC_CONTENT_STEP_SIZE";

	public static final String OUTPUT_QUERY_COORDINATES_TO_GTF = "OUTPUT_QUERY_COORDINATES_TO_GTF";

	// should be either prot or nucl
	public static final String BLAST_DB_TYPE = "BLAST_DB_TYPE";
	public static final String BLAST_ALL_COMMAND = "BLAST_ALL_COMMAND";

	public static final String INPUT_GTF_FILE = "INPUT_GTF_FILE";
	public static final String OUTPUT_GTF_FILE = "OUTPUT_GTF_FILE";

	public static final String MBGD_EXTENDED_PATH = "MBGD_EXTENDED_PATH";

	public static final String PATH_TO_RDP_JAR = "PATH_TO_RDP_JAR";
	public static final String PATH_TO_INPUT_DIRECTORY = "PATH_TO_INPUT_DIRECTORY";
	public static final String RDP_THRESHOLD = "RDP_THRESHOLD";

	public static final String INPUT_DIRS = "INPUT_DIRS";

	public static final String PATH_TO_KRAKEN_DATABASE = "PATH_TO_KRAKEN_DATABASE";
	public static final String PATH_TO_KRAKEN_BINARY = "PATH_TO_KRAKEN_BINARY";
	public static final String KRAKEN_SWITCHES = "KRAKEN_SWITCHES";
	public static final String MPA_FORMAT = "MPA_FORMAT";

	public ConfigReader( File file, String password ) throws Exception
	{
		init( file, password );
	}

	public ConfigReader( File file ) throws Exception
	{
		init( file, null );
	}

	protected void init( File file, String password ) throws Exception
	{
		propertiesFile = file;
		props = getPropsFromFile( propertiesFile );

		String bljRoot = getBLJRoot();
		String projectDir = createProjectDir( bljRoot );

		props.setProperty( PATH_TO_PROJECT_DIR, projectDir );
		props.setProperty( LOG_FILE, getLogName( projectDir ) );
		metadata = Metadata.loadMetadata( this );

		if( password != null )
		{
			props.setProperty( EMAIL_PASSWORD, password );
		}
	}

	public Metadata getMetaData()
	{
		return metadata;
	}

	public String getResourcesDir() throws Exception
	{
		return getBLJRoot() + "resources" + File.separator;
	}

	public File getPropertiesFile()
	{
		return propertiesFile;
	}

	public String getAProperty( String namedProperty )
	{
		Object obj = props.get( namedProperty );

		if( obj == null )
			return null;

		String val = obj.toString();

		// allow statements like thisDir = $someOtherDir to avoid re-typing paths
		if( val.startsWith( "$" ) )
		{
			obj = props.getProperty( val.substring( 1 ) );

			if( obj == null )
				return null;

			val = obj.toString();
		}

		return val;
	}

	public ArrayList<String> getPropertyAsList( String namedProperty )
	{
		ArrayList<String> list = new ArrayList<String>();
		String val = getAProperty( namedProperty );
		if( val != null && val.trim().length() > 0 )
		{
			StringTokenizer st = new StringTokenizer( val, "," );
			while( st.hasMoreTokens() )
			{
				list.add( st.nextToken() );
			}
		}

		return list;
	}

	private static Properties getPropsFromFile( File propertiesFile ) throws Exception
	{
		InputStream in = new FileInputStream( propertiesFile );
		Properties tempProps = new Properties();
		tempProps.load( in );
		in.close();
		return tempProps;
	}

	public HashMap<String, String> getProperties() throws Exception
	{
		Properties tempProps = getPropsFromFile( propertiesFile );
		HashMap<String, String> map = new HashMap<String, String>();
		Iterator<String> it = tempProps.stringPropertyNames().iterator();
		while( it.hasNext() )
		{
			String key = it.next();
			map.put( key, tempProps.getProperty( key ) );
		}
		return map;
	}

	private String getLogName( String projectDir ) throws Exception
	{
		String val = projectDir + getAProperty( PROJECT_NAME ) + ".log";
		System.setProperty( LOG_FILE, val );
		return val;
	}

	private String createProjectDir( String bljRoot ) throws Exception
	{
		String projectName = getAProperty( PROJECT_NAME );
		String pathToProj = bljRoot + "projects" + File.separator + projectName;

		File projectDir = null;
		while( projectDir == null || projectDir.exists() )
		{
			runTimeStamp = DATE_FORMAT.format( new Date() );
			projectDir = new File( pathToProj + "_" + runTimeStamp );
			if( projectDir.exists() )
				Thread.sleep( 1000 );
		}

		if( !projectDir.mkdirs() )
		{
			throw new Exception( "ERROR: Unable to create: " + projectDir.getAbsolutePath() );
		}

		return projectDir.getAbsolutePath() + File.separator;
	}

	private static String getBLJRoot() throws IOException, URISyntaxException
	{
		URL u = ConfigReader.class.getProtectionDomain().getCodeSource().getLocation();
		File f = new File( u.toURI() );
		return f.getParent() + File.separator;
	}

}
