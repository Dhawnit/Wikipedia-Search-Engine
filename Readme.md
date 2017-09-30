# Wikipedia-Search-Engine
Wikipedia search engine was implemented over a wiki dump of around 64 GB. Key challenge in this project was to implement multi level data indexing to provide on demand search results in memory through disk reads.

Features included:
1. XML Parsing - Used default SAX parser from Java SE. 
2. Tokenization - Hand-coded tokenizer (without using regular expressions)
3. Case folding - All tokens changed to lower case.
4. Stop words removal - Wordnet (http://www.d.umn.edu/~tpederse/Group01/WordNet/words.txt)
5. Stemming - Porter stemmer (http://tartarus.org/martin/PorterStemmer/java.txt)
6. Posting List / Inverted Index creation 
7. Fetch documents by query (Tfidf rank)

Term Field Abbreviations:
I - Infobox
B - Body
T - Title
L - External Link
R - References
C - Category

### Compile and Run:
Compile all .java files and run as below
```
sudo java -DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 SearchEngine 						=> Creates index on 62 GB dump

java TitleIndexer => Creates Secondary index for DocumentID to Title map.

java Query => Takes query and gives top 10 results.
```
