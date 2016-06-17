package bioLockJ;

import java.io.BufferedWriter;
import java.io.File;
import java.util.List;

import utils.ConfigReader;

public abstract class BioLockJExecutor
{
	public abstract void executeProjectFile(ConfigReader cReader, BufferedWriter logWriter) 
				throws Exception;
	public static final String FINISHED_SUFFIX= "_succesfullyFinished";
	public static final String RUN_BIOLOCK_J = "#RUN_BIOLOCK_J";
	
	public boolean poll()
	{
		return true;
	}
	public List<File> getScriptFiles() { return null; } 
	public File getRunAllFile() { return null; } 
	
	public abstract void checkDependencies(ConfigReader cReader) throws Exception;
	
}
