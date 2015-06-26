# QuotesExtracter
Goal: Extract quotes from famous people in the online newspaper.


We extract the quotes following the rules below:

Rule 1 : The Colon ":" Rule

If the sentence contains the colon ":" or the Chinese colon "：", then

the sentence will be parsed using this rule.

Assume the sentence looks like this: 

A : B

-- If both the length of A and length of B is more than 4 words, then

nothing will return

-- If the length of A > the length of B,

then B is the name, A is the quotes

-- If the length of B  >= the length of A,

then A is the name, B is the quotes

Rule 2 : Stanford Parser (English)

If the sentence has no Chinese characters, then the sentence will be

parsed using this rule.

1) The sentence will be tokenized.

2) Determined the typed dependency (universal dependency) using Stanford

Parser

3) If it contains the typed dependency "nsubj", ie: nsubj(word1, word2)

If word1 can be found in reportingVerbs.dictionary,

then word2 is the name.

4) If it contains the typed dependency "compound", and it contains word2

in "3)" then we can find out the full name

5) If it contains the typed dependency "mark", means the sentence might 

contain "that/to" such as he said that, so we need to eliminate that "that"

inside the quotes

6) If it contains the typed dependency "aux", means the sentence might

contain the word "have" such as have said, such verb need to be eliminated

7) If it contains the typed dependency "appos", means that the 

sentence might contain description

8) After extracting the quote, name and description, the parsing is done.

Rule 3 : Stanford Parser (Chinese)

If the sentence contains any chinese characters, it will be parsed using 

this rule.
 
1) The sentence will be tokenized using Stanford segmenter.

2) Determined the typed dependency (universal dependency) using Stanford

Parser

3) If it contains the typed dependency "nsubj", ie: nsubj(word1, word2)

If word1 can be found in reportingVerbs.dictionary,

then word2 is the name.

4) If it contains the typed dependency "nn", and it contains word2

in "3)" then we can find out the full name
 

 
** These rules might not be perfect and will be enhanced by time.
 
Reference:

Stanford Parser: http://nlp.stanford.edu/software/lex-parser.shtml

Stanford Word Segmenter: http://nlp.stanford.edu/software/segmenter.shtml
