package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class TextAnalyzer {
	private class AuthorQuote {
		private String _author = null;
		private String _quote = null;

		protected String getAuthor() {
			assert (_author != null);
			return _author;
		}

		protected String getQuote() {
			assert (_quote != null);
			return _quote;
		}

		protected void setAuthor(String author) {
			_author = author;
		}

		protected void setQuote(String quote) {
			_quote = quote;
		}
	}
	
	public TextAnalyzer() {
		loadFromFile();
	}
	
	private Vector <String> _reportingVerbsDictionary = null;
	
	private void loadFromFile() {
		BufferedReader br = null;
		int defaultMaxDictSize = 1000;
		_reportingVerbsDictionary = new Vector <String> (defaultMaxDictSize);
		try {
			br = new BufferedReader(new FileReader("reportingVerbs.dictionary"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	    try {
	    	String line = br.readLine();
	        while (line != null) {
	            _reportingVerbsDictionary.add(line);
	            line = br.readLine();
	        }
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	private boolean isReportingVerbs(String token) {
		for (int i = 0; i < _reportingVerbsDictionary.size(); i++) {
			String reportingVerb = _reportingVerbsDictionary.get(i);
			if (reportingVerb.equals(token)) {
				return true;
			}
		}
		return false;
	}

	private AuthorQuote getQuotes(String text) {
		AuthorQuote authorQuote = null;
		
		if (text.split(":").length == 2) {
			authorQuote = extractQuoteWithColon(text);
		} else {
			authorQuote = extractQuoteWithoutColon(text);
		}
		
		return authorQuote;
		/*
		 * Pattern p = Pattern.compile( "\"([^\"]*)\"" ); Matcher m =
		 * p.matcher(text); StringBuffer quote = new StringBuffer(""); while(
		 * m.find()) { quote.append(m.group(1).toString() + "\n"); } return
		 * quote.toString().trim();
		 */
	}
	
	private AuthorQuote extractQuoteWithColon(String text) {
		AuthorQuote authorQuote = null;
		String[] partitions = text.split(":");
		String[] partitionFirst = partitions[0].split(" ");
		String[] partitionSecond = partitions[1].split(" ");
		
		if (partitionFirst.length > 4 && partitionSecond.length > 4) {
			return null;
		}
		
		authorQuote = new AuthorQuote();
		if (partitionFirst.length > partitionSecond.length) {
			authorQuote.setAuthor(partitions[1]);
			authorQuote.setQuote(partitions[0]);
		} else {
			authorQuote.setAuthor(partitions[0]);
			authorQuote.setQuote(partitions[1]);
		}
		
		return authorQuote;
	}
	
	private int toInt(String str) {
		return Integer.parseInt(str);
	}

	/**
	 * @param text
	 * @return
	 */
	private AuthorQuote extractQuoteWithoutColon(String text) {
		AuthorQuote authorQuote = null;
		
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";

		LexicalizedParser lp;
		lp = LexicalizedParser.loadModel(parserModel);

		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory
				.getTokenizer(new StringReader(text));
		List<CoreLabel> rawWords2 = tok.tokenize();
		Tree parse = lp.apply(rawWords2);

		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack
		// for English
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		
		Vector <String> tokens = new Vector <String>(rawWords2.size());
		
		for (int i = 0; i < rawWords2.size(); i++) {
			String token = rawWords2.get(i).toString();
			token = token.split("-")[0].trim();
			tokens.add(token);
		}	
		
		//find name
		for (int i = 0; i < tdl.size(); i++) {
			TypedDependency td = tdl.get(i);
			String tds = td.toString();
			//System.out.println(tds);

			if (tds.startsWith("nsubj")) {
				tds = tds.replaceFirst("nsubj", "");
				tds = tds.replace("(", "");
				tds = tds.replace(")", "");
				tds = tds.replace(" ", "");
				String[] nsubjs = tds.split(",");
				nsubjs[0] = nsubjs[0].split("-")[0];
				nsubjs[1] = nsubjs[1].split("-")[0];

				if (isReportingVerbs(nsubjs[0])) {
					String author = nsubjs[1];
					int min = tdl.size() + 2;
					int max = -1;
					for (int j = 0; j < tdl.size(); j++) {
						TypedDependency td2 = tdl.get(j);
						String tds2 = td2.toString();
						if (tds2.startsWith("compound")) {
							tds2 = tds2.replaceFirst("compound", "");
							tds2 = tds2.replace("(", "");
							tds2 = tds2.replace(")", "");
							tds2 = tds2.replace(" ", "");
							String[] compounds = tds2.split(",");
							String[] authorTokens = compounds[0].split("-");
							if (author == authorTokens[0]) {
								int first = toInt(authorTokens[1]);
								int second = toInt(compounds[1].split("-")[1]);
								
								min = Math.min(first, min);
								min = Math.min(second, min);
								max = Math.max(first, max);
								max = Math.max(second, max);
							}
									
						}
					}
					
					StringBuffer sb = new StringBuffer();
					for (int j = min; j <= max; j++) {
						sb.append(tokens.get(j - 1));
					}
					author = sb.toString();
					
					//appos to find the xing rong ci
					
					//aux to eliminate has/have
					
					//mark to eliminate that/to
					
					
					//String reportingVerb = nsubjs[0];
					
					String quote = text;
					//String quote = text.replace(author, "");
					//quote = quote.replace(reportingVerb, "");
					//quote = quote.replace("  ", " ");
					
					authorQuote = new AuthorQuote();
					
					authorQuote.setAuthor(author);
					authorQuote.setQuote(quote);
				}
			}
		}
	    
		return authorQuote;
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		
		TextAnalyzer analyzer = new TextAnalyzer();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			AuthorQuote quote = analyzer.getQuotes(line);
			if (quote != null) {
				String reply = "author: " + quote.getAuthor() + "\n" + "quote: " + quote.getQuote() + "\n";
				System.out.println(reply);
			}
			// System.out.println(quote);
		}
		sc.close();
	}
}
