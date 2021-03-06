# XML Grammar

This directory contains the XML source for the XPath/XQuery family 
of grammars, and the XSLT files for a transformation to a test parser 
for [JavaCC/JJTree](https://javacc.java.net/) parser generation.

This is a tool for readers and parser implementers of XPath, XQuery,
XSLT and other languages using or extending the XPath grammar.  The
results should not be used as an absolute reference, and may flag
legal syntax or errors not intended by the drafts.  However, the
parser is generated by processing the [XML representation of the
grammar](xpath-grammar.xml), which is also used to produce the EBNF
productions in the XPath and XQuery specifications.  This parser is
also used by the working groups to validate the integrity of the
grammar.


## Building

Generate the Java source code for the XQuery 3.0 parser with [Apache
Ant](http://ant.apache.org) by running `ant javacc`.  Note that this
will download an 11MiB archive of various third-party dependencies.


## License

This software or document includes material copied from or derived
from the [XPath/XQuery Applets](https://www.w3.org/2013/01/qt-applets/)
Copyright (C) 2013 W3C(R) (MIT, ERCIM, Keio, Beihang).  See
[LICENSE](LICENSE) or
http://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
for more details.