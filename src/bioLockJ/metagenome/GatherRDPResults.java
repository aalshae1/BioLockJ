package bioLockJ.metagenome;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

import bioLockJ.BioLockJExecutor;
import bioLockJ.BioLockJUtils;
import parsers.NewRDPNode;
import parsers.NewRDPParserFileLine;
import utils.ConfigReader;

public class GatherRDPResults extends BioLockJExecutor
{
	public static final String THREE_COL_SUFFIX = "_SparseThreeCol.txt";
	
	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_OUTPUT_RDP_DIRECTORY);
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.RDP_SUMMARY_DIRECTORY);
		BioLockJUtils.requirePositiveInteger(cReader,  ConfigReader.RDP_THRESHOLD);
		
	}
	
	@Override
	public void executeProjectFile(ConfigReader cReader, BufferedWriter logWriter) throws Exception
	{
		File rdpOutDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.PATH_TO_OUTPUT_RDP_DIRECTORY);
		File summaryDir =  BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.RDP_SUMMARY_DIRECTORY);
		int rdpThreshold = BioLockJUtils.requirePositiveInteger(cReader,  ConfigReader.RDP_THRESHOLD);
		
		HashMap<String, BufferedWriter> taxaWriters = new HashMap<String, BufferedWriter>();
		
		for( int x=1; x < NewRDPParserFileLine.TAXA_ARRAY.length; x++)
		{
			 BufferedWriter writer = new BufferedWriter(
					 	new FileWriter(new File(
					 summaryDir.getAbsolutePath() + File.separator + 
					 		NewRDPParserFileLine.TAXA_ARRAY[x] + THREE_COL_SUFFIX)));
			 taxaWriters.put(NewRDPParserFileLine.TAXA_ARRAY[x], writer);
		}
		
		for(String s : rdpOutDir.list())
		{
			System.out.println(s);
				List<NewRDPParserFileLine> list = NewRDPParserFileLine.getRdpListSingleThread(
					rdpOutDir.getAbsoluteFile() + File.separator + s	);
				
			for( int x=1; x < NewRDPParserFileLine.TAXA_ARRAY.length; x++)
			{
				HashMap<String, Integer> countMap = 
						getCount(NewRDPParserFileLine.TAXA_ARRAY[x], list, rdpThreshold);
				
				BufferedWriter writer = taxaWriters.get(NewRDPParserFileLine.TAXA_ARRAY[x]);
				
				for(String key: countMap.keySet())
				{
					writer.write( s.replaceAll(RunMultipleRDP.FINISHED_SUFFIX, "") + "\t" + 
								key + "\t" + countMap.get(key) + "\n");
				}
				
				writer.flush();
			}
		}
		
		for(BufferedWriter writer : taxaWriters.values())
		{
			writer.flush();  writer.close();
		}

	}
	

	private static HashMap<String, Integer> getCount( String level, 
					List<NewRDPParserFileLine>  rdpList , int threshold) throws Exception
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		for( NewRDPParserFileLine rdp : rdpList )
		{
			NewRDPNode node = rdp.getTaxaMap().get(level);
			
			if( node != null && node.getScore() >= threshold)
			{
				Integer count = map.get(node.getTaxaName());
				
				if( count == null)
					count =0;
				
				count++;
				
				map.put(node.getTaxaName(), count);
			}
		}
		
		return map;
	}
}
