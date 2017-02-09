package homologySearch.blast;

import java.io.File;
import java.util.ArrayList;

import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bioLockJ.ScriptBuilder;
import bioLockJ.BioLockJExecutor;

public class FormatSingleBlastDatabase extends BioLockJExecutor
{
	/**
	 * Takes in FASTA_FILE_TO_FORMAT_FOR_BLAST_DB
	 * Writes a single script to SCRIPTS_DIR/SCRIPTS_DIR_FOR_BLAST_FORMAT
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
		BioLockJUtils.requireExistingFile(
						getConfig(), ConfigReader.FASTA_FILE_TO_FORMAT_FOR_BLAST_DB);
		BioLockJUtils.requireDBType(getConfig());
	}
	
	@Override
	public void executeProjectFile() throws Exception
	{
		String blastBinDin = BioLockJUtils.requireString(getConfig(), ConfigReader.BLAST_BINARY_DIR);
		File fastaFileToFormat= BioLockJUtils.requireExistingFile(
						getConfig(), ConfigReader.FASTA_FILE_TO_FORMAT_FOR_BLAST_DB);
		
		String dbType = BioLockJUtils.requireDBType(getConfig());
		
		//String[] files = BioLockJUtils.getFilePaths(fastaFileToFormat);
		//log.debug("Number of valid  files found: " + files.length);
		//setInputDir(fastaFileToFormat);
		
		String prelimString = getConfig().getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
		
		ArrayList<String> lines = new ArrayList<String>();
		if( prelimString != null )
			lines.add(prelimString);
	
		lines.add(blastBinDin + "/makeblastdb -dbtype " + dbType +  
								" -in " + fastaFileToFormat.getAbsolutePath());
				
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		data.add(lines);
		
		String[] files = new String[1];
		files[0] = fastaFileToFormat.getName();
		
		ScriptBuilder.buildScripts(this, data, files);
	}

}
