/**
 * @UNCC BINF 8380
 *  
 * @author Michael Sioda
 * @date Feb 9, 2017
 */
package bioLockJ;

import java.util.*;
import java.io.*;
import org.apache.commons.csv.*;
import org.slf4j.*;

/**
 *
 */
public class Metadata {

	
	protected static final Logger log = LoggerFactory.getLogger(Metadata.class);
	
	private ArrayList<String> attributeNames;
	private HashMap<String, ArrayList<String>> map;
	    
	public Metadata(ArrayList<ArrayList<String>> csvData)
	{
		init(csvData);
	}
	
	public static Metadata loadMetadata(String fileName) 
	{

		if(fileName==null || fileName.trim().length() < 1)
		{
			log.info("Metadata file not configured: " + fileName);
		}
		
		
		log.info("Loading metadata file: " + fileName);
		FileReader fileReader = null;
		CSVParser csvFileParser = null;
		
		ArrayList<ArrayList<String>> csvData = new ArrayList<ArrayList<String>>();
		
        try {
        	
            fileReader = new FileReader(fileName);
            csvFileParser = new CSVParser(fileReader, CSVFormat.DEFAULT);

            Iterator<CSVRecord> csvRecords = csvFileParser.getRecords().iterator();
            
            //int rowNum = 0;
            while (csvRecords.hasNext()) 
            {
            	ArrayList<String> record = new ArrayList<String>();
            	CSVRecord csvRow = csvRecords.next(); csvRow.getRecordNumber();
            	Iterator<String> it = csvRow.iterator();
            	//log.debug("Row #" + BioLockJUtils.formatInt(rowNum++, 3) + " :: #attributes = " + csvRow.size());
            	while (it.hasNext()) 
                {
            		record.add(it.next());
                }
            	//log.debug(record.toString());
            	csvData.add(record);
			}
            
            log.debug("CSV Metadata number rows: " + (csvData.size() - 1));

        } catch (Exception e) {
        	log.error("Error in CsvFileReader  !!!", e);
        } finally {
            try {
                fileReader.close();
                csvFileParser.close();
            } catch (IOException e) {
            	log.error("Error while closing fileReader/csvFileParser !!!", e);
            }
        }
        
        log.info("About to return meta: ");
        return new Metadata(csvData);
	}
	
	public ArrayList<String> getAttributeNames()
	{
		return attributeNames;
	}
	
	public Set<String> getFileNames()
	{
		return map.keySet();
	}
	
	public ArrayList<String> getAttributes(String fileName)
	{
		return map.get(fileName);
	}
	
	public String getAttribute(String fileName, String attribute)
	{
		return map.get(fileName).get(attributeNames.indexOf(attribute));
	}
	
	
	protected void init(ArrayList<ArrayList<String>> csvData)
	{
		log.debug("Initializing Metadata object");
		Iterator<ArrayList<String>> rows = csvData.iterator();
		map = new HashMap<String, ArrayList<String>>();
		boolean firstRow = true;
		log.debug("Number of rows: " + csvData.size());
		while(rows.hasNext())
		{	
			boolean firstCol = true;
			String name = null;
			ArrayList<String> atts = new ArrayList<String>();
			
			Iterator<String> record = rows.next().iterator();
			while(record.hasNext())
			{
				String val = record.next();
				if(!firstCol)
				{
					atts.add(val);
				}
				else
				{
					name = val;
					firstCol = false;
				}
			}
			
			if(firstRow)
			{
				firstRow = false;
				attributeNames = atts;
				log.debug("Setting attribute names: " + atts);
			}
			else if (name!=null)
			{
				log.debug("Setting " + name + " attribute values: " + atts);
				map.put(name, atts);
			}
		}	
	}   	
}
