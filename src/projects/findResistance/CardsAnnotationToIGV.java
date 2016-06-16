package projects.findResistance;

import java.io.File;

import bioLockJ.BioJLockUtils;
import homologySearch.BreakUpFastaSequence;
import homologySearch.blast.FormatMultipleBlastDatabases;
import utils.ConfigReader;

public class CardsAnnotationToIGV 
{
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
			throw new Exception("Usage " + CardsAnnotationToIGV.class.getName() + " pathToPropertyFile.txt");
		
		File propsFile = BioJLockUtils.findProperyFile(args);
		ConfigReader cReader = new ConfigReader(propsFile);
		new BreakUpFastaSequence().executeProjectFile(propsFile);
		FormatMultipleBlastDatabases fmbd = new FormatMultipleBlastDatabases();
		fmbd.executeProjectFile(propsFile);
		BioJLockUtils.executeCHMOD_ifDefined(cReader);
		BioJLockUtils.executeFile(fmbd.getRunAllFile());

	}
}
