package bioLockJ;

import java.io.File;

public class LogUtils
{
	public static File createLogDirectory( File outputDirectory ) throws Exception
	{
		while(true)
		{
			File logDir = null;
			
			while( logDir == null || logDir.exists() )
			{
				logDir = new File(outputDirectory.getAbsolutePath() + File.separator + 
							"log_" + System.currentTimeMillis());
				
				if( logDir.exists())
					Thread.sleep(100);
				
			}
			
			logDir.mkdirs();
			
			return logDir;
		}
	}
}
