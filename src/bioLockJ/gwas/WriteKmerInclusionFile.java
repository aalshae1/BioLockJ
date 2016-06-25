package bioLockJ.gwas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

public class WriteKmerInclusionFile extends BioLockJExecutor
{

	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{	
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.DSK_OUTPUT_DIRECTORY);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.GENOME_TO_INTEGER_FILE);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.KMER_TO_HAS_GENOME_FILE);		
	}
	
	// as a side effect writes a two column text file to outFilePath
	private HashMap<String, Integer> getNameToIntegerMap( File inDirectory, File outFilePath) throws Exception 
	{
		HashMap<String, Integer> map = new HashMap<String,Integer>();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFilePath));
		
		String[] names = inDirectory.list();
		
		int index =0;
		for(String s : names)
		{
			if( s.endsWith(".txt"))
			{
				String key = s.replace("_dsk.txt", "");
				writer.write(key + "\t" + index);
				map.put(key, index);
				index++;
			}
		}
		
		writer.flush();  writer.close();
		return map;
		
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader, BufferedWriter logWriter) throws Exception
	{
		File genomeToIndexFile = BioLockJUtils.requireExistingFile(cReader, ConfigReader.GENOME_TO_INTEGER_FILE);
	}
}