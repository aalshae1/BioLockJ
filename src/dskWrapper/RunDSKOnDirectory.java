package dskWrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

/* 
 * for now, just uses the default kmer size of 31
 */
public class RunDSKOnDirectory extends BioLockJExecutor
{
	private File runAllFile = null;
	private List<File> scripts = null;
	
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
	
	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{	
		BioLockJUtils.requireString(cReader, ConfigReader.DSK_INPUT_DIRECTORY);
		BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
		BioLockJUtils.requireString(cReader, ConfigReader.DSK_BINARY_DIRECTORY);
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
		this.scripts = new ArrayList<File>();
		String dskBinaryPath = BioLockJUtils.requireString(cReader, ConfigReader.DSK_BINARY_DIRECTORY);
		String clusterBatchCommand = BioLockJUtils.requireString(cReader, ConfigReader.CLUSTER_BATCH_COMMAND);
		File dskInputDirectory = BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.DSK_INPUT_DIRECTORY);
		File dskOuputDirectory = getOutputDir(cReader);
		File scriptDir = getScriptDir(cReader);
		
		int index =1;
		
		this.runAllFile = createRunAllFile(cReader, scriptDir.getAbsolutePath());

		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(this.runAllFile));
		
		String[] filesToRun= dskInputDirectory.list();
		
		for( String s : filesToRun)
		{
			File fastaFile = new File(dskInputDirectory.getAbsolutePath() + File.separator + s);
			
			if( ! fastaFile.isDirectory())
			{
				File script = new File(
						scriptDir.getAbsolutePath() + File.separator + "run_" + index + "_" +
								getTimeStamp(cReader) + "_.sh");
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(script));
				
				File outFile = new File( dskOuputDirectory+ File.separator + fastaFile.getName() + "_dsk");
				
				writer.write(dskBinaryPath + "/dsk -file " + 
						fastaFile.getAbsolutePath() + " -out " + 
							outFile.getAbsolutePath() +  
							" -abundance-min 1\n");
				
				writer.write(dskBinaryPath + "/dsk2ascii  -file " + 
						outFile.getAbsolutePath() + " -out " + 
							outFile.getAbsolutePath() + ".txt\n" );
				
				File touchFile = new File(script.getAbsolutePath() + FINISHED_SUFFIX );
				
				if( touchFile.exists())
					touchFile.delete();
				
				writer.write("touch " + touchFile.getAbsolutePath() + "\n");
				
				writer.flush();  writer.close();
				this.scripts.add(script);
				
				allWriter.write(clusterBatchCommand + " " + script.getAbsolutePath() + "\n");
				allWriter.flush();
				
				index++;
			}
		}
		
		allWriter.flush();  allWriter.close();
	}
	
	
}
