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

	@Override
	public void checkDependencies() throws Exception
	{	
		BioLockJUtils.requireString(getConfig(), ConfigReader.DSK_INPUT_DIRECTORY);
		BioLockJUtils.requireString(getConfig(), ConfigReader.DSK_BINARY_DIRECTORY);
	}
	
	@Override
	public void executeProjectFile() throws Exception
	{
		String dskBinaryPath = BioLockJUtils.requireString(getConfig(), ConfigReader.DSK_BINARY_DIRECTORY);
		File dskInputDirectory = BioLockJUtils.requireExistingDirectory(getConfig(), ConfigReader.DSK_INPUT_DIRECTORY);

		int index = 0;
		
		BufferedWriter allWriter = new BufferedWriter(new FileWriter(getRunAllFile()));
		
		
		String[] filesToRun = dskInputDirectory.list();
		
		for( String s : filesToRun)
		{
			File fastaFile = new File(dskInputDirectory.getAbsolutePath() + File.separator + s);
			
			if( ! fastaFile.isDirectory())
			{
				File script = makeNewRunFile(allWriter, index++);
				BufferedWriter writer = new BufferedWriter(new FileWriter(script));
				
				File outFile = new File(getOutputDir().getAbsolutePath() + File.separator + 
						fastaFile.getName() + "_dsk");
				
				writer.write(dskBinaryPath + "/dsk -file " + fastaFile.getAbsolutePath() + " -out " + 
						outFile.getAbsolutePath() + " -abundance-min 1\n");
				
				writer.write(dskBinaryPath + "/dsk2ascii  -file " + outFile.getAbsolutePath() + 
						" -out " + outFile.getAbsolutePath() + ".txt\n" );
				
				BioLockJUtils.closeRunFile(writer, script);
			}
		}
		
		BioLockJUtils.closeRunFile(allWriter, getRunAllFile());
	}
	
	
}
