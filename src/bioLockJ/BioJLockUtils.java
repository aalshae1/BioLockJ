package bioLockJ;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import utils.ConfigReader;
import utils.ProcessWrapper;

public class BioJLockUtils
{
	//http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
		private static void copyFile(File sourceFile, File destFile) throws Exception {
		    if(!destFile.exists()) {
		        destFile.createNewFile();
		    }

		    FileChannel source = null;
		    FileChannel destination = null;

		    try {
		        source = new FileInputStream(sourceFile).getChannel();
		        destination = new FileOutputStream(destFile).getChannel();
		        destination.transferFrom(source, 0, source.size());
		    }
		    finally {
		        if(source != null) {
		            source.close();
		        }
		        if(destination != null) {
		            destination.close();
		        }
		    }
		}
		
	public static void logAndRethrow(BufferedWriter logWriter, Exception ex)
		throws Exception
	{
		logWriter.write("Terminating at " + new Date().toString());
		logWriter.write(ex.toString());
		logWriter.flush();
		throw ex;
	}
		
	public static void executeCHMOD_ifDefined(ConfigReader cReader )  throws Exception
	{
		if( cReader.getAProperty(ConfigReader.CHMOD_STRING) != null)
		{
			StringTokenizer sToken = new StringTokenizer(ConfigReader.CHMOD_STRING);
			List<String> list = new ArrayList<String>();
			
			while(sToken.hasMoreTokens())
				list.add(sToken.nextToken());
			
			String[] args = new String[list.size()];
			
			for( int x=0; x  < list.size(); x++)
				args[x] = list.get(x);
			
			new ProcessWrapper(args);
		}
	}
	
	public static void executeFile(File f) throws Exception
	{
		String[] cmd = new String[1];
		cmd[1] = f.getAbsolutePath();
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
	
	public static void appendSuccessToPropertyFile( File propertyFile, String invokingClass,
			File logDirectory) throws Exception
	{
		FileWriter fWriter = new FileWriter(propertyFile, true);
		BufferedWriter writer = new BufferedWriter(fWriter);
		PrintWriter out = new PrintWriter(writer);
		
		out.write("\n# ran " + invokingClass + " log to " + logDirectory.getAbsolutePath() + " " + 
						new Date().toString() + "\n");
		
		out.flush(); out.close();
	}
	
	public static String requireString(ConfigReader reader, String propertyName) throws Exception
	{
		String val = reader.getAProperty(propertyName);
		
		if( val == null)
			throw new Exception("Could not find " + propertyName + " in " 
							+ reader.getPropertiesFile().getAbsolutePath());
		
		return val;
	}
	
	public static File requireExistingDirectory(ConfigReader reader, String propertyName) throws Exception
	{
		String val = reader.getAProperty(propertyName);
		
		if( val == null)
			throw new Exception(propertyName + " is not defined in " 
								+ reader.getPropertiesFile().getAbsolutePath());
		
		File aFile = new File(reader.getAProperty(propertyName));
		
		if( ! aFile.exists() || ! aFile.isDirectory())
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
	
	public static void copyPropertiesFile( File oldPropsFile, File logDirectory )
		throws Exception
	{
		copyFile(oldPropsFile, new File(logDirectory.getAbsolutePath() + File.separator + 
						oldPropsFile.getName()));
	}
	
	public static File createLogDirectory( File outputDirectory, String parentInvoker ) throws Exception
	{
		while(true)
		{
			File logDir = null;
			
			while( logDir == null || logDir.exists() )
			{
				logDir = new File(outputDirectory.getAbsolutePath() + File.separator + 
							"log_" + parentInvoker + "_" +  System.currentTimeMillis());
				
				if( logDir.exists())
					Thread.sleep(100);
				
			}
			
			logDir.mkdirs();
			
			return logDir;
		}
	}
}
