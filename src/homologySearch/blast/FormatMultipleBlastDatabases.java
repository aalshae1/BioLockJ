package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import bioLockJ.BioJLockUtils;
import bioLockJ.BioLockJExecutor;
import homologySearch.BreakUpFastaSequence;
import utils.ConfigReader;

public class FormatMultipleBlastDatabases extends BioLockJExecutor
{
	/*
	 * Takes in FASTA_DIR_TO_FORMAT which should only contain fasta files
	 * (sub-directories are allowed but will be ignored)
	 * 
	 * Writes scripts to SCRIPTS_DIR_FOR_BLAST_FORMAT
	 * 
	 * will issue BLAST_PRELIMINARY_STRING if defined
	 * requires BLAST_BIN_DIR to be defined
	 * 
	 * CLUSTER_BATCH_COMMAND must be defined (e.g. qsub -q "viper" ) where viper is the name of the cluster
	 */
	private File runAllFile = null;
	private List<File> scripts = null;
	
	@Override
	public void executeProjectFile(File projectFile) throws Exception
	{
		this.scripts = new ArrayList<File>();
		ConfigReader cReader = new ConfigReader(projectFile);
		String blastBinDin = BioJLockUtils.requireString(cReader, ConfigReader.BLAST_BINARY_DIR);
		File fastaDirToFormat = BioJLockUtils.requireExistingDirectory(cReader, ConfigReader.FASTA_DIR_TO_FORMAT);
		
		File scriptDir = BioJLockUtils.requireExistingDirectory(cReader, ConfigReader.SCRIPTS_DIR_FOR_BLAST_FORMAT);
	
		String clusterBatchCommand = BioJLockUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
		
		int index =1;
		this.runAllFile = new File(scriptDir.getAbsolutePath() + File.separator + "runAll.sh");
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(this.runAllFile));
		
		String[] filesToFormat = fastaDirToFormat.list();
		
		for( String s : filesToFormat)
		{
			File fastaFile = new File(fastaDirToFormat.getAbsolutePath() + File.separator + s);
			
			if( ! fastaFile.isDirectory())
			{
				File script = new File(
						scriptDir.getAbsolutePath() + File.separator + "run_" + index + ".sh");
				BufferedWriter writer = new BufferedWriter(new FileWriter(script));
				
				String prelimString = cReader.getAProperty(ConfigReader.BLAST_PRELIMINARY_STRING);
				
				if( prelimString != null)
					writer.write(prelimString + "\n");
				
				writer.write(blastBinDin + " /makeblastdb -dbtype nucl " + 
								"-in " + fastaFile.getAbsolutePath() + "\n");
				writer.write("echo \"" + FINISHED_STRING + "\"\n");
				
				writer.flush();  writer.close();
				this.scripts.add(script);
				
				allWriter.write(clusterBatchCommand + " " + script.getAbsolutePath() + "\n");
				allWriter.flush();
				
				index++;
			}
		}
		
		allWriter.flush();  allWriter.close();
	}
	
	@Override
	public File getRunAllFile()
	{
		return runAllFile;
	}
	
	@Override
	public List<File> getScriptFiles()
	{
		return scripts;
	}
	
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			System.out.println("Usage " + BreakUpFastaSequence.class.getName() + " pathToPropertyFile" );
			System.exit(1);
		}
		
		File propFile = BioJLockUtils.findProperyFile(args);
		new BreakUpFastaSequence().executeProjectFile(propFile);
	}
	
	
}
