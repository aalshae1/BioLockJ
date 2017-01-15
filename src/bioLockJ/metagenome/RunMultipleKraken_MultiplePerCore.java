package bioLockJ.metagenome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

/**
 * 
 * Use this to have multiple kraken jobs per core
 */
public class RunMultipleKraken_MultiplePerCore extends BioLockJExecutor
{
	
	
	@Override
	public void checkDependencies() throws Exception
	{	
		BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_BINARY);
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_DATABASE);
		BioLockJUtils.requirePositiveInteger(getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE);
	}
	
	
	@Override
	public void executeProjectFile() throws Exception
	{
	 	File fastaInDir =  BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.PATH_TO_INPUT_RDP_FASTA_DIRECTORY);
		File krakenBinary =  BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_BINARY);
		File krakenDatabase = BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.PATH_TO_KRAKEN_DATABASE);
		int numJobsPerCore = BioLockJUtils.requirePositiveInteger(getConfig(), ConfigReader.NUMBER_OF_JOBS_PER_CORE);


		String[] files = fastaInDir.list();
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(getRunAllFile()));
		allWriter.write(krakenBinary.getAbsolutePath() + " --version");
		int countNum = 0;
		int numToDo = numJobsPerCore;

		File runFile = makeNewRunFile(allWriter, countNum++);
		
		BufferedWriter aWriter = new BufferedWriter(new FileWriter(runFile));
		
		for(String s : files)
		{
			File fastaFile = new File(fastaInDir.getAbsolutePath() + File.separator + s);
			
			String krakenOutput = getOutputDir().getAbsolutePath() + File.separator + s + "toKraken.txt";
			String krakenTranslate = getOutputDir().getAbsolutePath() + File.separator + s + "toKrakenTranslate.txt";
			
			aWriter.write(krakenBinary.getAbsolutePath() + " --db " +  krakenDatabase.getAbsolutePath()  + 
					" --output " + krakenOutput + " " +  fastaFile + "\n" );
			aWriter.write("if [ $? –eq 0 ]; then \n" );
			aWriter.write("    " + krakenBinary.getAbsolutePath() + "-translate " + " --db " +  
					krakenDatabase.getAbsolutePath()  + " " + krakenOutput + " > " + krakenTranslate + "\n" );
			aWriter.write("else touch " + getOutputDir().getAbsolutePath() + File.separator + s + FAILED_TO_PROCESS + " fi \n" );
					
			if( --numToDo == 0 )
			{
				numToDo = numJobsPerCore;
				BioLockJUtils.closeRunFile(aWriter, runFile);
				runFile = makeNewRunFile(allWriter, countNum++);
				aWriter = new BufferedWriter(new FileWriter(runFile));
			}
		}

		BioLockJUtils.closeRunFile(aWriter, runFile);
		BioLockJUtils.closeRunFile(allWriter, getRunAllFile());
	}
}