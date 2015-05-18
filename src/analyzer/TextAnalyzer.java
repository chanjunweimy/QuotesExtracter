package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class TextAnalyzer {
	private String getQuotes(String text){
		Pattern p = Pattern.compile( "\"([^\"]*)\"" );
		Matcher m = p.matcher(text);
		StringBuffer quote = new StringBuffer("");
		while( m.find()) {
		   quote.append(m.group(1).toString() + "\n");
		}
		return quote.toString().trim();
	}
	
	/*
	private String getName(String text) {
		
		
		BufferedReader br = null;
		try {
			br = new BufferedReader
					 (new FileReader("dictionary.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	        try {
				br.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}

			return null;
		}

        while (line != null) {
            try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}*/
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			TextAnalyzer analyzer = new TextAnalyzer();
			String quote = analyzer.getQuotes(line);
			System.out.println(quote);
		}
		sc.close();
	}
}
