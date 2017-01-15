package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import bioLockJ.BioLockJUtils;
import bioLockJ.BioLockJExecutor;
import utils.ConfigReader;

/**
 * Takes in a BLAST_QUERY_DIRECTORY that should only contain FASTA files (subdirectories are ignored)
 * Takes in a FASTA_FILE_TO_FORMAT_FOR_BLAST_DB (assuming formatted for example by FormatSingleBlastDatabase)
 * Writes multiple scripts to PATH_TO_SCRIPTS_DIR/SCRIPTS_DIR_FOR_BLAST_QUERY
 * Writes results to PATH_TO_OUTPUT_DIR
 * 
 * will issue BLAST_PRELIMINARY_STRING if defined
 * requires BLAST_BIN_DIR to be defined
 * 
 * CLUSTER_BATCH_COMMAND must be defined (e.g. qsub -q "viper" ) where viper is the name of the cluster
 */
public class MultipleQueriesToOneBlastDB extends BioLockJExecutor
{

	@Override
	public void checkDependencies() throws Exception
	{	
		BioLockJUtils.requireString(getConfig(), ConfigReader.BLAST_BINARY_DIR);
		BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.BLAST_QUERY_DIRECTORY);
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.FASTA_FILE_TO_FORMAT_FOR_BLAST_DB);
		BioLockJUtils.requireString(getConfig(), ConfigReader.BLAST_ALL_COMMAND);
	}
	
	@Override
	public void executeProjectFile() throws Exception
	{
		String blastBinDin = BioLockJUtils.requireString(getConfig(), ConfigReader.BLAST_BINARY_DIR);
		File blastQueryDir = BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.BLAST_QUERY_DIRECTORY);
		File blastDatabaseFile = BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.FASTA_FILE_TO_FORMAT_FOR_BLAST_DB);
		String blastAllCommand = BioLockJUtils.requireString(getConfig(), ConfigReader.BLAST_ALL_COMMAND);

		int index = 0;
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(getRunAllFile()));
		
		String[] filesToFormat = blastQueryDir.list();
		
		for( String s : filesToFormat)
		{
			File fastaFile = new File(blastQueryDir.getAbsolutePath() + File.separator + s);
			
			if( ! fastaFile.isDirectory())
			{
				File script = makeNewRunFile(allWriter, index++);
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(script));
				
				String prelimString = getConfig().getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
				
				if( prelimString != null)
					writer.write(prelimString + "\n");
				
				File outFile = new File( getOutputDir() + File.separator + fastaFile.getName() + "_to_" + 
							blastDatabaseFile.getName() + ".txt");
				
				writer.write(blastBinDin + "/" + blastAllCommand + " -db " + 
						blastDatabaseFile.getAbsolutePath() + " -out " + 
							outFile.getAbsolutePath() +  
							" -query " +fastaFile.getAbsolutePath() + 
							" -outfmt 6\n");
				
				BioLockJUtils.closeRunFile(writer, script);
			}
		}
		
		BioLockJUtils.closeRunFile(allWriter, getRunAllFile());
	}
	
}
