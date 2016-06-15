package blast;

import java.io.File;

import bioLockJ.BioLockJExecutor;

/*
 * Takes in FASTA_TO_CHUNK_PATH
 * Writes out to BASE_OUTPUT_DIR/FASTA_CHUNKS
 */
public class BreakUpFastaSequence implements BioLockJExecutor
{
	public static void main(String[] args)
	{
		if( args.length != 1)
		{
			System.out.println("Usage " + BreakUpFastaSequence.class.getName() + " pathToPropertyFile" );
			System.exit(1);
		}
		
		File propFile = new File(args[0]);
		
	}
	
	@Override
	public void executeProjectFile(File projectFile)
	{
		// TODO Auto-generated method stub
		
	}
}
