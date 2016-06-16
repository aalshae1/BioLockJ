package projects.findResistance;

import java.io.File;

import bioLockJ.BioJLockUtils;
import homologySearch.BreakUpFastaSequence;
import homologySearch.blast.FormatSingleBlastDatabase;
import homologySearch.blast.GatherBlastHits;
import homologySearch.blast.MultipleQueriesToOneBlastDB;
import utils.ConfigReader;

public class CardsAnnotationToIGV 
{
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
			throw new Exception("Usage " + CardsAnnotationToIGV.class.getName() + " pathToPropertyFile.txt");
		
		File propsFile = BioJLockUtils.findProperyFile(args);
		
		
		//ConfigReader cReader = new ConfigReader(propsFile);
		//new BreakUpFastaSequence().executeProjectFile(propsFile);
		//BioJLockUtils.writeScriptsAndSpin(cReader, new FormatSingleBlastDatabase());
		//BioJLockUtils.writeScriptsAndSpin(cReader, new MultipleQueriesToOneBlastDB());
	
		new GatherBlastHits().executeProjectFile(propsFile);
		
		System.out.println("Succesful finish");
		
	}
}
