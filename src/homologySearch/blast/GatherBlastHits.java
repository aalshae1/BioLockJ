package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bioLockJ.BioLockJUtils;
import bioLockJ.BioLockJExecutor;
import parsers.HitScores;
import utils.ConfigReader;

/**
 * Gathers the top hits in BLAST_OUTPUT_DIRECTORY.
 * There should be other files in BLAST_OUTPUT_DIRECTORY (sub-directories are ignored)
 * and writes them all to BLAST_GATHERED_TOP_HITS_FILE
 * 
 * if GTF_GATHERED_TOP_HITS_FILE is defined, then a GTF file is written to that path
 */
public class GatherBlastHits extends BioLockJExecutor
{
	private List<HitScores> getHits(File blastOutputDir) throws Exception
	{
		List<HitScores> list = new ArrayList<HitScores>();
		
		for(String s : blastOutputDir.list())
		{
			File f = new File(blastOutputDir.getAbsolutePath() + File.separator + s);
			
			if( ! f.isDirectory())
			{
				HashMap<String, HitScores> map = HitScores.getTopHitsAsQueryMap(f.getAbsolutePath());
				list.addAll(map.values());
			}
		}
		
		return list;
	}
	
	private void writeGTFFile( List<HitScores> list, File outFile, boolean useQueryCoordinates ) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		
		for(HitScores hs : list)
		{
			writer.write(hs.getTargetId() + "\tblast\t" +  hs.getQueryId() + "\t");  
			
			if( ! useQueryCoordinates)
				writer.write(hs.getTargetStart() + "\t" + hs.getTargetEnd() + "\t");
			else
				writer.write(hs.getQueryStart() + "\t" + hs.getQueryEnd() + "\t");
			
			writer.write(hs.getBitScore() + "\t" + "+"  + "\t"  + "+" + "0\taGene\taGene\n");
		}
		
		writer.flush();  writer.close();
	}
	
	private void writeResults(List<HitScores> list, File outFile) throws Exception
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		
		writer.write("queryID\ttargetID\ttargetStart\ttargetEnd\tqueryStart\tqueryEnd\tbitScore\teScore\n");
		
		for(HitScores hs : list)
		{
			writer.write(hs.getQueryId() + "\t"+ hs.getTargetId() + "\t" + 
							hs.getTargetStart() + "\t" + hs.getTargetEnd() + "\t" + hs.getQueryStart() + "\t"+
								hs.getQueryEnd() + "\t" + hs.getBitScore() + "\t" + hs.getEScore() + "\n");
		}
		
		writer.flush();  writer.close();
	}
	
	@Override
	public void checkDependencies(ConfigReader cReader) throws Exception
	{
		BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.BLAST_OUTPUT_DIRECTORY);
		BioLockJUtils.requireString(cReader, ConfigReader.BLAST_GATHERED_TOP_HITS_FILE);
		BioLockJUtils.requireBoolean(cReader, ConfigReader.OUTPUT_QUERY_COORDINATES_TO_GTF);
	}
	
	public void executeProjectFile(ConfigReader cReader) throws Exception
	{
		File blastOutputDir = BioLockJUtils.requireExistingDirectory(cReader, ConfigReader.BLAST_OUTPUT_DIRECTORY);
		File topHitsFile = new File( BioLockJUtils.requireString(cReader, ConfigReader.BLAST_GATHERED_TOP_HITS_FILE));
		boolean useQueryCoordiantes = BioLockJUtils.requireBoolean(cReader, ConfigReader.OUTPUT_QUERY_COORDINATES_TO_GTF);
		
		List<HitScores> hits = getHits(blastOutputDir);
		writeResults(hits, topHitsFile);
		
		if( cReader.getAProperty(ConfigReader.GTF_GATHERED_TOP_HITS_FILE) != null)
		{
			writeGTFFile(hits, new File(cReader.getAProperty(ConfigReader.GTF_GATHERED_TOP_HITS_FILE)),
					useQueryCoordiantes);
		}
		else
		{
			logWriter.write(ConfigReader.GTF_GATHERED_TOP_HITS_FILE + " not defined so skipping GTF\n");
			logWriter.flush();
		}
	}
	
}
