package bioLockJ.gwas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.StringTokenizer;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import bioLockJ.ConfigReader;
import bitManipulations.Encode;

public class WriteKmerInclusionFile extends BioLockJExecutor
{
	public static final int KMER_SIZE = 31;
	public static final String SUFFIX_TO_REMOVE = "_dsk.txt";

	@Override
	public void checkDependencies() throws Exception
	{	
		BioLockJUtils.requireString(getConfig(), ConfigReader.GENOME_TO_INTEGER_FILE);
		BioLockJUtils.requireString(getConfig(), ConfigReader.KMER_TO_HAS_GENOME_FILE);		
	}
	
	private HashMap<Long, BitSet> getBigBitSet(File inDirectory, 
			HashMap<String, Integer> nameMap) throws Exception
	{
		long startTime = System.currentTimeMillis();
		
		HashMap<Long, BitSet> bigMap = new HashMap<Long,BitSet>();

		String[] names = inDirectory.list();
		
		int index =0;
		
		for(String s : names)
		{
			log.info(index + " of " + names.length + " "+  
					"Starting " + s + " at " + (System.currentTimeMillis() - startTime)/1000f 
								+ " with " + Runtime.getRuntime().freeMemory() + " free \n");
			
			index++;
			
			if( s.endsWith(".txt"))
			{
				Integer keyValue = nameMap.get(s.replaceAll(SUFFIX_TO_REMOVE, ""));
				
				if(keyValue == null)
					throw new Exception("Could not find " + s.replaceAll(SUFFIX_TO_REMOVE, ""));
				
				File inFile = new File(inDirectory.getAbsolutePath() + File.separator + s);
				
				BufferedReader reader = new BufferedReader(new FileReader(inFile));
				try{
					for(String s2= reader.readLine(); s2 != null ; s2 = reader.readLine())
					{
						StringTokenizer sToken = new StringTokenizer(s2);
						
						String kmer = sToken.nextToken();
						
						if( kmer.length() != KMER_SIZE)
							throw new Exception("Initial implementation is for k=31 only \n" + 
									"Parsing error " + inFile.getAbsolutePath() + " " +s);
						
						long aVal = Encode.makeLong(kmer);
						
						sToken.nextToken();
						
						if( sToken.hasMoreTokens())
							throw new Exception("Parsing error " + inFile.getAbsolutePath() + " " +s2);
						
						BitSet bSet = bigMap.get(aVal);
						
						if( bSet == null)
						{
							bSet = new BitSet(nameMap.size());
							bigMap.put(aVal, bSet);
						}
						
						bSet.set(keyValue);
						
					}
				}finally{ reader.close(); }
			}
		}
		
		return bigMap;
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
				String key = s.replace(SUFFIX_TO_REMOVE, "");
				writer.write(key + "\t" + index + "\n");
				map.put(key, index);
				index++;
			}
		}
		
		writer.flush();  writer.close();
		return map;
		
	}
	
	private static String getAsBitString(BitSet set, int length) throws Exception
	{
		StringBuffer buff =  new StringBuffer();
		
		for(int x=0; x < length; x++)
		{
			if( set.get(x))
				buff.append("1");
			else
				buff.append("0");
		}
		
		return buff.toString();
	}
	
	private static void writeResults(File outFile, HashMap<Long, BitSet> bigMap,
						HashMap<String, Integer> nameMap) 
		throws Exception
	{
		BufferedWriter writer =new BufferedWriter(new FileWriter(outFile));
		
		for( Long l : bigMap.keySet())
		{
			BitSet set = bigMap.get(l);
			
			writer.write(l + "\t" + getAsBitString(set, nameMap.size()) + "\n");
		}
		
		writer.flush();  writer.close();
	}
	@Override
	public void executeProjectFile() throws Exception
	{
		File genomeToIndexFile = new File(
				BioLockJUtils.requireString(getConfig(), ConfigReader.GENOME_TO_INTEGER_FILE));
		File kmerToGenomeFile = new File(
				BioLockJUtils.requireString(getConfig(), ConfigReader.KMER_TO_HAS_GENOME_FILE));	
		File dirToParse = getOutputDir();
		
		HashMap<String, Integer> nameMap= getNameToIntegerMap(dirToParse, genomeToIndexFile);
		HashMap<Long, BitSet> bigMap = getBigBitSet(dirToParse, nameMap);
		writeResults(kmerToGenomeFile, bigMap, nameMap);
	}
}