package homologySearch.blast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import bioLockJ.BioJLockUtils;
import bioLockJ.BioLockJExecutor;
import homologySearch.BreakUpFastaSequence;
import parsers.HitScores;
import utils.ConfigReader;

/**
 * Gathers the top hits in BLAST_OUTPUT_DIRECTORY.
 * There should be other files in BLAST_OUTPUT_DIRECTORY (sub-directories are ignored)
 * and writes them all to BLAST_GATHERED_TOP_HITS_FILE
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
	
	public void executeProjectFile(File projectFile) throws Exception
	{
		ConfigReader cReader = new ConfigReader(projectFile);
		File blastOutputDir = BioJLockUtils.requireExistingDirectory(cReader, ConfigReader.BLAST_OUTPUT_DIRECTORY);
		File topHitsFile = new File( BioJLockUtils.requireString(cReader, ConfigReader.BLAST_GATHERED_TOP_HITS_FILE));
		
		File logDir = BioJLockUtils.createLogDirectory(topHitsFile.getParentFile(), 
								BreakUpFastaSequence.class.getSimpleName());
		BioJLockUtils.copyPropertiesFile(projectFile, logDir);
		
		List<HitScores> hits = getHits(blastOutputDir);
		writeResults(hits, blastOutputDir);
		
		BufferedWriter logWriter = new BufferedWriter(new FileWriter(new File(
				logDir.getAbsolutePath() + File.separator + BreakUpFastaSequence.class.getSimpleName() 
				 +"log.txt")));
		
		logWriter.write("successful completion at " + new Date().toString() + "\n"); 
		logWriter.flush(); logWriter.close();
		BioJLockUtils.appendSuccessToPropertyFile(projectFile, this.getClass().getName(), logDir);
	}

	
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			System.out.println("Usage " + GatherBlastHits.class.getName() + " pathToPropertyFile" );
			System.exit(1);
		}
		
		File propFile = BioJLockUtils.findProperyFile(args);
		new FormatSingleBlastDatabase().executeProjectFile(propFile);
	}
	
}
