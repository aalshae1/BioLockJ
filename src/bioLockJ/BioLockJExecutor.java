package bioLockJ;

import java.io.File;
import java.util.List;

public abstract class BioLockJExecutor
{
	public abstract void executeProjectFile(File projectFile) throws Exception;
	
	public boolean poll()
	{
		return true;
	}
	public List<File> getScriptFiles() { return null; } 
	public File getRunAllFile() { return null; } 
	
}
