package bioLockJ.gwas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;

public class FilterOutRareKmers extends BioLockJExecutor
{
	@Override
	public void checkDependencies() throws Exception
	{
		BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.KMER_TO_HAS_GENOME_FILE);
		BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.MIN_NUMBER_OF_DIFFERENT_KMERS);
	}
	
	@Override
	public void executeProjectFile() throws Exception
	{
		File inFile = BioLockJUtils.requireExistingFile(getConfig(), ConfigReader.KMER_TO_HAS_GENOME_FILE);
		
		int minNumber = BioLockJUtils.requirePositiveInteger( getConfig(), ConfigReader.MIN_NUMBER_OF_DIFFERENT_KMERS);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				inFile.getAbsolutePath() + "_filteredTo_" +  minNumber + ".txt")));
		
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		
		for( String s = reader.readLine(); s != null; s = reader.readLine())
		{
			String[] splits = s.split("\t");
			
			if( splits.length != 2)
				throw new Exception("Parsing error");
			
			String counts = splits[1];
			
			int numOnes = 0;
			
			for( int x=0; x < counts.length(); x++)
			{
				if( counts.charAt(x) == '1')
					numOnes++;
			}
			
			if( numOnes >= minNumber && numOnes <= (counts.length() - minNumber))
				writer.write(s + "\n");
		}
		
		reader.close();
		writer.flush();  writer.close();
	}
	
}
