package analyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

public class TextAnalyzer {
	
	
	public TextAnalyzer() {
		loadFromFile();
	}
	
	private Vector <String> _reportingVerbsDictionary = null;
	//final private String[] PUNCTUATIONS = {",", "\"", "\'"};
	final private String[] MARKS = {"that"};
	private static final String basedir = System.getProperty("TextAnalyzer", "data");
	private CRFClassifier<CoreLabel> _segmenter;

	
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
	    
	    
	    //for Chinese
	    Properties props = new Properties();
	    props.setProperty("sighanCorporaDict", basedir);
	    // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
	    // props.setProperty("normTableEncoding", "UTF-8");
	    // below is needed because CTBSegDocumentIteratorFactory accesses it
	    props.setProperty("serDictionary", basedir + "/dict-chris6.ser.gz");
	    props.setProperty("inputEncoding", "UTF-8");
	    props.setProperty("sighanPostProcessing", "true");

	    _segmenter = new CRFClassifier<CoreLabel>(props);
	    _segmenter.loadClassifierNoExceptions(basedir + "/ctb.gz", props);
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

	public AuthorQuote getQuotes(String text) {
		AuthorQuote authorQuote = null;
		
		if (text.split(":").length == 2) {
			authorQuote = extractQuoteWithColon(text);
		} else if (text.split("：").length == 2) {
			authorQuote = extractQuoteWithChineseColon(text);
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

	private AuthorQuote extractQuoteWithChineseColon(String text) {
		AuthorQuote authorQuote = null;
		String[] partitions = text.split("：");
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
				
		if (isCJK(text)) {
			authorQuote = parseChineseTexts(text, authorQuote);
		} else {
			authorQuote = parseEnglishTexts(text, authorQuote);
		}
	    
		return authorQuote;
	}

	/**
	 * @param text
	 * @param authorQuote
	 * @return
	 */
	private AuthorQuote parseChineseTexts(String text, AuthorQuote authorQuote) {
		String parserModel;
		parserModel = "edu/stanford/nlp/models/lexparser/chineseFactored.ser.gz";
		/*
		 * 		HashSet <String> chineseStrings = new HashSet<String> ();

		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
		    Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
		    if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)|| 
		        Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)|| 
		        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)){
		    	String chineseString = text.substring(i, i + 1);
		    	chineseStrings.add(chineseString);
		        //return true;
		    }
		}
		
		for (String str : chineseStrings) {
		    text = text.replaceAll(str, str + " ");
		}
		*/
		
		

	    List<String> segmented = _segmenter.segmentString(text);

	    StringBuffer sb = new StringBuffer();
	    for (String token : segmented) {
	    	sb.append(token);
	    	sb.append(" ");
	    }
	    text = sb.toString().trim();
	    System.err.println("tokenized text is: " + text);
		
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
			System.out.println(tds);


			if (tds.startsWith("nsubj")) {
				tds = tds.replaceFirst("nsubj", "");
				tds = tds.replace("(", "");
				tds = tds.replace(")", "");
				tds = tds.replace(" ", "");
				String[] nsubjs = tds.split(",");
				String[] reportingVerbArray = nsubjs[0].split("-");
				//String[] authorArray = nsubjs[1].split("-");

				if (isReportingVerbs(reportingVerbArray[0])) {
					String author = nsubjs[1];
					int min = tdl.size() + 2;
					int max = -1;
					
					int authorMin = tdl.size() + 2;
					int authorMax = -1;
					String appos = "";
					//compound to find the full name
					//aux to eliminate has/have
					//mark to eliminate that/to
					//appos to find the xing rong ci
					
					for (int j = 0; j < tdl.size(); j++) {
						TypedDependency td2 = tdl.get(j);
						String tds2 = td2.toString();
						
						if (tds2.startsWith("compound")) {
							
							tds2 = tds2.replaceFirst("compound", "");
							tds2 = tds2.replace("(", "");
							tds2 = tds2.replace(")", "");
							tds2 = tds2.replace(" ", "");
							String[] compounds = tds2.split(",");
							
							if (author.equals(compounds[0])) {
								String[] authorTokens = compounds[0].split("-");
								int first = toInt(authorTokens[1]);
								int second = toInt(compounds[1].split("-")[1]);
								
								min = Math.min(first, min);
								min = Math.min(second, min);
								max = Math.max(first, max);
								max = Math.max(second, max);
								
								authorMin = Math.min(first, authorMin);
								authorMin = Math.min(second, authorMin);
								authorMax = Math.max(first, authorMax);
								authorMax = Math.max(second, authorMax);
							}
									
						}  else if (tds2.startsWith("mark")) {
							tds2 = tds2.replaceFirst("mark", "");
							tds2 = tds2.replace("(", "");
							tds2 = tds2.replace(")", "");
							tds2 = tds2.replace(" ", "");
							String[] marks = tds2.split(",");
							marks = marks[1].split("-");
							
							for (int k = 0; k < MARKS.length; k++) {
								if (MARKS[k].equals(marks[0].toLowerCase())) {
									int mark = toInt(marks[1]);
									
									min = Math.min(mark, min);
									max = Math.max(mark, max);
								}
							}
						} else if (tds2.startsWith("appos")) {
							tds2 = tds2.replaceFirst("appos", "");
							tds2 = tds2.replace("(", "");
							tds2 = tds2.replace(")", "");
							tds2 = tds2.replace(" ", "");
							String[] apposes = tds2.split(",");
							
							if (apposes[0].equals(nsubjs[1])) {
								String tempAppos = apposes[1];
								StringBuffer apposBuffer = new StringBuffer();
								int tempApposPos = toInt(apposes[1].split("-")[1]);
								int apposMin = tempApposPos;
								int apposMax = tempApposPos;
								for (int k = 0; k < tdl.size(); k++) {
									String tds3 = tdl.get(k).toString();
									if (tds3.startsWith("compound")) {
										tds3 = tds3.replaceFirst("compound", "");
										tds3 = tds3.replace("(", "");
										tds3 = tds3.replace(")", "");
										tds3 = tds3.replace(" ", "");
										String[] compounds = tds3.split(",");
										
										if (compounds[0].equals(tempAppos)) {
											compounds = compounds[1].split("-");
											int pos = toInt(compounds[1]);
											apposMin = Math.min(pos, apposMin);
											apposMax = Math.max(pos, apposMax);
										}
									}
								}
								
								for (int k = apposMin; k <= apposMax; k++) {
									apposBuffer.append(tokens.get(k - 1) + " ");
								}
								
								min = Math.min(apposMin, min);
								max = Math.max(apposMax, max);
								
								appos = apposBuffer.toString().trim();
							}
						}
					}
					
					int reportingVerbsPosition = toInt(reportingVerbArray[1]);
					min = Math.min(min, reportingVerbsPosition);
					max = Math.max(max, reportingVerbsPosition);
					
					StringBuffer authorBuffer = new StringBuffer();
					for (int j = authorMin; j <= authorMax; j++) {
						authorBuffer.append(tokens.get(j - 1) + " ");
					}
					author = authorBuffer.toString().trim();
					
					

					
					
					//String reportingVerb = nsubjs[0];
					
					//String quote = text;
					//String quote = text.replace(author, "");
					//quote = quote.replace(reportingVerb, "");
					//quote = quote.replace("  ", " ");
					StringBuffer quoteBuffer = new StringBuffer();
					min--;
					max--;
					for (int j = 0; j < tokens.size(); j++) {
						if (j >= min && j <= max) {
							continue;
						}
						quoteBuffer.append(tokens.get(j) + " ");
					}
					String quote = quoteBuffer.toString().trim();
					
					authorQuote = new AuthorQuote();
					
					authorQuote.setAuthor(author);
					authorQuote.setQuote(quote);
					authorQuote.setDescription(appos);
					break;
				}
			}
		}
		return authorQuote;
	}

	/**
	 * @param text
	 * @param authorQuote
	 * @return
	 */
	private AuthorQuote parseEnglishTexts(String text, AuthorQuote authorQuote) {
		String parserModel;
		parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
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
			System.out.println(tds);


			if (tds.startsWith("nsubj")) {
				tds = tds.replaceFirst("nsubj", "");
				tds = tds.replace("(", "");
				tds = tds.replace(")", "");
				tds = tds.replace(" ", "");
				String[] nsubjs = tds.split(",");
				String[] reportingVerbArray = nsubjs[0].split("-");
				//String[] authorArray = nsubjs[1].split("-");

				if (isReportingVerbs(reportingVerbArray[0])) {
					String author = nsubjs[1];
					int min = tdl.size() + 2;
					int max = -1;
					
					int authorMin = tdl.size() + 2;
					int authorMax = -1;
					String appos = "";
					//compound to find the full name
					//aux to eliminate has/have
					//mark to eliminate that/to
					//appos to find the xing rong ci
					
					for (int j = 0; j < tdl.size(); j++) {
						TypedDependency td2 = tdl.get(j);
						String tds2 = td2.toString();
						
						if (tds2.startsWith("compound")) {
							
							tds2 = tds2.replaceFirst("compound", "");
							tds2 = tds2.replace("(", "");
							tds2 = tds2.replace(")", "");
							tds2 = tds2.replace(" ", "");
							String[] compounds = tds2.split(",");
							
							if (author.equals(compounds[0])) {
								String[] authorTokens = compounds[0].split("-");
								int first = toInt(authorTokens[1]);
								int second = toInt(compounds[1].split("-")[1]);
								
								min = Math.min(first, min);
								min = Math.min(second, min);
								max = Math.max(first, max);
								max = Math.max(second, max);
								
								authorMin = Math.min(first, authorMin);
								authorMin = Math.min(second, authorMin);
								authorMax = Math.max(first, authorMax);
								authorMax = Math.max(second, authorMax);
							}
									
						}  else if (tds2.startsWith("mark")) {
							tds2 = tds2.replaceFirst("mark", "");
							tds2 = tds2.replace("(", "");
							tds2 = tds2.replace(")", "");
							tds2 = tds2.replace(" ", "");
							String[] marks = tds2.split(",");
							marks = marks[1].split("-");
							
							for (int k = 0; k < MARKS.length; k++) {
								if (MARKS[k].equals(marks[0].toLowerCase())) {
									int mark = toInt(marks[1]);
									
									min = Math.min(mark, min);
									max = Math.max(mark, max);
								}
							}
						} else if (tds2.startsWith("appos")) {
							tds2 = tds2.replaceFirst("appos", "");
							tds2 = tds2.replace("(", "");
							tds2 = tds2.replace(")", "");
							tds2 = tds2.replace(" ", "");
							String[] apposes = tds2.split(",");
							
							if (apposes[0].equals(nsubjs[1])) {
								String tempAppos = apposes[1];
								StringBuffer apposBuffer = new StringBuffer();
								int tempApposPos = toInt(apposes[1].split("-")[1]);
								int apposMin = tempApposPos;
								int apposMax = tempApposPos;
								for (int k = 0; k < tdl.size(); k++) {
									String tds3 = tdl.get(k).toString();
									if (tds3.startsWith("compound")) {
										tds3 = tds3.replaceFirst("compound", "");
										tds3 = tds3.replace("(", "");
										tds3 = tds3.replace(")", "");
										tds3 = tds3.replace(" ", "");
										String[] compounds = tds3.split(",");
										
										if (compounds[0].equals(tempAppos)) {
											compounds = compounds[1].split("-");
											int pos = toInt(compounds[1]);
											apposMin = Math.min(pos, apposMin);
											apposMax = Math.max(pos, apposMax);
										}
									}
								}
								
								for (int k = apposMin; k <= apposMax; k++) {
									apposBuffer.append(tokens.get(k - 1) + " ");
								}
								
								min = Math.min(apposMin, min);
								max = Math.max(apposMax, max);
								
								appos = apposBuffer.toString().trim();
							}
						}
					}
					
					int reportingVerbsPosition = toInt(reportingVerbArray[1]);
					min = Math.min(min, reportingVerbsPosition);
					max = Math.max(max, reportingVerbsPosition);
					
					StringBuffer authorBuffer = new StringBuffer();
					for (int j = authorMin; j <= authorMax; j++) {
						authorBuffer.append(tokens.get(j - 1) + " ");
					}
					author = authorBuffer.toString().trim();
					
					

					
					
					//String reportingVerb = nsubjs[0];
					
					//String quote = text;
					//String quote = text.replace(author, "");
					//quote = quote.replace(reportingVerb, "");
					//quote = quote.replace("  ", " ");
					StringBuffer quoteBuffer = new StringBuffer();
					min--;
					max--;
					for (int j = 0; j < tokens.size(); j++) {
						if (j >= min && j <= max) {
							continue;
						}
						quoteBuffer.append(tokens.get(j) + " ");
					}
					String quote = quoteBuffer.toString().trim();
					
					authorQuote = new AuthorQuote();
					
					authorQuote.setAuthor(author);
					authorQuote.setQuote(quote);
					authorQuote.setDescription(appos);
					break;
				}
			}
		}
		return authorQuote;
	}
	
	public List<String> extractSentencesFromText(String text) {
		Reader reader = new StringReader(text);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		List<String> sentenceList = new ArrayList<String>();

		for (List<HasWord> sentence : dp) {
		   String sentenceString = Sentence.listToString(sentence);
		   sentenceList.add(sentenceString.toString());
		}

		return sentenceList;
	}
	
	public boolean isCJK(String str){
        int length = str.length();
        for (int i = 0; i < length; i++){
            char ch = str.charAt(i);
            Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)|| 
                Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)|| 
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)){
                return true;
            }
        }
        return false;
    }

	public static void main(String[] args) {
		/*
		String input = "a.in";
		String output = "a.out";
		
		if (args.length == 2) {
			input = args[0];
			output = args[1];
		} else if (args.length == 1) {
			input = args[0];
		}
		PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream(output));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		System.setOut(out);
		
		File file = new File(input);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		byte[] data = new byte[(int) file.length()];
		try {
			fis.read(data);
			fis.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		String text = null;
		try {
			text = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		*/
		
		Scanner sc = new Scanner(System.in);
		try {
			System.setOut(new PrintStream(System.out, true, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		TextAnalyzer analyzer = new TextAnalyzer();
		//List<String> sentences = analyzer.extractSentencesFromText(text);
		String line = null;
		while ((line = sc.nextLine()) != null) {
			System.err.println(line);
			
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			AuthorQuote quote = analyzer.getQuotes(line);
			if (quote != null) {
				String reply = "sentence: " + line + "\n"
							 + "author: " + quote.getAuthor() + "\n" + "quote: " + quote.getQuote() + "\n"
						 	 + "description: " + quote.getDescription() + "\n";
				System.out.println(reply);
				//System.err.println(reply);
			}
			// System.out.println(quote);
		}
		sc.close();
	}
}
