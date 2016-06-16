package projects.findResistance;

import java.io.File;

import bioLockJ.BioJLockUtils;
import homologySearch.BreakUpFastaSequence;
import homologySearch.blast.FormatSingleBlastDatabase;
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
		FormatSingleBlastDatabase fmsd = new FormatSingleBlastDatabase();
		fmsd.executeProjectFile(propsFile);
		BioJLockUtils.executeCHMOD_ifDefined(cReader, fmsd.getRunAllFile());
		BioJLockUtils.executeFile(fmsd.getRunAllFile());
		BioJLockUtils.pollAndSpin(fmsd.getScriptFiles(), pollTime );
		
		System.out.println("Succesful finish");
		
	}
}
