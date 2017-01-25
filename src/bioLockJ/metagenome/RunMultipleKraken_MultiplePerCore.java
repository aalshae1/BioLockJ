package bioLockJ.metagenome;

import java.io.*;
import java.util.*;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.ScriptBuilder;

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

		String[] files = BioLockJUtils.getFilePaths(fastaInDir);
		log.debug("Number of valid  files found: " + files.length);
		setInputDir(fastaInDir);
		
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for( String file : files )
		{	
			
			String inputFile = fastaInDir.getAbsolutePath() + File.separator + file;
			String krakenOutput = getOutputDir().getAbsolutePath() + File.separator + file + "_toKraken.txt";
			String krakenTranslate = getOutputDir().getAbsolutePath() + File.separator + file + "_toKrakenTranslate.txt";

			String firstLine = krakenBinary.getAbsolutePath() + " --db " + krakenDatabase.getAbsolutePath()  + 
					" --output " + krakenOutput + " " + inputFile;
			
			String nextLine = krakenBinary.getAbsolutePath() + "-translate --db " +  
					krakenDatabase.getAbsolutePath()  + " " + krakenOutput + " > " + krakenTranslate;
			
			ArrayList<String> lines = new ArrayList<String>(2);
			lines.add(firstLine);
			lines.add(nextLine);
			data.add(lines);
		}
		
		ScriptBuilder.buildScripts(this, data);
	}
}