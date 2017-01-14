package bioLockJ;

import java.io.*;
import java.util.*;
import org.slf4j.*;

import utils.ConfigReader;

/**
 * Test code with BioLockJ ./testProp
 */
public class BioLockJ
{
	protected static final Logger log = LoggerFactory.getLogger(BioLockJ.class);
	
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			log.info("Usage " + BioLockJ.class.getName() + " pathToPropertiesFile.txt");
			System.exit(1);
		}
		
		File propFile = new File(args[0]);
		if( !propFile.exists() || propFile.isDirectory() )
			throw new Exception(propFile.getAbsolutePath() + " is not a valid file");
		
		ConfigReader cReader = new ConfigReader(propFile);
		List<BioLockJExecutor> list = getListToRun(propFile);
		
		String projectDir = BioLockJUtils.requireString(cReader, ConfigReader.PATH_TO_PROJECT_DIR);
				
		BioLockJUtils.logConfigFileSettings(cReader);
		BioLockJUtils.copyPropertiesFile(propFile, projectDir);

		for( BioLockJExecutor e : list)
			e.checkDependencies(cReader);
		
		for( BioLockJExecutor e : list)
		{
			BioLockJUtils.noteStartToLogWriter(e);
			
			try
			{
				BioLockJUtils.executeAndWaitForScriptsIfAny(cReader, e);
			}
			catch(Exception ex)
			{
				BioLockJUtils.logAndRethrow(ex);
			}
			
			BioLockJUtils.noteEndToLogWriter(e);
		}
		
		log.info(BioLockJUtils.LOG_SPACER);
		log.info("PROGRAM COMPLETE");
		log.info(BioLockJUtils.LOG_SPACER);
	}
	
	private static List<BioLockJExecutor> getListToRun( File propFile ) throws Exception
	{
		List<BioLockJExecutor> list = new ArrayList<BioLockJExecutor>();
		BufferedReader reader =new BufferedReader(new FileReader(propFile));
		try
		{
			for(String s = reader.readLine(); s != null; s= reader.readLine())
			{
				if (s.startsWith(BioLockJExecutor.RUN_BIOLOCK_J))
				{
					StringTokenizer sToken = new StringTokenizer(s);
					sToken.nextToken();
					
					if( ! sToken.hasMoreTokens())
						throw new Exception("Lines starting with " + BioLockJExecutor.RUN_BIOLOCK_J 
								+ " must be followed by a Java class that is a BioLockJExecutor");
					
					list.add( (BioLockJExecutor) Class.forName(sToken.nextToken()).newInstance());
					
					if( sToken.hasMoreTokens())
						throw new Exception("Lines starting with " + BioLockJExecutor.RUN_BIOLOCK_J 
								+ " must be followed by a Java class that is a BioLockJExecutor with no parameters");
				}
			}
		}
		finally{ if (reader != null) reader.close(); }
		
		return list;
	}
	
}
