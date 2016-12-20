These are the XML sources for the XPath/XQuery family of grammars, the
XSLT files for a transformation to a test parser for JavaCC/JJTree
(https://javacc.dev.java.net/) parser generation, and the generated
jars.


# Building

You need Ant and a JDK version >= 1.4 to build this software.  You
also need to download some libraries in order to compile this code, to
be put in `../lib`; grab for this
https://www.w3.org/2013/01/qt-applets/xgrammar_libs.zip .

Go to `parser` and run e.g. `ant xpath30.jar` or `ant xquery30.jar`.
The resulting files are in `parser/applet/`.


# Running

The generated jars can validate either an expression on the
command-line, as in e.g.

  java -jar applet.jar -xqueryx -expr 'for $a in foo return $a[@bar]'

or an expression from a file, as in e.g.

  java -jar applet.jar -xqueryx -file foo.xql


# Licensing

All files except for `parser/XQueryXConvertBase-xpath30.java' are

Copyright (c) 2005 W3C(r) (http://www.w3.org/) (MIT
(http://www.lcs.mit.edu/), INRIA (http://www.inria.fr/), Keio
(http://www.keio.ac.jp/)).  All Rights Reserved.  See
http://www.w3.org/Consortium/Legal/ipr-notice-20000612#Copyright.

W3C liability
(http://www.w3.org/Consortium/Legal/ipr-notice-20000612#Legal_Disclaimer),
trademark
(http://www.w3.org/Consortium/Legal/ipr-notice-20000612#W3C_Trademarks),
document use
(http://www.w3.org/Consortium/Legal/copyright-documents-19990405), and
software licensing rules
(http://www.w3.org/Consortium/Legal/copyright-software-19980720)
apply.
