package bioLockJ;

import java.io.*;
import java.util.*;

import org.slf4j.*;

import utils.ConfigReader;
import utils.ProcessWrapper;

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
			if(log==null) log = LoggerFactory.getLogger(BioLockJ.class);
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
			executeAndWaitForScriptsIfAny(e);
			BioLockJUtils.noteEndToLogWriter(e);
		}
	}
	
	
	protected static void executeAndWaitForScriptsIfAny(BioLockJExecutor invoker) throws Exception
	{
		invoker.executeProjectFile();
		if( invoker.hasScripts() )
		{
			int pollTime = 15;
			try
			{
				pollTime = BioLockJUtils.requirePositiveInteger(invoker.getConfig(), ConfigReader.POLL_TIME);
			}
			catch(Exception ex)
			{
				invoker.log.warn("Could not set " + ConfigReader.POLL_TIME + ".  Setting poll time to " + 
								pollTime +  " seconds ", ex);		
			}

			executeCHMOD_ifDefined(invoker.getConfig(), invoker.getScriptDir());
			executeFile(invoker.getRunAllFile());
			pollAndSpin(invoker, pollTime );
		}
	}
	
	
	protected static void executeCHMOD_ifDefined(ConfigReader cReader, File scriptDir)  throws Exception
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
	
	
	
	
	
	protected static void pollAndSpin(BioLockJExecutor invoker, int pollTime) throws Exception
	{
		
		boolean finished = false;
		while( !finished )
		{
			finished = poll(invoker);
			
			if( !finished )
			{
				Thread.sleep(pollTime * 1000);
			}
		}
	}
	
	protected static boolean poll(BioLockJExecutor invoker) throws Exception
	{
		List<File> scriptFiles = invoker.getScriptFiles();
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
					invoker.log.info(f.getAbsolutePath() + " not finished ");
				}
			}
		}
		
		File runAllFailed = new File(invoker.getRunAllFile().getAbsolutePath() + ScriptBuilder.SCRIPT_FAILED);
		if(runAllFailed.exists())
		{
			throw new Exception("CANCEL SCRIPT EXECUTION: ERROR IN...runAll.sh");
		}
		
		invoker.log.info("Script Status (Total=" + scriptFiles.size() + "): Success=" + numSuccess + "; Failures=" + numFailed);
		return (numSuccess + numFailed) == scriptFiles.size();
	}
	
	protected static void executeFile(File f) throws Exception
	{
		String[] cmd = new String[1];
		cmd[0] = f.getAbsolutePath();
		new ProcessWrapper(cmd);
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
					blje.setExecutorDir(BioLockJUtils.getSimpleClassName(fullClassName), count++);
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
	
	
	
}
