package bioLockJ.genomeFeatures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import utils.ConfigReader;
import utils.TabReader;

public class AddMBGDGeneAnnotationsToGTF extends BioLockJExecutor
{
	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.INPUT_GTF_FILE);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.MBGD_EXTENDED_PATH);
		BioLockJUtils.requireString(cReader, ConfigReader.OUTPUT_GTF_FILE);
		BioLockJUtils.requireExistingFile(cReader, ConfigReader.BLAST_GATHERED_TOP_HITS_FILE);
	}
	
	public static HashMap<Integer, String> getLineDescriptions(File extendedFile) throws Exception
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
				System.out.println("Reading annotations " + lineNumber);
		}
		
		reader.close();
		return map;
	}
	
	public static HashMap<String, HashSet<Integer>>  getFileLineMap( File extendedFile ) throws Exception
	{
		System.out.println("Reading annotations...");
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
			
			lineNumber++;
			
			if( lineNumber % 1000 == 0 )
				System.out.println(lineNumber);
				
		}
		
		reader.close();
		return map;
	}
	
	public static void addGeneAnnotation(String inFile, String outFile, 
			HashMap<Integer, String> lineDescriptions, HashMap<String, String> geneIdToProtMap) 
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
			sToken.nextToken();
			
			String key = sToken.nextToken().replaceAll("\"", "");
			System.out.println("Trying " + key);
			String protKey = geneIdToProtMap.get(key);
			System.out.println("Got " + protKey);
			
			String description = null;
			
			if( protKey != null)
			{
				Integer intKey = Integer.parseInt(s.split("\t")[0].replaceAll("\"", "").replace("Line_", ""));
				description = lineDescriptions.get(intKey) ;
				
			}
			
			if( description == null)
				description = "NA";
			
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
			
			map.put(splits[0], splits[1]);
		}
		
		reader.close();
		
		return map;
	}
	
	
	@Override
	public void executeProjectFile(ConfigReader cReader, BufferedWriter logWriter) throws Exception
	{
		File inputFile =  BioLockJUtils.requireExistingFile(cReader, ConfigReader.INPUT_GTF_FILE);
		File mbdgFile = BioLockJUtils.requireExistingFile(cReader, ConfigReader.MBGD_EXTENDED_PATH);
		File outFile = new File(BioLockJUtils.requireString(cReader, ConfigReader.OUTPUT_GTF_FILE));
		File topHitsFile = 
				BioLockJUtils.requireExistingFile(cReader, ConfigReader.BLAST_GATHERED_TOP_HITS_FILE);
		
		HashMap<String, String> geneIdtoProtMap = geneIDtoProtMap(topHitsFile);
		HashMap<Integer, String> map = getLineDescriptions(mbdgFile);
		
		addGeneAnnotation(inputFile.getAbsolutePath(), outFile.getAbsolutePath(), 
					map, geneIdtoProtMap);	
	}
	
}
