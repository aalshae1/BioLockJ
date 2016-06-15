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
		
		StringBuffer found = new StringBuffer();
		StringBuffer notFound = new StringBuffer();
		
		for( Field f : cReader.getClass().getFields())
		{
			String name = f.getName();
			
			if( ! name.equals("TRUE") && ! name.equals("YES"))
			{
				if( cReader.getAProperty(name)  != null)
				{
					found.append(name + " " + cReader.getAProperty(name) + "\n");
					
					if( name.indexOf("PATH") != -1 || name.indexOf("DIR") != -1  )
					{
						File bFile = new File(cReader.getAProperty(name));
						
						if( ! bFile.exists())
							found.append("\tCould not find file " + bFile.getAbsolutePath() + "\n");
						
						if( bFile.exists() &&  name.indexOf("DIR") != -1 && ! bFile.isDirectory())
							found.append("\t" + bFile.getAbsolutePath() + " is not a directory " + "\n");
					}
				}
				else
				{
					notFound.append(name + " \n");
				}
			}	
		}
		
		System.out.println("\nFOUND:\n" + found.toString() + "\n");
		System.out.println("NOT FOUND:\n" + notFound.toString() + "\n");
	}
}
