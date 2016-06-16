package projects.findResistance;

import java.io.File;

import bioLockJ.BioJLockUtils;
import homologySearch.BreakUpFastaSequence;
import homologySearch.blast.FormatMultipleBlastDatabases;

public class CardsAnnotationToIGV 
{
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
			throw new Exception("Usage " + CardsAnnotationToIGV.class.getName() + " pathToPropertyFile.txt");
		
		File propsFile = BioJLockUtils.findProperyFile(args);
		new BreakUpFastaSequence().executeProjectFile(propsFile);
		new FormatMultipleBlastDatabases().executeProjectFile(propsFile);
	}
}
