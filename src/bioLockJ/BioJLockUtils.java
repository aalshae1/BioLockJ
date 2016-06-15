package bioLockJ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import utils.ConfigReader;

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
		
	public static File findProperyFile(String[] args ) throws Exception
	{
		if( args.length != 1)
			throw new Exception("Sole input should be property file");
		
		File aFile = new File(args[0]);
		
		if( ! aFile.exists())
			throw new Exception("Could not find " + aFile.getAbsolutePath());
		
		return aFile;
	}
	
	public File requireExistingDirectory(ConfigReader reader, String propertyName) throws Exception
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
	
	public static void copyPropertiesFile( File oldPropsFile, File outputDirectory )
		throws Exception
	{
		copyFile(oldPropsFile, new File(outputDirectory.getAbsolutePath() + File.separator + 
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
