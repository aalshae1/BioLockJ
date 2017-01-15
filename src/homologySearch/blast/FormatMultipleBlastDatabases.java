package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import bioLockJ.BioLockJUtils;
import bioLockJ.BioLockJExecutor;
import utils.ConfigReader;

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
		String blastBinDin = BioLockJUtils.requireString(getConfig(), ConfigReader.BLAST_BINARY_DIR);
		File fastaDirToFormat = BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.FASTA_DIR_TO_FORMAT);
		int index = 0;
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(getRunAllFile()));
		
		String[] filesToFormat = fastaDirToFormat.list();
		
		for( String s : filesToFormat)
		{
			File fastaFile = new File(fastaDirToFormat.getAbsolutePath() + File.separator + s);
			
			if( !fastaFile.isDirectory() )
			{
				
				File script = makeNewRunFile(allWriter, index++);
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(script));
				
				String prelimString = getConfig().getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
				
				if( prelimString != null)
					writer.write(prelimString + "\n");
				
				writer.write(blastBinDin + "/makeblastdb -dbtype nucl " + 
								"-in " + fastaFile.getAbsolutePath() + "\n");
				
				BioLockJUtils.closeRunFile(writer, script);
			}
		}
		
		allWriter.flush();  allWriter.close();
	}
	
}
