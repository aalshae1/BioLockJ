package projects.findResistance;

import java.io.File;

import bioLockJ.BioJLockUtils;
import homologySearch.BreakUpFastaSequence;
import homologySearch.blast.MultipleQueriesToOneBlastDB;
import utils.ConfigReader;

public class CardsAnnotationToIGV 
{
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
			throw new Exception("Usage " + CardsAnnotationToIGV.class.getName() + " pathToPropertyFile.txt");
		
		File propsFile = BioJLockUtils.findProperyFile(args);
		ConfigReader cReader = new ConfigReader(propsFile);
		int pollTime = BioJLockUtils.requirePositiveInteger(cReader, ConfigReader.POLL_TIME);
		
		new BreakUpFastaSequence().executeProjectFile(propsFile);
		
		MultipleQueriesToOneBlastDB queries = new MultipleQueriesToOneBlastDB();
		queries.executeProjectFile(propsFile);
		BioJLockUtils.executeCHMOD_ifDefined(cReader, queries.getRunAllFile());
		BioJLockUtils.executeFile(queries.getRunAllFile());
		BioJLockUtils.pollAndSpin(queries.getScriptFiles(), pollTime );
		
		System.out.println("Succesful finish");
		
	}
}
