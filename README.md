# XPath Parser

__XPath Parser__ extracts [XPath expressions](https://www.w3.org/TR/xpath-30/)
from within [XQuery](https://www.w3.org/TR/xquery-30/) files or XML files, in
particular [XSLT](https://www.w3.org/TR/xslt-30/) files, and returns an XML
abstract syntax tree in [XQueryX](https://www.w3.org/TR/xqueryx-30/) for them.  
The program also provides facilities for checking this XQueryX output against
multiple [XML Schemas](https://www.w3.org/standards/techs/xmlschema) and
[RelaxNG Compact Schemas](http://relaxng.org/compact.html).

This software development was funded in part by the
[ANR PRODAQ](http://projects.lsv.ens-cachan.fr/prodaq/) project.


## Building

Build with [Apache Ant](http://ant.apache.org/) by running `ant compile`.
This will download [Jing](http://www.thaiopensource.com/relaxng/jing.html).

If you wish to modify the XQuery 3.0 grammar in the [xgrammar](xgrammar/)
directory, running `ant javacc` will download the necessary libraries and
regenerate the parser sources.


## Running

Use the provided [xpparser](xpparser) script.  Typical examples: 

```shell
./xpparser --xslt xgrammar/*.xsl --xsd xgrammar/xquery-3.0.xsd --rnc relaxng/xpath*.rnc
echo '//foo/bar' | ./xpparser --xquery xqx2xql.xsl
```

Run `./xpparser -h` for command-line usage. 


## License

Copyright (C) 2016 Sylvain Schmitz (ENS Cachan).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License in [LICENSE](LICENSE) or http://www.gnu.org/licenses/
for more details.

This software or document includes material copied from or derived
from the [XPath/XQuery Applets](https://www.w3.org/2013/01/qt-applets/)
and the [XQueryX 3.0 W3C Recommendation of 08 April
2014](https://www.w3.org/TR/2014/REC-xqueryx-30-20140408/).  Copyright
(C) 2013-2014 W3C(R) (MIT, ERCIM, Keio, Beihang).  See
[xgrammar/LICENSE](xgrammar/LICENSE) or
http://www.w3.org/Consortium/Legal/2015/copyright-software-and-document
for more details.