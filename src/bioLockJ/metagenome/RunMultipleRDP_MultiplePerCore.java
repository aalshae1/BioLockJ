package bioLockJ.metagenome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
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
		int numJobsPerCore = BioLockJUtils.requirePositiveInteger(getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE);

		String[] files = fastaInDir.list();
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(getRunAllFile()));
		
		int countNum = 0;
		int numToDo = numJobsPerCore;
		File runFile = createSubScript(allWriter, countNum++);

		BufferedWriter aWriter = new BufferedWriter(new FileWriter(runFile));
		
		for(String s : files)
		{
			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			File rdpOutFile = new File(getOutputDir().getAbsolutePath() + File.separator + 
					s  + "toRDP.txt");
			
			aWriter.write("java -jar "  + rdpBinary.getAbsolutePath() + " " +  
					"-o \"" + rdpOutFile.getAbsolutePath()  + "\" -q \"" + fastaFile + "\"\n" );
			
			aWriter.write("gzip \"" + rdpOutFile.getAbsolutePath() + "\" \n");
			
			if( --numToDo == 0 )
			{
				numToDo = numJobsPerCore;
				BioLockJUtils.closeSubScript(aWriter, runFile);
				runFile = createSubScript(allWriter, countNum++);
				aWriter = new BufferedWriter(new FileWriter(runFile));
			}
		}

		BioLockJUtils.closeSubScript(aWriter, runFile);
		BioLockJUtils.closeSubScript(allWriter, getRunAllFile());
	}
}
