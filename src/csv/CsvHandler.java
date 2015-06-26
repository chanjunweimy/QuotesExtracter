package csv;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import analyzer.AuthorQuote;
import analyzer.TextAnalyzer;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CsvHandler {
	private static final char CSV_DELIMITER = ',';
	
	public boolean writeFile(String filename, String[] columnDatas) {
		File f = new File(filename);
		if (!f.exists()) {
			System.err.println("initiating file " + filename);
			initCsvFile(filename);
		}
		return writeDataToCsv(filename, columnDatas);
	}

	/**
	 * @param filename
	 */
	private void initCsvFile(String filename) {
		String columnRecord = "Id,Source,Date,Url,Title,IsTitleHasStatement,Author,Quote,Description,Rule";
		String[] columnNames = columnRecord.split(",");
		writeDataToCsv(filename, columnNames);
	}

	/**
	 * @param filename
	 * @param columnDatas
	 * @return
	 */
	private boolean writeDataToCsv(String filename, String[] columnDatas) {
		try {
			boolean isAppend = true;
			FileWriter fileWriter = new FileWriter(filename, isAppend);
			CSVWriter writer = new CSVWriter(fileWriter);
			
			//Write the record to file
			writer.writeNext(columnDatas);
			    
			//close the writer
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}

		
		return true;
	}
	
	public void readFile(String filename) {
        CSVReader reader = null;
        try
        {
            //Get the CSVReader instance with specifying the delimiter to be used
            reader = new CSVReader(new FileReader(filename), CSV_DELIMITER);
            String [] nextLine = null;
            String [] columnNames = null;
            
            TextAnalyzer analyzer = new TextAnalyzer();
            boolean start = true;
            
            //Read one line at a time
            while ((nextLine = reader.readNext()) != null)
            {
            	if (start) {
            		start = false;
            		columnNames = nextLine;
            		continue;
            	}
            	
                String id = "";
                String source = "";
                String date = "";
                String url = "";
                String rule = "";
                for (int i = 0; i < nextLine.length; i++) {
                    String token = nextLine[i];
                    String columnName = columnNames[i].trim();
                    if (columnName.equalsIgnoreCase("source")) {
                    	source = token;
                    } else if (columnName.equalsIgnoreCase("date")) {
                    	date = token;
                    } else if (columnName.equalsIgnoreCase("url")) {
                    	url = token;
                    } else if (columnName.equalsIgnoreCase("id")) {
                    	id = token;
                    }
                }
                
                for (int i = 0; i < nextLine.length; i++) {
                	String token = nextLine[i];
                    String columnName = columnNames[i].trim();
                	if (columnName.equalsIgnoreCase("title")) {
                    	String title = token;
                    	AuthorQuote authorQuote = analyzer.getQuotes(token);
                    	String statement = "no";
                    	String author = "";
                    	String quote = "";
                    	String description = "";
            			if (authorQuote != null) {
            				statement = "yes";         
            				author = authorQuote.getAuthor();
            				quote = authorQuote.getQuote();
            				description = authorQuote.getDescription();
            				rule = authorQuote.getRule();
            			}
            			//System.err.println(reply);
        				String delimeter = ",,,,,,,,";
        				String record = id + delimeter + 
        								source + delimeter +
        								date + delimeter +
        								url + delimeter +
        								title + delimeter + 
        								statement + delimeter +
        								author + delimeter +
        								quote + delimeter + 
        								description + delimeter +
        								rule;
        					
        		    	String outputFile = "output.csv";
        		    	String[] columns = record.split(delimeter);
        		    	writeFile(outputFile, columns);
                    }
                }
            }
            
            System.out.println("\n");

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
        System.out.println("Done Parsing.");
	}
	
	
    public static void main(String[] args) {
    	CsvHandler handler = new CsvHandler();
    	handler.readFile("result.csv");
    }
}
