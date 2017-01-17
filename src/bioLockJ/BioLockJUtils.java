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
	
	public static final String FINISHED_SUFFIX = "_succesfullyFinished";
	
	public static void executeAndWaitForScriptsIfAny(BioLockJExecutor bje) throws Exception
	{
		bje.executeProjectFile();
		
		if( bje.getRunAllFile() != null)
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
			
			executeCHMOD_ifDefined(bje.getConfig(), bje.getRunAllFile());
			
			if(log!=null)
			{
				log.info("EXITING PROGRAM EARLY");
			}
			else
			{
				System.out.println("SysOut::EXITING PROGRAM EARLY");
				log.info("EXITING PROGRAM EARLY");
				executeFile(bje.getRunAllFile());
				pollAndSpin(bje.getScriptFiles(), pollTime );
			}
			
		}
	}
	
	public static void noteStartToLogWriter( BioLockJExecutor invoker )
	{
		log.info("starting " + invoker.getClass().getName() + "\n");
	}
		
	public static void noteEndToLogWriter( BioLockJExecutor invoker )
	{
		log.info("Finished " + invoker.getClass().getName() + "\n");
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
	
	public static boolean requireBoolean(ConfigReader cReader, String propertyName) throws Exception
	{
		String val = cReader.getAProperty(propertyName);
		
		if( val == null)
			throw new Exception("Could not find " + propertyName);
		
		if( val.equalsIgnoreCase(ConfigReader.TRUE))
			return true;
		
		if( val.equalsIgnoreCase(ConfigReader.FALSE))
			return false;
		
		throw new Exception(propertyName + " must be set to either " + ConfigReader.TRUE + " or " +
								ConfigReader.FALSE);	
	}
	
//	public static void logAndRethrow(Exception ex)
//		throws Exception
//	{
//		log.error(ex.toString());
//		throw ex;
//	}
	
		
	public static void executeCHMOD_ifDefined(ConfigReader cReader, File file )  throws Exception
	{
		if( cReader.getAProperty(ConfigReader.CHMOD_STRING) != null)
		{
			StringTokenizer sToken = new StringTokenizer(cReader.getAProperty(ConfigReader.CHMOD_STRING) + " " + 
								file.getAbsolutePath());
			List<String> list = new ArrayList<String>();
			
			while(sToken.hasMoreTokens())
				list.add(sToken.nextToken());
			
			String[] args = new String[list.size()];
			
			for( int x=0; x  < list.size(); x++)
				args[x] = list.get(x);
			
			new ProcessWrapper(args);
		}
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
		
		for(File f : scriptFiles)
		{
			File test = new File(f.getAbsolutePath() + FINISHED_SUFFIX);
			
			if(test.exists())
			{
				numSuccess++;
			}
			else
			{
				log.info(f.getAbsolutePath() + " not succesfully finished ");
			}
		}
		
		log.info("\n Finished " + numSuccess + " of " + scriptFiles.size() + "\n");
		
		return numSuccess == scriptFiles.size();
	}
	
	public static void executeFile(File f) throws Exception
	{
		String[] cmd = new String[1];
		cmd[0] = f.getAbsolutePath();
		new ProcessWrapper(cmd);
	}
	
	public static File findProperyFile(String[] args ) throws Exception
	{
		if( args.length != 1)
			throw new Exception("Sole input should be property file");
		
		File aFile = new File(args[0]);
		
		if( ! aFile.exists())
			throw new Exception("Could not find " + aFile.getAbsolutePath());
		
		return aFile;
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
		while(it.hasNext()){
			String key = it.next();
			log.info(key + " = " + map.get(key));
		}
		log.info(LOG_SPACER);
	}
	
	

	
	public static void closeRunFile(BufferedWriter aWriter, File runFile) throws Exception
	{
		File touchFile = new File(runFile.getAbsolutePath() + FINISHED_SUFFIX );
		if( touchFile.exists()) touchFile.delete();
		aWriter.write("touch " + touchFile.getAbsolutePath() + "\n");
		aWriter.flush();  aWriter.close();
	}
	
	
	
	public static String removeLastChar(String val)
	{
		return val.substring(0, val.trim().length()-1);
	}
	
	public static String formatInt(int x)
	{
		if(x<1)
		{
			return "00";
		}
		else if(x<10)
		{
			return "0" + x;
		}
		
		return new Integer(x).toString();
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
