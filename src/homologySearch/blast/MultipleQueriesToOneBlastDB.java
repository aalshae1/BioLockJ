package homologySearch.blast;

import java.io.File;
import java.util.ArrayList;

import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.ScriptBuilder;
import bioLockJ.BioLockJExecutor;

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
		
		String prelimString = getConfig().getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
		
		String[] files = BioLockJUtils.getFilePaths(blastQueryDir);
		log.debug("Number of valid  files found: " + files.length);
		setInputDir(blastQueryDir);
		
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for( String file : files )
		{	
			ArrayList<String> lines = new ArrayList<String>();
			if( prelimString != null ) 
				lines.add(prelimString);
			
			String outFile = getOutputDir() + File.separator + file + "_to_" + 
					blastDatabaseFile.getName() + ".txt";
			
			lines.add(blastBinDin + "/" + blastAllCommand + " -db " + 
					blastDatabaseFile.getAbsolutePath() + " -out " + 
						outFile + " -query " + file + " -outfmt 6");
		
			data.add(lines);
		}
		
		ScriptBuilder.buildScripts(this, data);
	}
}
