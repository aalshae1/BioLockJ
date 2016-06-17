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
		File logDirectory = BioJLockUtils.createLogDirectory(propFile.getName());
		BioJLockUtils.copyPropertiesFile(propFile, logDirectory);
		
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(
					new File(logDirectory + File.separator + 
				"log.txt")));
		
		
		for( BioLockJExecutor e : list)
		{
			BioJLockUtils.noteStartToLogWriter(logWriter, e);
			
			try
			{
				BioJLockUtils.executeAndWaitForScriptsIfAny(cReader, e, logWriter);
			}
			catch(Exception ex)
			{
				BioJLockUtils.logAndRethrow(logWriter, ex);
			}
			
			BioJLockUtils.noteEndToLogWriter(logWriter, e);
			BioJLockUtils.appendSuccessToPropertyFile(propFile, e.getClass().getName(), logDirectory);
			BioJLockUtils.copyPropertiesFile(propFile, logDirectory);
		}
		
		logWriter.write("All ran " + new Date().toString());
		logWriter.flush(); logWriter.close();
	}
	
	private static List<BioLockJExecutor> getListToRun( File propFile ) throws Exception
	{
		List<BioLockJExecutor> list = new ArrayList<BioLockJExecutor>();
		BufferedReader reader =new BufferedReader(new FileReader(propFile));
		
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
		
		return list;
		
	}
}
