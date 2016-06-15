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
			String name = f.getName();
			
			if( ! name.equals("TRUE") && ! name.equals("YES"))
			{
				if( cReader.getAProperty(name)  != null)
				{
					System.out.println("Found " + name + " " + cReader.getAProperty(name));
					
					if( name.indexOf("PATH") != -1 || name.indexOf("DIR") != -1  )
					{
						File bFile = new File(cReader.getAProperty(name));
						
						if( ! bFile.exists())
							System.out.println("Could not find file " + bFile.getAbsolutePath());
						
						if( bFile.exists() &&  name.indexOf("DIR") != -1 && ! bFile.isDirectory())
							System.out.println(bFile.getAbsolutePath() + " is not a directory ");
					}
				}
				else
				{
					System.out.println(name + " is not defined in the property file");
				}
			}
			
		}
	}
}
