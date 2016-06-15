package blast;

import java.io.File;

import bioLockJ.BioJLockUtils;
import bioLockJ.BioLockJExecutor;
import utils.ConfigReader;

/*
 * Takes in FASTA_TO_SPLIT_PATH
 * Writes out to SPLIT_FASTA_DIR
 * Splits into NUMBER_CLUSTERS individual files
 */
public class BreakUpFastaSequence implements BioLockJExecutor
{
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			System.out.println("Usage " + BreakUpFastaSequence.class.getName() + " pathToPropertyFile" );
			System.exit(1);
		}
		
		File propFile = BioJLockUtils.findProperyFile(args);
		ConfigReader cReader = new ConfigReader(propFile);
		File outputDir= 
				BioJLockUtils.requireExistingDirectory( cReader, ConfigReader.SPLIT_FASTA_DIR);
		File fileToParse = 
				BioJLockUtils.requireExistingFile(cReader, ConfigReader.FASTA_TO_SPLIT_PATH);
		int numChunks = BioJLockUtils.requirePositiveInteger(cReader, ConfigReader.SPLIT_FASTA_DIR);

		File logDir = BioJLockUtils.createLogDirectory(
				outputDir, BreakUpFastaSequence.class.getSimpleName());
		
	}
	
	@Override
	public void executeProjectFile(File projectFile)
	{
		// TODO Auto-generated method stub
		
	}
}
