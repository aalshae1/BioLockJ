package bioLockJ;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import org.slf4j.*;

import utils.ConfigReader;
import utils.ProcessWrapper;

public class BioLockJUtils
{
	protected static final Logger log = LoggerFactory.getLogger(BioLockJUtils.class);

	public static void executeAndWaitForScriptsIfAny(BioLockJExecutor bje) throws Exception
	{
		bje.executeProjectFile();
		if( bje.hasScripts() )
		{
			int pollTime = 15;
			try
			{
				pollTime = requirePositiveInteger(bje.getConfig(), ConfigReader.POLL_TIME);
			}
			catch(Exception ex)
			{
				log.warn("Could not set " + ConfigReader.POLL_TIME + ".  Setting poll time to " + 
								pollTime +  " seconds ", ex);		
			}

			executeCHMOD_ifDefined(bje.getConfig(), bje.getScriptDir());
			executeFile(bje.getRunAllFile());
			pollAndSpin(bje.getScriptFiles(), pollTime );
		}
	}
	
	
	
	
	public static void noteStartToLogWriter( BioLockJExecutor invoker )
	{
		log.info("");
		log.info("Starting " + invoker.getClass().getName());
	}
		
	public static void noteEndToLogWriter( BioLockJExecutor invoker )
	{
		log.info("Finished " + invoker.getClass().getName());
	}
	
	public static String requireDBType(ConfigReader cReader) throws Exception
	{
		String val = cReader.getAProperty(ConfigReader.BLAST_DB_TYPE);
		
		if( val == null)
			throw new Exception("Could not find " + ConfigReader.BLAST_DB_TYPE);
		
		if( !val.equals("prot") && ! val.equals("nucl"))
			throw new Exception(ConfigReader.BLAST_DB_TYPE + " must be either prot or nucl");
		
		return val;
	}
	
	public static boolean getBoolean(ConfigReader cReader, String propertyName, boolean isRequired) throws Exception
	{
		String val = cReader.getAProperty(propertyName);
		
		if( isRequired && val == null )
			throw new Exception("Could not find " + propertyName);
		else if ( val == null )
			return false;

		if( val.equalsIgnoreCase(ConfigReader.TRUE))
			return true;
		
		if( val.equalsIgnoreCase(ConfigReader.FALSE))
			return false;
		
		throw new Exception(propertyName + " must be set to either " + ConfigReader.TRUE + " or " +
								ConfigReader.FALSE);	
	}
	
//	public static boolean requireBoolean(ConfigReader cReader, String propertyName) throws Exception
//	{
//		String val = cReader.getAProperty(propertyName);
//		
//		if( val == null)
//			throw new Exception("Could not find " + propertyName);
//		
//		if( val.equalsIgnoreCase(ConfigReader.TRUE))
//			return true;
//		
//		if( val.equalsIgnoreCase(ConfigReader.FALSE))
//			return false;
//		
//		throw new Exception(propertyName + " must be set to either " + ConfigReader.TRUE + " or " +
//								ConfigReader.FALSE);	
//	}


	public static void executeCHMOD_ifDefined(ConfigReader cReader, File scriptDir)  throws Exception
	{
		String chmod = cReader.getAProperty(ConfigReader.CHMOD_STRING);
		if( chmod != null )
		{
			File folder = new File(scriptDir.getAbsolutePath());
			File[] listOfFiles = folder.listFiles();
			for(File file: listOfFiles)
			{
				if(!file.getName().startsWith("."))
				{
					new ProcessWrapper(getArgs(chmod, file.getAbsolutePath()));
				}
			}	
		}
	}
	
	
	private static String[] getArgs(String command, String filePath) throws Exception
	{
		StringTokenizer sToken = new StringTokenizer(command + " " + filePath);
		List<String> list = new ArrayList<String>();
		while(sToken.hasMoreTokens())
			list.add(sToken.nextToken());
		
		String[] args = new String[list.size()];
		
		for( int x=0; x  < list.size(); x++)
			args[x] = list.get(x);;
		
		return args;
	}
	
	
	public static void pollAndSpin(List<File> scriptFiles, int pollTime) throws Exception
	{
		boolean finished = false;
		
		while( !finished )
		{
			finished = poll(scriptFiles);
			
			if( !finished )
			{
				Thread.sleep(pollTime * 1000);
			}
		}
	}
	
	public static boolean poll(List<File> scriptFiles) throws Exception
	{
		int numSuccess = 0;
		int numFailed = 0;
		
		for(File f : scriptFiles)
		{
			File testSuccess = new File(f.getAbsolutePath() + ScriptBuilder.SCRIPT_SUCCEEDED);
			
			if(testSuccess.exists())
			{
				numSuccess++;
			}
			else
			{ 
				File testFailure = new File(f.getAbsolutePath() + ScriptBuilder.SCRIPT_FAILED);
				if(testFailure.exists())
				{
					numFailed++;
				}
				else
				{
					log.info(f.getAbsolutePath() + " not finished ");
				}
			}
		}
		
		log.info("Script Status (Total=" + scriptFiles.size() + "): Success=" + numSuccess + "; Failures=" + numFailed);
		return (numSuccess + numFailed) == scriptFiles.size();
	}
	
	public static void executeFile(File f) throws Exception
	{
		String[] cmd = new String[1];
		cmd[0] = f.getAbsolutePath();
		new ProcessWrapper(cmd);
	}
	

	public static String requireString(ConfigReader reader, String propertyName) throws Exception
	{
		String val = reader.getAProperty(propertyName);
		
		if( val == null)
			throw new Exception("Could not find " + propertyName + " in " 
							+ reader.getPropertiesFile().getAbsolutePath());
		
		return val;
	}
	

	public static String getStringOrNull(ConfigReader reader, String propertyName) throws Exception
	{
		return  reader.getAProperty(propertyName);
	}
	
	public static File requireExistingDirectory(ConfigReader reader, String propertyName) throws Exception
	{
		String val = reader.getAProperty(propertyName);
		
		if( val == null || val.trim().isEmpty() )
			throw new Exception(propertyName + " is not defined in " 
								+ reader.getPropertiesFile().getAbsolutePath());
		
		File aFile = new File(reader.getAProperty(propertyName));
		
		if( ! aFile.exists() || ! aFile.isDirectory() )
			throw new Exception(aFile.getAbsolutePath() + " is not a valid directory ");
		
		return aFile;
	}
	
	public static int requirePositiveInteger( ConfigReader reader, String propertyName )
		throws Exception
	{
		String val = reader.getAProperty(propertyName);
		
		if( val == null)
			throw new Exception(propertyName + " is not defined in " 
								+ reader.getPropertiesFile().getAbsolutePath());
		
		Integer aVal = null;
		
		try
		{
			aVal = Integer.parseInt(val);
		}
		catch(Exception ex)
		{
			log.error(ex.getMessage());
			ex.printStackTrace();
		}
		
		if( aVal == null  || aVal < 1)
			throw new Exception(propertyName + " must be a positive integer in " + 
											reader.getPropertiesFile().getAbsolutePath());
		
		return aVal;
		
	}
	
	public static File requireExistingFile(ConfigReader reader, String propertyName) throws Exception
	{
		String val = reader.getAProperty(propertyName);
		
		if( val == null)
			throw new Exception(propertyName + " is not defined in " 
								+ reader.getPropertiesFile().getAbsolutePath());
		
		File aFile = new File(reader.getAProperty(propertyName));
		
		if( ! aFile.exists() )
			throw new Exception(aFile.getAbsolutePath() + " is not an existing file ");
		
		return aFile;
	}
	
	public static void copyPropertiesFile( File propsFile, String projectDir )
		throws Exception
	{
		copyFile(propsFile, new File(projectDir + propsFile.getName()));
	}
	
	public static void logConfigFileSettings(ConfigReader reader) throws Exception 
	{    
		HashMap<String, String> map = reader.getProperties();
		Iterator<String> it = map.keySet().iterator();
		log.info(LOG_SPACER);
		log.info("Property Config Settings");
		log.info(LOG_SPACER);
		while(it.hasNext()){
			String key = it.next();
			log.info(key + " = " + map.get(key));
		}
		log.info(LOG_SPACER);
	}
	
	
	
	
	
	public static String[] getFilePaths(File inputDir)
	{
		String[] input = inputDir.list();
		ArrayList<String> list = new ArrayList<String>();
		for(String s : input)
		{
			if(!s.startsWith(".")) // ignore hidden files
			{
				File file = new File(inputDir.getAbsolutePath() + File.separator + s);
				if( !file.isDirectory() )
				{
					list.add(s);
				}
			}
		}
		
		String[] files = new String[list.size()];
		int index = 0;
		for(String s : list)
		{
			files[index++] = s;
		}
		
		return files;
	}
	
	
	
	public static String removeLastChar(String val)
	{
		return val.substring(0, val.trim().length()-1);
	}
	
	public static String formatInt(int x, int numDigits)
	{
		String xString = String.valueOf(x);
		int xLength = xString.length();
		while(xLength<numDigits)
		{
			xString = "0" + xString;
			xLength = xString.length();
		}
		
		return xString;
	}

	//http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
	private static void copyFile(File sourceFile, File destFile) throws Exception 
	{    
		if(!destFile.exists()) destFile.createNewFile();
	    FileInputStream fileInputStream = new FileInputStream(sourceFile);
	    FileOutputStream fileOutputStream = new FileOutputStream(destFile);
	    FileChannel source = null, destination = null;
	    try{
	    	source = fileInputStream.getChannel();
	        destination = fileOutputStream.getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally{
	        if(source!= null) source.close();
	        if(destination != null) destination.close();
	        if(fileInputStream != null) fileInputStream.close();
	        if(fileOutputStream != null) fileOutputStream.close();
	    }
	}
	
	public static final String LOG_SPACER = "=================================================";
}
