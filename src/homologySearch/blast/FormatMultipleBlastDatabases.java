package homologySearch.blast;

import java.io.File;
import java.util.ArrayList;

import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.ScriptBuilder;
import bioLockJ.BioLockJExecutor;

public class FormatMultipleBlastDatabases extends BioLockJExecutor
{
	/*
	 * Takes in FASTA_DIR_TO_FORMAT which should only contain fasta files
	 * (sub-directories are allowed but will be ignored)
	 * 
	 * Writes scripts to SCRIPTS_DIR/SCRIPTS_DIR_FOR_BLAST_FORMAT
	 * 
	 * will issue BLAST_PRELIMINARY_STRING if defined
	 * requires BLAST_BIN_DIR to be defined
	 * 
	 * CLUSTER_BATCH_COMMAND must be defined (e.g. qsub -q "viper" ) where viper is the name of the cluster
	 */

	@Override
	public void checkDependencies() throws Exception
	{
		BioLockJUtils.requireString(getConfig(), ConfigReader.BLAST_BINARY_DIR);
		BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.FASTA_DIR_TO_FORMAT);
	}
	
	@Override
	public void executeProjectFile() throws Exception
	{
		String blastBinDir = BioLockJUtils.requireString(getConfig(), ConfigReader.BLAST_BINARY_DIR);
		File fastaDirToFormat = BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.FASTA_DIR_TO_FORMAT);

		String prelimString = getConfig().getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
		
		String[] files = BioLockJUtils.getFilePaths(fastaDirToFormat);
		log.debug("Number of valid  files found: " + files.length);
		setInputDir(fastaDirToFormat);
		
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		for( String file : files )
		{
			ArrayList<String> lines = new ArrayList<String>();
			if( prelimString != null )
				lines.add(prelimString);

			lines.add(blastBinDir + "/makeblastdb -dbtype nucl -in " + file);
			data.add(lines);
		}
		
		ScriptBuilder.buildScripts(this, data);
	}
	
}
