Compile and Run:
================

sudo java -DentityExpansionLimit=2147480000 -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 SearchEngine 						=> Creates index on 62 GB dump

java TitleIndexer => Creates Secondary index for DocumentID to Title map.

java Query => Takes query and gives top 10 results.