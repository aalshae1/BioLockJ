package bioLockJ;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import utils.ConfigReader;

/** 
 * 
 */
public class BioLockJ
{
	
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			System.out.println("Usage " + BioLockJ.class.getName() + " <FULL PATH TO PROP FILE>");
			System.out.println("TERMINATE PROGRAM");
			System.exit(1);
		}
		
		File propFile = new File(args[0]);
		if( !propFile.exists() || propFile.isDirectory() )
			throw new Exception(propFile.getAbsolutePath() + " is not a valid file");
		
		Logger log = null;
		try{
			runProgram(propFile);
		}catch(Exception ex){
			log = LoggerFactory.getLogger(BioLockJ.class);
			log.error(ex.getMessage(), ex);
		}
		
		if(log==null) log = LoggerFactory.getLogger(BioLockJ.class);
		log.info(BioLockJUtils.LOG_SPACER);
		log.info("PROGRAM COMPLETE");
		log.info(BioLockJUtils.LOG_SPACER);
	}
	
	
	protected static void runProgram(File propFile) throws Exception
	{
		ConfigReader cReader = new ConfigReader(propFile);
		
		List<BioLockJExecutor> list = getListToRun(cReader);
		
		String projectDir = BioLockJUtils.requireString(cReader, ConfigReader.PATH_TO_PROJECT_DIR);
				
		BioLockJUtils.logConfigFileSettings(cReader);
		BioLockJUtils.copyPropertiesFile(propFile, projectDir);

		for( BioLockJExecutor e : list )
			e.checkDependencies();
		
		for( BioLockJExecutor e : list )
		{
			BioLockJUtils.noteStartToLogWriter(e);
			BioLockJUtils.executeAndWaitForScriptsIfAny(e);
			BioLockJUtils.noteEndToLogWriter(e);
		}
	}
	
	
	
	protected static List<BioLockJExecutor> getListToRun( ConfigReader cReader ) throws Exception
	{
		List<BioLockJExecutor> list = new ArrayList<BioLockJExecutor>();
		BufferedReader reader = new BufferedReader(new FileReader(cReader.getPropertiesFile()));
		try
		{
			int count = 0;
			BioLockJExecutor bljePrevious = null;
			for(String s = reader.readLine(); s != null; s= reader.readLine())
			{
				if (s.startsWith(BioLockJExecutor.RUN_BIOLOCK_J))
				{
					StringTokenizer sToken = new StringTokenizer(s);
					sToken.nextToken();
					if( ! sToken.hasMoreTokens())
						throw new Exception("Lines starting with " + BioLockJExecutor.RUN_BIOLOCK_J 
								+ " must be followed by a Java class that is a BioLockJExecutor");
					
					String fullClassName = sToken.nextToken();
					BioLockJExecutor blje = (BioLockJExecutor) Class.forName(fullClassName).newInstance();
					blje.setConfig(cReader);
					blje.setExecutorDir(getSimpleClassName(fullClassName), count++);
					if(bljePrevious!=null)
					{
						blje.setInputDir(bljePrevious.getOutputDir());
					}

					bljePrevious = blje;
					list.add(blje);
					if( sToken.hasMoreTokens())
						throw new Exception("Lines starting with " + BioLockJExecutor.RUN_BIOLOCK_J 
								+ " must be followed by a Java class that is a BioLockJExecutor with no parameters");
				}
			}
		}
		finally{ if (reader != null) reader.close(); }
		
		return list;
	}
	
	
	private static String getSimpleClassName(String fullPathClassName) throws Exception
	{
		StringTokenizer st = new StringTokenizer(fullPathClassName, ".");
		String token = st.nextToken();
		while(st.hasMoreTokens())
		{
			token = st.nextToken();
		}
		
		return token;
	}
	
	
}
