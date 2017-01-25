package dskWrapper;

import java.io.File;
import java.util.ArrayList;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.ScriptBuilder;

/* 
 * for now, just uses the default kmer size of 31
 */
public class RunDSKOnDirectory extends BioLockJExecutor
{

	@Override
	public void checkDependencies() throws Exception
	{	
		BioLockJUtils.requireString(getConfig(), ConfigReader.DSK_INPUT_DIRECTORY);
		BioLockJUtils.requireString(getConfig(), ConfigReader.DSK_BINARY_DIRECTORY);
	}
	
	@Override
	public void executeProjectFile() throws Exception
	{
		String dskBinaryPath = BioLockJUtils.requireString(getConfig(), ConfigReader.DSK_BINARY_DIRECTORY);
		File dskInputDirectory = BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.DSK_INPUT_DIRECTORY);

		String[] files = BioLockJUtils.getFilePaths(dskInputDirectory);
		log.debug("Number of valid files found: " + files.length);
		setInputDir(dskInputDirectory);
		
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for( String file : files )
		{
			String filePath = getOutputDir().getAbsolutePath() + File.separator + file + "_dsk";

			ArrayList<String> lines = new ArrayList<String>(2);
			lines.add(dskBinaryPath + "/dsk -file " + filePath + " -out " + filePath + " -abundance-min 1");
			lines.add(dskBinaryPath + "/dsk2ascii -file " + filePath + " -out " + filePath + ".txt");
			data.add(lines);
		}
		
		ScriptBuilder.buildScripts(this, data);
	}
}
