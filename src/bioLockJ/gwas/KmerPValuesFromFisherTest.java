package bioLockJ.gwas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;
import utils.FisherTest;

public class KmerPValuesFromFisherTest extends BioLockJExecutor
{
	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.KMER_TO_HAS_GENOME_FILE);
		BioLockJUtils.requirePositiveInteger( cReader, ConfigReader.MIN_NUMBER_OF_DIFFERENT_KMERS);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.STRAIN_METADATA_FILE);
		BioLockJUtils.requireString(cReader, ConfigReader.FISHER_CONDITION_1);
		BioLockJUtils.requireString(cReader, ConfigReader.FISHER_CONDITION_2);
		BioLockJUtils.requireString(cReader, ConfigReader.GENOME_TO_INTEGER_FILE);
		BioLockJUtils.requireString(cReader, ConfigReader.FISHER_PVALUES_OUTPUT_FILE );
	}
	
	private static HashMap<Integer, String> getIntegerToGenomeMap(
			File genomeToIntegerFile) throws Exception
	{
		HashMap<Integer, String> map = new HashMap<Integer,String>();
		
		BufferedReader reader = new BufferedReader(new FileReader(genomeToIntegerFile
				));
		
		for(String s=  reader.readLine(); s != null; s = reader.readLine())
		{
			String[] splits =s.split("\t");
			
			if( splits.length != 2)
				throw new Exception("Parsing error");
			
			int key= Integer.parseInt(splits[1]);
			
			if( map.containsKey(key))
				throw new Exception("Parsing error");
			
			map.put(key, splits[0]);
		}
		
		reader.close();
		
		return map;
	}
	
	private HashMap<String, String> getMetaMap(File metaFile) throws Exception
	{
		HashMap<String, String> map = new HashMap<String,String>();
		
		BufferedReader reader = new BufferedReader(new FileReader(metaFile));
		
		for(String s=  reader.readLine(); s != null; s = reader.readLine())
		{
			String[] splits =s.split("\t");
			
			if( splits.length != 2)
				throw new Exception("Parsing error");
			
			if( map.containsKey(splits[0]))
				throw new Exception("Parsing error");
			
			map.put(splits[0], splits[1]);
		}
		
		reader.close();
		
		return map;
	}
	
	private static HashMap<Integer, String> getIntegerToMetaMap(
			HashMap<Integer,String> genomeToIntegerMap,HashMap<String, String> metaMap ) throws Exception
	{
		HashMap<Integer, String> integerToMetaMap = new HashMap<Integer,String>();
		
		for( int i : genomeToIntegerMap.keySet())
		{
			String meta = metaMap.get(genomeToIntegerMap.get(i));
			
			if( meta == null)
				throw new Exception("Could not find meta for " + i + " " + genomeToIntegerMap.get(i));
			
			integerToMetaMap.put(i, meta);
		}
		
		return integerToMetaMap;
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader, BufferedWriter logWriter) throws Exception
	{
		File inKmerFile =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.KMER_TO_HAS_GENOME_FILE);
		File genomeToIntegerFile = BioLockJUtils.requireExistingFile(cReader, ConfigReader.GENOME_TO_INTEGER_FILE);
		int minKmerNumber =  BioLockJUtils.requirePositiveInteger( cReader, ConfigReader.MIN_NUMBER_OF_DIFFERENT_KMERS);
		File strainMetadataFile =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.STRAIN_METADATA_FILE);
		String condition1 =  BioLockJUtils.requireString(cReader, ConfigReader.FISHER_CONDITION_1);
		String condition2 = BioLockJUtils.requireString(cReader, ConfigReader.FISHER_CONDITION_2);
		File outFile = new File( BioLockJUtils.requireString(cReader, ConfigReader.FISHER_PVALUES_OUTPUT_FILE ));
		
		HashMap<Integer,String> genomeToIntegerMap = getIntegerToGenomeMap(genomeToIntegerFile);
		HashMap<String, String> metaMap = getMetaMap(strainMetadataFile);
		HashMap<Integer, String> integerToMetaMap = getIntegerToMetaMap(genomeToIntegerMap, metaMap);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		writer.write("kmer\tnumCondition1WithKmer\tnumCondition1WithoutKmer\t" + 
				"numCondition2WithKmer\tnumCondition2WithoutKmer\tpValue\n");		
		BufferedReader reader = new BufferedReader(new FileReader(inKmerFile.getAbsolutePath() + "_filteredTo_" 
				+  minKmerNumber+ ".txt"));
		
		for(String s = reader.readLine(); s!=null; s = reader.readLine())
		{
			String[] splits = s.split("\t");
			
			if( splits.length != 2)
				throw new Exception("Parsing error");
			
			int numCondition1WithKmer = 0;
			int numCondition1WithoutKmer =0;
			int numCondition2WithKmer = 0;
			int numCondition2WithoutKmer =0;
			
			String db = splits[1]; 
			
			if( db.length() != genomeToIntegerMap.size())
				throw new Exception("Parsing error");
	
			for( int x=0; x < db.length(); x++)
			{
				String meta = integerToMetaMap.get(x);

				char kmerIsPresent = db.charAt(x);
				
				if( meta ==  null)
					throw new Exception("Logic error");
				
				if( meta.equals(condition1))
				{
					if( kmerIsPresent == '1')
						numCondition1WithKmer++;
					else if( kmerIsPresent == '0')
						numCondition1WithoutKmer++;
					else throw new Exception("Parsing error");
				}
				else if ( meta.equals(condition2))
				{
					if( kmerIsPresent == '1')
						numCondition2WithKmer++;
					else if( kmerIsPresent == '0')
						numCondition2WithoutKmer++;
					else throw new Exception("Parsing error");
				}
			}
			
			if( numCondition1WithKmer + numCondition1WithoutKmer == 0 )
				throw new Exception("Could not find condition 1");
			
			if( numCondition2WithKmer + numCondition2WithoutKmer == 0 )
				throw new Exception("Could not find condition 2");
			
			writer.write(splits[0] + "\t");
			writer.write(numCondition1WithKmer + "\t");
			writer.write(numCondition1WithoutKmer + "\t");
			writer.write(numCondition2WithKmer + "\t");
			writer.write(numCondition2WithoutKmer + "\t");
			
			int bigN = numCondition1WithKmer + numCondition1WithoutKmer + numCondition2WithKmer + 
								numCondition2WithoutKmer;
			
			int bigK = numCondition1WithKmer + numCondition2WithKmer;
			
			int littleN = numCondition1WithKmer + numCondition1WithoutKmer;
			
			int litteK= numCondition1WithKmer;
			
			writer.write( FisherTest.getFisherPSum(bigN, bigK, littleN, litteK) + "\n" );
			writer.flush();
		}
		
		reader.close();
		writer.flush(); writer.close();
		
	}
}
