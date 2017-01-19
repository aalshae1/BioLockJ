package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import bioLockJ.BioLockJUtils;
import bioLockJ.BioLockJExecutor;
import utils.ConfigReader;

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
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(getRunAllFile()));
		
		File script = createSubScript(allWriter, 0);
		BufferedWriter writer = new BufferedWriter(new FileWriter(script));
				
		String prelimString = getConfig().getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
				
		if( prelimString != null)
			writer.write(prelimString + "\n");
				
		writer.write(blastBinDin + "/makeblastdb -dbtype " + dbType +  
								" -in " + fastaFileToFormat.getAbsolutePath() + "\n");
				
		BioLockJUtils.closeSubScript(writer, script);
		BioLockJUtils.closeSubScript(allWriter, getRunAllFile());
	}

}
