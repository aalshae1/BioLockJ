package bioLockJ;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import utils.ConfigReader;

/**
 * Test code with BioLockJ ./testProp
 */
public class BioLockJ
{
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			System.out.println("Usage " + BioLockJ.class.getName() + " pathToPropertiesFile.txt");
			System.exit(1);
		}
		
		File propFile = new File(args[0]);
		
		if( ! propFile.exists() || propFile.isDirectory())
			throw new Exception(propFile.getAbsolutePath() + " is not a valid file");
		
		ConfigReader cReader = new ConfigReader(propFile);
		List<BioLockJExecutor> list = getListToRun(propFile);
		File logDirectory = BioLockJUtils.createLogDirectory(propFile.getName());
		BioLockJUtils.copyPropertiesFile(propFile, logDirectory);
		
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(
					new File(logDirectory + File.separator + "log.txt")));
		
		for( BioLockJExecutor e : list)
			e.checkDependencies(cReader);
		
		for( BioLockJExecutor e : list)
		{
			BioLockJUtils.noteStartToLogWriter(logWriter, e);
			
			try
			{
				BioLockJUtils.executeAndWaitForScriptsIfAny(cReader, e, logWriter);
			}
			catch(Exception ex)
			{
				BioLockJUtils.logAndRethrow(logWriter, ex);
			}
			
			BioLockJUtils.noteEndToLogWriter(logWriter, e);
			BioLockJUtils.appendSuccessToPropertyFile(propFile, e.getClass().getName(), logDirectory);
			BioLockJUtils.copyPropertiesFile(propFile, logDirectory);
		}
		
		logWriter.write("All ran " + new Date().toString());
		logWriter.flush(); logWriter.close();
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
