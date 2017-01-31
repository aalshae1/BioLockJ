package bioLockJ;

import java.io.*;
import java.util.*;

import org.slf4j.*;

/** 
 * To run BioLockJ program, from project root directory ($BLJ) run:
 * 
 *  java -cp $BLJ/lib/*:$BLJ/bin bioLockJ.BioLockJ $BLJ/resources/allMiniKraken/krakenAdenonas2015	
 *  java -cp $BLJ/lib/*:$BLJ/bin bioLockJ.BioLockJ ./resources/somePropFile.prop
 *  
 *  BioLockJ is designed to run on any platform.  
 *  Each time BioLockJ runs, a new project specific directory is created in ./projects. 
 *  PROJECT_NAME is read from propFile (required).  
 *  NUMBER_OF_JOBS_PER_CORE = # commands/subscript for cluster compute node (optional).
 *  
 *  Project Structure
 *  -----------------------------------------------------------------------------------
 *  ./projects
 *  	> PROJECT_NAME_%timestamp% (PROJECT_NAME = required property)
 *  		> 00_#RUN_BIOLOCK_J<BioLockJExecutor> (#RUN_BIOLOCK_J = required property)
 *  			> input - if COPY_INPUT_FLAG=TRUE (optional property)
 *  			> output 
 *  			> scripts 
 *  				-runAll.sh (calls all run_###.sh scripts)
 *  				-run_000.sh (at least one numbered subscript is created)
 *  				-run_XXX.sh (as many as required byas per NUMBER_OF_JOBS_PER_CORE)
 *  		> 01_#RUN_BIOLOCK_J<BioLockJExecutor> (additional #RUN_BIOLOCK_J = optional)
 * 				* No input directory, uses 00_#RUN_BIOLOCK_J/output as input.
 * 				> output
 * 				> scripts
 * 			> XX_#RUN_BIOLOCK_J<BioLockJExecutor> (additional #RUN_BIOLOCK_J = optional)
 */
public class BioLockJ
{
	// wait to initialize until after ConfigReader names log file.
	protected static Logger log;  

	
	/**
	 * The main method is the first method called when BioLockJ is run.  Here we
	 * read property file, copy it to project directory, initialize ConfigReader 
	 * and call runProgram(cReader).
	 * 
	 * @param args - args[0] path to property file
	 */
	public static void main(String[] args)
	{
		System.out.println("START PROGRAM");
		try{
			if( args.length != 1)
			{
				System.out.println("Usage " + BioLockJ.class.getName() + " <FULL PATH TO PROP FILE>");
				System.out.println("TERMINATE PROGRAM");
				System.exit(1);
			}
			
			File propFile = new File(args[0]);
			if( !propFile.exists() || propFile.isDirectory() )
				throw new Exception(propFile.getAbsolutePath() + " is not a valid file");

			ConfigReader cReader = new ConfigReader(propFile);
			if(log == null) log = LoggerFactory.getLogger(BioLockJ.class);
			String projectDir = BioLockJUtils.requireString(cReader, ConfigReader.PATH_TO_PROJECT_DIR);
			BioLockJUtils.logConfigFileSettings(cReader);
			BioLockJUtils.copyPropertiesFile(propFile, projectDir);

			runProgram(cReader);

		}catch(Exception ex){
			log.error(ex.getMessage(), ex);
		}finally{
			if(log!=null){
				log.info(BioLockJUtils.LOG_SPACER);
				log.info("PROGRAM COMPLETE");
				log.info(BioLockJUtils.LOG_SPACER);
			}else{
				System.out.println("TERMINATE PROGRAM");
			}
		}
	}
	
	
	protected static void runProgram(ConfigReader cReader) throws Exception
	{
		List<BioLockJExecutor> list = getListToRun(cReader);
		
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
				BioLockJExecutor.log.warn("Could not set " + ConfigReader.POLL_TIME + ".  Setting poll time to " + 
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
			File[] listOfFiles = scriptDir.listFiles();
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
					log.info(f.getAbsolutePath() + " not finished ");
				}
			}
		}
		
		File runAllFailed = new File(invoker.getRunAllFile().getAbsolutePath() + ScriptBuilder.SCRIPT_FAILED);
		if(runAllFailed.exists())
		{
			throw new Exception("CANCEL SCRIPT EXECUTION: ERROR IN...runAll.sh");
		}
		
		log.info("Script Status (Total=" + scriptFiles.size() + "): Success=" + numSuccess + "; Failure=" + numFailed);
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
				if (s.startsWith(ScriptBuilder.RUN_BIOLOCK_J))
				{
					StringTokenizer sToken = new StringTokenizer(s);
					sToken.nextToken();
					if( ! sToken.hasMoreTokens())
						throw new Exception("Lines starting with " + ScriptBuilder.RUN_BIOLOCK_J 
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
						throw new Exception("Lines starting with " + ScriptBuilder.RUN_BIOLOCK_J 
								+ " must be followed by a Java class that is a BioLockJExecutor with no parameters");
				}
			}
		}
		finally{ if (reader != null) reader.close(); }
		
		return list;
	}
	
	private static String[] getArgs(String command, String filePath) 
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
