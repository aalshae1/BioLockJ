package bioLockJ.metagenome;

import java.io.File;
import java.util.ArrayList;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ScriptBuilder;
import utils.ConfigReader;

/**
 * 
 * Use this to have multiple RDP jobs per core
 */
public class RunMultipleRDP_MultiplePerCore extends BioLockJExecutor
{
	
	@Override
	public void checkDependencies() throws Exception
	{	
		BioLockJUtils.requireString(getConfig(), ConfigReader.CLUSTER_BATCH_COMMAND);
		BioLockJUtils.requireString(getConfig(), ConfigReader.CLUSTER_PARAMS);
		BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_RDP_JAR);
		BioLockJUtils.requirePositiveInteger(getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE);
	}
	
	
	@Override
	public void executeProjectFile() throws Exception
	{
	 	File fastaInDir =  BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File rdpBinary =  BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_RDP_JAR);

		String[] files = BioLockJUtils.getFilePaths(fastaInDir);
		log.debug("Number of valid  files found: " + files.length);
		setInputDir(fastaInDir);
		
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for( String file : files )
		{
			String fastaFile = fastaInDir.getAbsolutePath() + File.separator + file;
			String rdpOutFile = getOutputDir().getAbsolutePath() + File.separator + file + "toRDP.txt";
			
			String firstLine = "java -jar "  + rdpBinary.getAbsolutePath() + " " +  
					"-o \"" + rdpOutFile  + "\" -q \"" + fastaFile + "\"";
			
			String nextLine = "gzip \"" + rdpOutFile + "\"";
			
			
			ArrayList<String> lines = new ArrayList<String>(2);
			lines.add(firstLine);
			lines.add(nextLine);
			data.add(lines);

		}

		ScriptBuilder.buildScripts(this, data);
	}
}
