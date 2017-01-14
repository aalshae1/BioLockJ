package bioLockJ.metagenome;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;
import org.slf4j.*;

public class GatherKrakenResults extends BioLockJExecutor
{
	public static final String[] KRAKEN_TAXONOMY = 
			 {"domain","phylum", "class", "order", "family", "genus", "species"};
	
	public static final String THREE_COL_SUFFIX = "_SparseThreeCol.txt";
	
	@Override //as required by abstract class
	public void checkDependencies(ConfigReader cReader) throws Exception{}
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
		for( int x=0; x < KRAKEN_TAXONOMY.length; x++)
		{
			HashMap<String, HashMap<String, Integer>> map = getAllSamples(cReader, x+2);
			File summaryFile = new File(getSummaryDir(cReader) + "kraken_" + 
					KRAKEN_TAXONOMY[x] + ".txt");
			GatherRDPResults.writeResults(map, summaryFile.getAbsolutePath());
		}
	}
	
	private static HashMap<String, HashMap<String, Integer>> getAllSamples( 
			ConfigReader cReader, int parseLevel ) throws Exception
	{
		HashMap<String, HashMap<String, Integer>>  map = new HashMap<String,HashMap<String,Integer>>();
		File outputDir = getOutputDir(cReader);
		for(String s : outputDir.list())
		{
			if(s.endsWith("toKrakenTranslate.txt"))
			{
				File inFile= new File(outputDir.getAbsoluteFile() + File.separator + s);
				HashMap<String, Integer> innerMap = getCounts(inFile, parseLevel);
				
				long sum = 0;
				
				for( Integer i : innerMap.values())
					sum += i;
				
				if( sum >0 )
					map.put(inFile.getName().replace("toKrakenTranslate.txt", ""), innerMap);
			}
		}
				
		return map;
	}

	private static HashMap<String, Integer> getCounts( File inFile, int parseNum ) throws Exception
	{
		LOG.info("GatherKrakenResults.getCounts from file: " + inFile.getAbsolutePath());
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		for(String s= reader.readLine(); s != null; s= reader.readLine())
		{
			StringTokenizer sToken =new StringTokenizer(s, "\t");
			sToken.nextToken();
			
			String[] splits = sToken.nextToken().split(";");
			
			if( splits.length -1 >= parseNum)
			{
				Integer val = map.get(splits[parseNum]);
				
				if( val == null)
					val =0;
				
				val++;
				
				map.put(new String (splits[parseNum]), val );
			}
		}

		reader.close();
		
		return map;
	}
}
