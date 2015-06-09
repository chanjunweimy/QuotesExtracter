package csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import com.opencsv.CSVReader;

public class CsvReader {
	private final String CSV_DELIMITER = ",";
	
	public Vector < Vector <String> > readCsvFile(String filename) {
		File csvFile = new File(filename);
		Vector < Vector <String> > csvFileContents = null;
		try {
			Scanner scanner = new Scanner(csvFile);
			csvFileContents = new Vector < Vector <String> >();
			while (scanner.hasNextLine()) {
				String row = scanner.nextLine();
				
			}
			
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return csvFileContents;
	}
	
    public static void main(String[] args) {
        CSVReader reader = null;
        try
        {
            //Get the CSVReader instance with specifying the delimiter to be used
            reader = new CSVReader(new FileReader("result.csv"),',');
            String [] nextLine;
            //Read one line at a time
            while ((nextLine = reader.readNext()) != null)
            {
                for(String token : nextLine)
                {
                    //Print all tokens
                    System.out.println(token);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
