package bioLockJ;

import java.io.File;
import java.util.List;

import org.slf4j.*;

import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

public abstract class BioLockJExecutor
{

	public abstract void executeProjectFile(ConfigReader cReader) throws Exception;
	public abstract void checkDependencies(ConfigReader cReader) throws Exception;
	
	public static final String FINISHED_SUFFIX = "_succesfullyFinished";
	public static final String FAILED_TO_PROCESS = "_failedToProcess";
	public static final String RUN_BIOLOCK_J = "#RUN_BIOLOCK_J";
	

	protected static Logger LOG = LoggerFactory.getLogger(BioLockJExecutor.class);
	
	
	
	public boolean poll(){
		return true;
	}
	
	public List<File> getScriptFiles() { return null; } 
	
	
	public File getRunAllFile() 
	{ 
//		if (runAllFile==null)
//		{
//			
//		}
		return null; 
	} 
	
	//private File runAllFile;
	

	public static String getTimeStamp(ConfigReader cReader)
	{
		return cReader.getAProperty(ConfigReader.RUN_TIMESTAMP);
	}
	
	public static File getBLJRoot(ConfigReader cReader) throws Exception
	{
		return BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_BLJ_ROOT);
	}
	
	public static File getProjectDir(ConfigReader cReader) throws Exception
	{
		return BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_PROJECT_DIR);
	}
	
	public static File getOutputDir(ConfigReader cReader) throws Exception
	{
		return BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_OUTPUT_DIR);
	}
	
	public static File getSummaryDir(ConfigReader cReader) throws Exception
	{
		return BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_SUMMARY_DIR);
	}
	
	public static File getScriptDir(ConfigReader cReader) throws Exception
	{
		return BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_SCRIPT_DIR);
	}
	
	/**
	 * Create the RunAllFile.  If the script path value end with File.separator, trim it off.
	 *  
	 * @param cReader
	 * @param pathToScriptDir
	 * @return
	 * @throws Exception
	 */
	public static File createRunAllFile(ConfigReader cReader, String pathToScriptDir) throws Exception
	{
		if(pathToScriptDir!=null && pathToScriptDir.trim().endsWith(File.separator)){
			pathToScriptDir = pathToScriptDir.substring(0, pathToScriptDir.trim().length()-1);
		}
		
		return new File(pathToScriptDir + File.separator + "runAll_" + 
				getTimeStamp(cReader) + ".sh");
	}
	
}
