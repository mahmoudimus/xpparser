XPath Parser extracts XPath expressions from within XQuery or XML (in
particular XSLT) files, and returns an XML abstract syntax tree (in
XQueryX) for them.

# Building

Build with Maven:

  mvn compile


# Running

Use the provided `xpparser` script.  The command-line is currently
undocumented, so ask me, or read `Main.java`.


# Licensing

Some files are Copyright (c) 2016 ENS Cachan and licensed under GPL
version 3.0 or later.

Most files are the XML sources for the XPath/XQuery family of
grammars, the XSLT files for a transformation to a test parser for
JavaCC/JJTree (https://javacc.dev.java.net/) parser generation.  They
are licensed as:

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
