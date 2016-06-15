package bioLockJ;

import java.io.File;
import java.lang.reflect.Field;

import utils.ConfigReader;

public class ValidatePropertiesFile
{
	public static void main(String[] args) throws Exception
	{
		if( args.length != 1)
		{
			System.out.println("Usage " + ValidatePropertiesFile.class.getName() 
					+ " pathToPropertiesFileToValidate " );
			System.exit(1);
		}
		
		File aFile =new File(args[0]);
		

		System.out.println("Trying to find " + aFile.getAbsolutePath());
		
		if( ! aFile.exists())
			throw new Exception("Could not find " + aFile.getAbsolutePath());
		
		ConfigReader cReader = new ConfigReader(aFile.getAbsoluteFile());
		
		for( Field f : cReader.getClass().getFields())
		{
			System.out.println(f.getName());
		}
	}
}
