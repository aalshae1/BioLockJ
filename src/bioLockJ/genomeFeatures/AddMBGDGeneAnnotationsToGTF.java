package bioLockJ.genomeFeatures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;
import utils.TabReader;

public class AddMBGDGeneAnnotationsToGTF extends BioLockJExecutor
{
	protected static final Logger log = LoggerFactory.getLogger(AddMBGDGeneAnnotationsToGTF.class);
	
	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.INPUT_GTF_FILE);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.MBGD_EXTENDED_PATH);
		BioLockJUtils.requireString(cReader, ConfigReader.OUTPUT_GTF_FILE);
		BioLockJUtils.requireString(cReader, ConfigReader.BLAST_GATHERED_TOP_HITS_FILE);
	}
	
	private static HashMap<Integer, String> getLineDescriptions(File extendedFile) throws Exception
	{
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		
		BufferedReader reader = new BufferedReader(new FileReader(extendedFile));
		
		for( int x=0; x< 9; x++)
			reader.readLine();
		
		int lineNumber = 9;
		for(String s= reader.readLine();  s != null; s = reader.readLine())
		{
			TabReader tReader = new TabReader(s);
			
			for( int x=0; x < 7; x++)
				tReader.nextToken();
			
			map.put(lineNumber, tReader.nextToken());
			lineNumber++;
			
			if(lineNumber % 1000 == 0)
				log.info("Reading annotations " + lineNumber);
		}
		
		reader.close();
		return map;
	}
	
	private static HashMap<String, HashSet<Integer>>  getFileLineMap( File extendedFile,
			HashSet<String> included) throws Exception
	{
		log.info("Reading annotations...");
		HashMap<String, HashSet<Integer>> map = new HashMap<String, HashSet<Integer>>();
		
		BufferedReader reader = new BufferedReader(new FileReader( 
				extendedFile));
		
		for( int x=0; x< 9; x++)
			reader.readLine();

		int lineNumber = 9;
		for(String s= reader.readLine();  s != null; s = reader.readLine())
		{
			
			TabReader tReader =new TabReader(s);
			
			for( int x=0; x < 8; x++)
				tReader.nextToken();
			
			while(tReader.hasMore())
			{
				String next = tReader.nextToken().trim();
				
				if( next.length() >0)
				{
					StringTokenizer innerTokenizer = new StringTokenizer(next);
					
					while( innerTokenizer.hasMoreTokens())
					{
						String key = new StringTokenizer(innerTokenizer.nextToken(), "(").nextToken();
						
						if( included.contains(key))
						{

							HashSet<Integer> set = map.get(key);
							
							if( set == null)
							{
								set = new HashSet<Integer>();
								map.put(key, set);
							}
							
							set.add(lineNumber);
						}
					}
				}
			}
			
			lineNumber++;
			
			if( lineNumber % 100 == 0 )
				log.info(lineNumber + " " + map.size());
				
		}
		
		reader.close();
		return map;
	}
	
	public static void addGeneAnnotation(String inFile, String outFile, 
			HashMap<Integer, String> lineDescriptions, HashMap<String, String> geneIdToProtMap,
			HashMap<String, HashSet<Integer>> fileLineMap) 
				throws Exception
	{	
		BufferedReader reader = new BufferedReader(new FileReader( inFile));
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

		for(String s = reader.readLine(); s != null; s = reader.readLine())
		{
			String[] splits = s.split("\t");
			
			if( splits.length != 9)
				throw new Exception("Parsing error");
			
			StringTokenizer sToken = new StringTokenizer(splits[8], ";");
			
			String key = sToken.nextToken().replaceAll("\"", "").replace("gene_id ", "");
			
			//log.info("Searching " + key);
			
			String protKey = geneIdToProtMap.get(key);
			
			//log.info("found " + protKey);
			
			StringBuffer description = new StringBuffer();
			
			if( protKey != null)
			{
				HashSet<Integer> set  = fileLineMap.get(protKey);
				
				if( set != null)
					for( Integer i : set)
						description.append(lineDescriptions.get(i) + ";");
				
			}
			
			if( description.length() == 0 )
				description.append("NA");
			
			for( int x=0; x < 8; x++)
				writer.write(splits[x] + "\t");
			
			writer.write(key  + ";" + description + "\n");
		}
		
		writer.flush(); writer.close();
		
		reader.close();
	}
	
	private static HashMap<String, String> geneIDtoProtMap(File blastHitFile) throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(blastHitFile));
		reader.readLine();
		
		HashMap<String, String> map = new HashMap<String,String>();
		
		for(String s= reader.readLine(); s != null; s = reader.readLine())
		{
			String[] splits = s.split("\t");
			
			if( map.containsKey(splits[0]))
				throw new Exception("duplicate " + splits[0]);
			
			//log.info("Adding " + splits[0] + " " + splits[1]);
			map.put(splits[0], splits[1]);
		}
		
		reader.close();
		
		return map;
	}
	
	private static HashSet<String> getNeededIds( File topHitsFile) throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(topHitsFile));
		
		HashSet<String> set = new HashSet<String>();
		
		reader.readLine();
		
		for(String s= reader.readLine(); s != null; s=reader.readLine())
			set.add(s.split("\t")[1]);
		
		reader.close();
		
		return set;
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
		File inputFile =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.INPUT_GTF_FILE);
		File mbdgFile = BioLockJUtils.requireExistingFile(cReader, ConfigReader.MBGD_EXTENDED_PATH);
		File outFile = new File(BioLockJUtils.requireString(cReader, ConfigReader.OUTPUT_GTF_FILE));
		File topHitsFile= new File(
				BioLockJUtils.requireString(cReader, ConfigReader.BLAST_GATHERED_TOP_HITS_FILE));
		
		HashMap<String, String> geneIdtoProtMap = geneIDtoProtMap(topHitsFile);
		HashMap<String, HashSet<Integer>> fileLineMap = getFileLineMap(mbdgFile, 
							getNeededIds(topHitsFile));
		HashMap<Integer, String> map = getLineDescriptions(mbdgFile);
		
		addGeneAnnotation(inputFile.getAbsolutePath(), outFile.getAbsolutePath(), 
					map, geneIdtoProtMap, fileLineMap);	
	}
	
}
