XPath Parser extracts XPath expressions from within XQuery or XML (in
particular XSLT) files, and returns an XML abstract syntax tree (in
XQueryX; see https://www.w3.org/TR/xqueryx-30/) for them.


# Building

Build with Apache Ant (http://ant.apache.org/) with `ant compile` or with
Apache Maven (http://maven.apache.org/) with `mvn compile`.


# Running

Use the provided `xpparser` script.  Typical examples: 

```shell
./xpparser --xslt sink/parser/*.xsl --validate xsd/*.xsd
./xpparser --xml '//@value' sink/tests/pathx1-tests.xml
echo '//foo/bar' | ./xpparser --xquery xqx2xql.xsl
```

See `./xpparser -h` for command-line usage. 


# Licence

Copyright (C) 2016 Sylvain Schmitz (ENS Cachan).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License in LICENSE or http://www.gnu.org/licenses/ for more
details.

This software or document includes material copied from or derived
from the XPath/XQuery Applets (https://www.w3.org/2013/01/qt-applets/)
and the XQueryX 3.0 W3C Recommendation of 08 April 2014
(https://www.w3.org/TR/2014/REC-xqueryx-30-20140408/).  Copyright
(C)2013-2014 W3C(R) (MIT, ERCIM, Keio, Beihang).  See LICENCE-W3C for more
details.