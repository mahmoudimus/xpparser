/*
Copyright (C) 2016 Sylvain Schmitz (ENS Cachan).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License in `LICENCE-GPL` for more details.

This software or document includes material copied from or derived
from the XPath/XQuery Applets
(https://www.w3.org/2013/01/qt-applets/), in particular on the file
`XQueryXConverterBase-xquery30.java`.  Copyright (C)2013 W3C(R) (MIT,
ERCIM, Keio, Beihang).  See `LICENSE-W3C` for more details.
 */
package fr.lsv.xpparser;

import java.util.Stack;
import javax.xml.XMLConstants;
import org.w3c.dom.*;
import org.w3c.xqparser.*;

/**
 * Transforms an XPath AST into an XQueryX DOM tree.  In spite of its
 * name, this class can actually handle any XQuery AST.
 */
public class XPathXConverter implements XParserTreeConstants {

    private Stack<SimpleNode> _openXMLElemStack;

    /**
     * The default prefix name for XQueryX is "xqx".
     */
    public static final String XQX_NS_PREFIX = "xqx";

    /**
     * The default URI for the XQueryX namespace.
     */
    public static final String XQX_NS_URI =
        "http://www.w3.org/2005/XQueryX";

    // There's nothing to do.  I might make the whole thing static.
    public XPathXConverter() {
        _openXMLElemStack = new Stack<SimpleNode>();
    }
    
    protected String xqxElementName(int id)
    // Note that this is inappropriate for some values of 'id'.
    // The caller is responsible for knowing if it's appropriate.
    {
        String node_name = jjtNodeName[id];
        String xqx_element_name = 
            XQX_NS_PREFIX + ":"
            + node_name.substring(0, 1).toLowerCase()
            + node_name.substring(1);
        return xqx_element_name;
    }

    protected String undelimitStringLiteral(SimpleNode stringLiteral_node) {
        assert stringLiteral_node.id == JJTSTRINGLITERAL;
        return undelimitStringLiteral(stringLiteral_node.m_value);
    }

    protected String undelimitStringLiteral(String lit) {
        char delimiter = lit.charAt(0);
        assert (lit.charAt( lit.length() - 1 ) == delimiter);
        String body = lit.substring(1, lit.length()-1);

        StringBuilder body_sans_escapes = new StringBuilder();
        int body_len = body.length();
        for (int i = 0; i < body_len; i++)
        {
            char c = body.charAt(i);
            if (c == delimiter)        {
                // The grammar guarantees that within the body of the
                // StringLiteral, the delimiter character only appears
                // in pairs.
                assert (i + 1) < body_len;
                assert body.charAt(i + 1) == delimiter;

                // For this pair, we want only one character in
                // body_sans_escapes.  The easiest way to do this is
                // increment i, thus skipping over the second
                // character of the pair.
                i++;
            }
            body_sans_escapes.append(c);
        }

        return body_sans_escapes.toString();
    }

    /* Merged from XQueryXConverter.java and
     * XQueryXConverterBase_xquery30.java. */
    protected void transformName(SimpleNode namenode,
                                 Element parent,
                                 String xname) {
        Document doc = parent.getOwnerDocument();
        if (   namenode.id != JJTNCNAME
            && namenode.id != JJTQNAME
            && namenode.id != JJTFUNCTIONQNAME
            && namenode.id != JJTTAGQNAME
            && namenode.id != JJTURIQUALIFIEDNAME) {
            parent.appendChild(doc.createComment
                               ("transformName got unexpected " 
                                + namenode.toString() 
                                + " for namenode."));
            // But proceed anyway.
        }
            
        String qname = namenode.m_value;
        assert qname != null;
        
        Element e = doc.createElementNS(XQX_NS_URI, xname);
        parent.appendChild(e);
            
        String localname;
        int i;
        if (namenode.id == JJTURIQUALIFIEDNAME) {
            i             = qname.lastIndexOf('}');
            String uri    = qname.substring(2, i);
            localname     = qname.substring(i + 1);
            e.setAttributeNS(XQX_NS_URI, "xqx:URI", uri);
        } else if ((i = qname.indexOf(':')) > 0) {
            String prefix = qname.substring(0, i);
            localname     = qname.substring(i + 1);
            e.setAttributeNS(XQX_NS_URI, "xqx:prefix", prefix);
        } else {
            localname     = qname;
        }
        e.appendChild(doc.createTextNode(localname));
    }

    protected void transformName
        (SimpleNode namenode, Element parent, int id) {

        transformName(namenode, parent, xqxElementName(id));
    }

    
    protected int transformClauseItem(SimpleNode parent,
                                      Element e,
                                      boolean is_for, int i) {
        Document doc = e.getOwnerDocument();
        Element ci = doc.createElementNS
            (XQX_NS_URI, is_for ? "xqx:forClauseItem" : "xqx:letClauseItem");
        e.appendChild(ci);

        SimpleNode nextChild = parent.getChild(i);
        assert nextChild.id == JJTVARNAME;

        Element tvb = doc.createElementNS
            (XQX_NS_URI, "xqx:typedVariableBinding");
        ci.appendChild(tvb);

        transformName(nextChild.getChild(0), tvb, nextChild.id);
        i++;
        nextChild = parent.getChild(i);
        
        if (nextChild.id == JJTTYPEDECLARATION) {
            transform(nextChild, tvb);
            i++;
            nextChild = parent.getChild(i);
        }
        // end typedVariableBinding

        if (nextChild.id == JJTALLOWINGEMPTY) {
            assert is_for;
            transformChildren(parent, ci, i, i);
            i++;
            nextChild = parent.getChild(i);
        }

        if (nextChild.id == JJTPOSITIONALVAR) {
            assert is_for;
            transformChildren(parent, ci, i, i);
            i++;
            nextChild = parent.getChild(i);
        }

        Element expr = doc.createElementNS
            (XQX_NS_URI, is_for ? "xqx:forExpr" : "xqx:letExpr");
        ci.appendChild(expr);
        transform(nextChild, expr);
        i++;
        return i;
    }

    protected void transformChildren(SimpleNode node, Element parent) {
        this.transformChildren
            (node, parent, 0, node.jjtGetNumChildren() - 1);
    }

    protected void transformChildren(SimpleNode node, Element parent,
                                  int start) {
        this.transformChildren(node, parent, start, 
                               node.jjtGetNumChildren() - 1);
    }

    protected void transformChildren(SimpleNode node, Element parent,
                                     int start, int end) {
        assert(end <  node.jjtGetNumChildren());
        for (int i = start; i <= end; i++)
        {
            SimpleNode child = node.getChild(i);
            this.transform(child, parent);
        }
    }

    protected void filterPredicate(SimpleNode node, Element parent,
                                   int start, int end) {

        Document doc = parent.getOwnerDocument();
        int predicates = 0;

        for (int j = end; node.getChild(j).id == JJTPREDICATE; j--)
            predicates++;

        Element e = doc.createElementNS(XQX_NS_URI, "xqx:filterExpr");
        parent.appendChild(e);
        dynamicFunctionInvocation(node, e, start, end - predicates);

        if (predicates != 0) {
            e = doc.createElementNS(XQX_NS_URI, "xqx:predicates");
            parent.appendChild(e);
            transformChildren(node, e, end - predicates + 1, end);
        }
    }

    protected void dynamicFunctionInvocation(SimpleNode node, 
                                             Element parent,
                                             int start, int end) {

        Document doc = parent.getOwnerDocument();
        SimpleNode child = node.getChild(start);

        if (end == start) {
           if (child.id == JJTPARENTHESIZEDEXPR) {
               Element e = doc.createElementNS(XQX_NS_URI,
                                               "xqx:sequenceExpr");
               parent.appendChild(e);
               transform(child, e);
           }
           else
               transform(child, parent);
        }
        else {
            SimpleNode dfi = node.getChild(end--);

            int predicates = 0;
            for (int j = end; node.getChild(j).id == JJTPREDICATE; j--)
                predicates++;

            Element e = doc.createElementNS
                (XQX_NS_URI, "xqx:dynamicFunctionInvocationExpr");
            parent.appendChild(e);
            
            Element e1 = doc.createElementNS
                (XQX_NS_URI, "xqx:functionItem");
            e.appendChild(e1);
            dynamicFunctionInvocation(node, e1, start, end - predicates);

           if (predicates != 0) {
               e1 = doc.createElementNS(XQX_NS_URI, "xqx:predicates");
               e.appendChild(e1);
               transformChildren(node, e1, end - predicates + 1, end);
           }
           
           if (dfi.jjtGetNumChildren() != 0) {
               e1 = doc.createElementNS(XQX_NS_URI, "xqx:arguments");
               e.appendChild(e1);
               transformChildren(dfi, e1);
           }
        }
    }


    // -------------------------------------------------------

    static final int BSP_STRIP = 0;

    static final int BSP_PRESERVE = 1;

    int _boundarySpacePolicy = BSP_STRIP;

    protected boolean isPreviousSiblingBoundaryWhitespaceChar(SimpleNode node) {
        node = getPreviousSibling(node.getParent());
        if (node == null)
            return true;
        if (node.jjtGetNumChildren() > 0)
            node = node.getChild(0);
        if (node.id == JJTCDATASECTION)
            return false;
        else if (node.id == JJTELEMENTCONTENTCHAR
                || node.id == JJTQUOTATTRCONTENTCHAR
                || node.id == JJTAPOSATTRCONTENTCHAR) {
            if (node.m_value.trim().length() == 0) {
                return isPreviousSiblingBoundaryWhitespaceChar(node);
            }
        } else {
            if (node.id == JJTCOMMONCONTENT) {
                node = node.getChild(0);
                if (node.id == JJTLCURLYBRACEESCAPE
                        || node.id == JJTRCURLYBRACEESCAPE
                        || node.id == JJTCHARREF
                        || node.id == JJTPREDEFINEDENTITYREF)
                    return false;
            }
            return true;
        }
        return false;
    }

    protected boolean isNextSiblingBoundaryWhitespaceChar(SimpleNode node) {
        node = getNextSibling(node.getParent());
        if (node == null)
            return true;
        if (node.jjtGetNumChildren() > 0)
            node = node.getChild(0);
        if (node.id == JJTCDATASECTION)
            return false;
        else if (node.id == JJTELEMENTCONTENTCHAR
                || node.id == JJTQUOTATTRCONTENTCHAR
                || node.id == JJTAPOSATTRCONTENTCHAR) {
            if (node.m_value.trim().length() == 0) {
                return isNextSiblingBoundaryWhitespaceChar(node);
            }
        } else {
            if (node.id == JJTCOMMONCONTENT) {
                node = node.getChild(0);
                if (node.id == JJTLCURLYBRACEESCAPE
                        || node.id == JJTRCURLYBRACEESCAPE
                        || node.id == JJTCHARREF
                        || node.id == JJTPREDEFINEDENTITYREF)
                    return false;
            }
            return true;
        }
        return false;
    }

    protected boolean isBoundaryWhitespaceChar(SimpleNode node) {
        if (node.id == JJTCDATASECTION)
            return false;
        if (node.id == JJTELEMENTCONTENTCHAR
                || node.id == JJTQUOTATTRCONTENTCHAR
                || node.id == JJTAPOSATTRCONTENTCHAR) {
            if (node.m_value.trim().length() == 0) {
                if (isPreviousSiblingBoundaryWhitespaceChar(node)
                        && isNextSiblingBoundaryWhitespaceChar(node)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean shouldStripChar(SimpleNode node) {
        if ((_boundarySpacePolicy == BSP_STRIP)
                && isBoundaryWhitespaceChar(node))
            return true;
        else
            return false;
    }

    protected Element simpleElement(Element parent,
                                    String qname,
                                    String text) {
        Document doc = parent.getOwnerDocument();
        Element e = doc.createElementNS(XQX_NS_URI, qname);
        if (text != null && !text.isEmpty()) {
            e.appendChild(doc.createTextNode(text));
        }
        parent.appendChild(e);
        return e;
    }

    protected Element emptyElement(Element parent, String qname) {
        return simpleElement(parent, qname, null);
    }              

    /**
     * Recursively convert AST nodes into DOM Elements and append
     * the result to its parent.
     * @param node   The node to be processed.
     * @param parent Where to append the result.
     */
    public void transform(final SimpleNode node, Element parent) {
        // information that we'll probably need:
        int id = node.id;
        String qname = xqxElementName(id);
        Document doc = parent.getOwnerDocument();
        int n = node.jjtGetNumChildren();

        // the big switch
        switch (id) {
        case JJTSTART:
        case JJTMODULE:
        case JJTMAINMODULE:
        case JJTQUERYLIST:
        case JJTQUERYBODY:
            transformChildren(node, parent);
            break;
            
        case JJTVERSIONDECL: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            
            // The children of the VERSIONDECL node are:
            // one for each StringLiteral, and one for Separator.
            assert (node.getChild(n-1).id == JJTSEPARATOR);
            
            if (node.m_value.equals("encoding")) {
                // VersionDecl -> "xquery" "encoding" StringLiteral
                //                Separator
                simpleElement(e, "xqx:encoding",
                              undelimitStringLiteral(node.getChild(0)));
                assert n == 2;
            }
            else if (node.m_value.equals("version")) {
                // VersionDecl -> "xquery" "version" StringLiteral
                //                ("encoding" StringLiteral)? Separator
                simpleElement(e, "xqx:version",
                              undelimitStringLiteral(node.getChild(0)));

                if (n == 2) {
                    e.appendChild(doc.createComment("encoding: " + "null"));
                }
                else if (n == 3) {
                    simpleElement(e, "xqx:encoding",
                                  undelimitStringLiteral(node.getChild(1)));
                }
                else {
                    assert false;
                }
            }
            else {
                assert false;
            }

            break;
        }

        case JJTINTEGERLITERAL:
        case JJTDECIMALLITERAL:
        case JJTDOUBLELITERAL:
        case JJTSTRINGLITERAL: {
                if (id == JJTSTRINGLITERAL 
                    && node.getParent().id == JJTOPTIONDECL) {
                    simpleElement(parent, "xqx:optionContents",
                                  undelimitStringLiteral(node));
                    break;
                } else if (id == JJTSTRINGLITERAL
                           && node.getParent().id == JJTDECIMALFORMATDECL) {
                    simpleElement(parent, "xqx:decimalFormatParamValue",
                                  undelimitStringLiteral(node));
                    break;
                }

                switch (id) {
                case JJTINTEGERLITERAL:
                    qname = "xqx:integerConstantExpr";
                    break;
                case JJTDECIMALLITERAL:
                    qname = "xqx:decimalConstantExpr";
                    break;
                case JJTDOUBLELITERAL:
                    qname = "xqx:doubleConstantExpr";
                    break;
                case JJTSTRINGLITERAL:
                    qname = "xqx:stringConstantExpr";
                    break;
                default:
                    assert false;
                    break;
                }

                String content =
                    (id == JJTSTRINGLITERAL)
                    ? undelimitStringLiteral(node)
                    : node.m_value;

                Element e = doc.createElementNS(XQX_NS_URI, qname);
                parent.appendChild(e);
                simpleElement(e, "xqx:value", content);
                break;
            }
            
        case JJTBASEURIDECL:
            // handled in JJTBASEURIDECL
            transformChildren(node, parent);
            break;

        case JJTURILITERAL: {
            if (node.getParent().id == JJTBASEURIDECL)
                qname = "xqx:baseUriDecl";
            else if (node.getParent().id == JJTORDERMODIFIER)
                qname = "xqx:collation";
            else if (node.getParent().id == JJTGROUPINGSPEC)
                qname = "xqx:collation";
            else if (node.getParent().id == JJTDEFAULTCOLLATIONDECL)
                qname = "xqx:defaultCollationDecl";
            else if (node.getParent().id == JJTMODULEIMPORT) {
                SimpleNode firstChild = node.getParent().getChild(0);
                if ((firstChild == node)
                    || (firstChild.id == JJTNCNAME &&
                        ((node.getParent().getChild(1)) == node)))
                    qname = "xqx:targetNamespace";
                else
                    qname = "xqx:targetLocation";
            } else
                qname = "xqx:uri";
            SimpleNode child = node.getChild(0);
            simpleElement(parent, qname, undelimitStringLiteral(child));
            break;
        }

        case JJTNAMEDFUNCTIONREF: {
            Element e = doc.createElementNS(XQX_NS_URI,
                                            "xqx:namedFunctionRef");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTPROLOG: {
            if (node.jjtGetNumChildren() > 0) {
                Element e = doc.createElementNS(XQX_NS_URI,
                                                "xqx:prolog");
                parent.appendChild(e);
                transformChildren(node, e);
            }
            break;
        }

        case JJTURIQUALIFIEDSTAR: {
            String uri_qualified_star = node.m_value;
            int i = uri_qualified_star.lastIndexOf('}');
            String uri = uri_qualified_star.substring(2, i);
            String star = uri_qualified_star.substring(i + 1);
            assert star.equals("*");
            simpleElement(parent, "xqx:uri", uri);
            parent.appendChild(doc.createElementNS(XQX_NS_URI, "xqx:star"));
            break;
        }

        case JJTNCNAMECOLONSTAR: {
            String ncname_colon_star = node.m_value;
            int i = ncname_colon_star.indexOf(':');
            String ncname = ncname_colon_star.substring(0, i);
            String star = ncname_colon_star.substring(i + 1);
            assert star.equals("*");
            simpleElement(parent, "xqx:NCName", ncname);
            parent.appendChild(doc.createElementNS(XQX_NS_URI, "xqx:star"));
            break;
        }

        case JJTSTARCOLONNCNAME: {
            String star_colon_ncname = node.m_value;
            int i = star_colon_ncname.indexOf(':');
            String star = star_colon_ncname.substring(0, i);
            String ncname = star_colon_ncname.substring(i + 1);
            assert star.equals("*");
            parent.appendChild(doc.createElementNS(XQX_NS_URI, "xqx:star"));
            simpleElement(parent, "xqx:NCName", ncname);
            break;
        }

        case JJTSINGLETYPE: {
            boolean optionality = (node.m_value != null);
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e);
            if (optionality)
                e.appendChild(doc.createElementNS(XQX_NS_URI, "xqx:optional"));
            break;
        }

        case JJTPRAGMAOPEN:
        case JJTPRAGMACLOSE:
            // No Action
            break;

        case JJTATOMICORUNIONTYPE:
            // handled in JJTQNAME
            transformChildren(node, parent);
            break;
            
        case JJTATTRIBNAMEORWILDCARD:
        case JJTELEMENTNAMEORWILDCARD:
            if (node.m_value != null && node.m_value.equals("*")) {
                qname = xqxElementName
                    ((id == JJTATTRIBNAMEORWILDCARD)
                     ? JJTATTRIBUTENAME
                     : JJTELEMENTNAME);
                Element e = doc.createElementNS(XQX_NS_URI, qname);
                parent.appendChild(e);
                e.appendChild(doc.createElementNS(XQX_NS_URI, "xqx:star"));
                break;
            } else {
                transformChildren(node, parent);
                break;
            }

        case JJTATTRIBUTENAME:
        case JJTELEMENTNAME: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

            // case JJTTYPENAME:
        case JJTURIQUALIFIEDNAME:
        case JJTNCNAME:
        case JJTQNAME: {
            int pid = node.getParent().id;
            if (pid == JJTCOMPELEMCONSTRUCTOR 
                || pid == JJTCOMPATTRCONSTRUCTOR)
                    qname = "xqx:tagName";
            else if (pid == JJTTYPENAME) {
                // The TypeName's parent could be:
                //  - a ValidateExpr, an AttributeTest, or an ElementTest,
                //    for which xqx:typeName is the correct result,
                //  or
                //  - a SimpleTypeName (in a SingleType), and
                //    backwards-compatibility for xqx:singleType means
                //    that the correct result is xqx:atomicType.
                if (node.getParent().getParent().id == JJTSIMPLETYPENAME)
                    qname = "xqx:atomicType";
                else
                    qname = "xqx:typeName";
            }
            else if (pid == JJTCOMPPICONSTRUCTOR)
                qname = "xqx:piTarget";
            else if (pid == JJTATOMICORUNIONTYPE) // XXX
                qname = "xqx:atomicType";
            else if (pid == JJTNAMESPACEDECL)
                qname = "xqx:prefix";
            else if (pid == JJTVARDECL || pid == JJTPARAM)
                qname = "xqx:varName";
            else if (pid == JJTFUNCTIONDECL || pid == JJTFUNCTIONCALL
                     || pid == JJTNAMEDFUNCTIONREF )
                qname = "xqx:functionName";
            else if (pid == JJTOPTIONDECL)
                qname = "xqx:optionName";
            else if (pid == JJTOPTIONDECL)
                qname = "xqx:optionName";
            else if (pid == JJTMODULEIMPORT)
                qname = "xqx:namespacePrefix";
            else if (pid == JJTPRAGMA)
                qname = "xqx:pragmaName";
            else if (pid == JJTCURRENTITEM)
                qname = "xqx:currentItem";
            else if (pid == JJTNEXTITEM)
                qname = "xqx:nextItem";
            else if (pid == JJTPREVIOUSITEM)
                qname = "xqx:previousItem";
            else if (pid == JJTDECIMALFORMATDECL)
                qname = "xqx:decimalFormatName";
            else if (pid == JJTANNOTATION)
                qname = "xqx:annotationName";
            else
                qname = "xqx:QName";
            transformName(node, parent, qname);
            break;
        }

        case JJTENDTAGQNAME: {
            SimpleNode openTag = _openXMLElemStack.peek();
            if(!openTag.getValue().equals(node.getValue()))
                throw new PostParseException("In a direct element constructor, the name used in the end tag must exactly match the name used in the corresponding start tag, including its prefix or absence of a prefix.");
            break;
        }

        case JJTTAGQNAME: {
            if (node.getParent().id == JJTDIRATTRIBUTELIST
                && isNamespaceDecl(node)) {
                if(!node.m_value.equals("xmlns")){
                    int i = node.m_value.indexOf(':');
                    String prefix = node.m_value.substring(i + 1);
                    simpleElement(parent, "xqx:prefix", prefix);
                }
                break;
            }

            if (node.getParent().id == JJTDIRATTRIBUTELIST) {
                qname = "xqx:attributeName";
            } else {
                qname = "xqx:tagName";
            }
            transformName(node, parent, qname);
            break;
        }

        case JJTFUNCTIONQNAME:
            transformName(node, parent, "xqx:functionName");
            break;

        case JJTWILDCARD: {
            Element e = doc.createElementNS(XQX_NS_URI, "xqx:Wildcard");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTNAMETEST:
            if (node.getChild(0).id == JJTWILDCARD)
                transformChildren(node, parent);
            else
                transformName(node.getChild(0), parent, "xqx:nameTest");
            break;

        case JJTVARNAME: {
            int pid = node.getParent().id;
            if (/* pid == JJTCASECLAUSE || */
                pid == JJTTYPESWITCHEXPR && getNextSibling(node) != null) {
                transformName(node.getChild(0), parent, "xqx:variableBinding");
            } else if (pid == JJTVARDECL || pid == JJTGROUPINGVARIABLE) {
                transformName(node.getChild(0), parent, "xqx:varName");
            } else {
                Element e = doc.createElementNS(XQX_NS_URI, "xqx:varRef");
                parent.appendChild(e);
                transformName(node.getChild(0), e, "xqx:name");
            }
            break;
        }

        case JJTPOSITIONALVAR:
            transformName(node.getChild(0).getChild(0), parent,
                           "xqx:positionalVariableBinding");
            break;

        case JJTITEMTYPE:
            if (node.m_value != null && node.m_value.equals("item"))
                emptyElement(parent, "xqx:anyItemType");
            else
                transformChildren(node, parent);
            break;

        case JJTSEQUENCETYPE: {
            int pid = node.getParent().id;
            boolean shouldBeSeqType = 
                (pid == JJTSEQUENCETYPEUNION || pid == JJTTYPEDFUNCTIONTEST);
            Element e = parent;
            if (shouldBeSeqType) {
                e = doc.createElementNS(XQX_NS_URI, "xqx:sequenceType");
                parent.appendChild(e);                
            }
            else if (pid == JJTFUNCTIONDECL) {
                e = doc.createElementNS(XQX_NS_URI, "xqx:typeDeclaration");
                parent.appendChild(e);
            }

            if (node.m_value != null && node.m_value.equals("empty-sequence"))
                emptyElement(e, "xqx:voidSequenceType");
            else
                transformChildren(node, e);
            break;
        }

        case JJTEXPR: {
            Element e = parent;
            if (getNumExprChildren(node) > 1) {
                e = doc.createElementNS(XQX_NS_URI, "xqx:sequenceExpr");
                parent.appendChild(e);
            }
            transformChildren(node, e);
            break;
        }

        case JJTSIMPLEMAPEXPR: {
            // XQueryConverterBase-xquery30 binarizes this, but the
            // most recent schema for XqueryX allows unboundedly many
            // children.
            Element e = doc.createElementNS(XQX_NS_URI, "xqx:simpleMapExpr");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTPATHEXPR: {
            // First, check whether we can translate this node
            // without an xqx:pathExpr element.
            // If the PathExpr's parent is a SimpleMapExpr,
            // then we can't shortcut, because xqx:simpleMapExpr's children
            // must be xqx:pathExprs.
            if (node.getParent().id != JJTSIMPLEMAPEXPR && n == 1) {
                SimpleNode only_child = node.getChild(0);
                if (only_child.id == JJTPOSTFIXEXPR 
                    && only_child.jjtGetNumChildren() == 1) {
                    SimpleNode only_grandchild = only_child.getChild(0);
                    // only_grandchild is some kind of PrimaryExpr node,
                    // so it doesn't need to be in a pathExpr.
                    transform(only_grandchild, parent);
                    break;
                }
            }

            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);

            qname = "xqx:stepExpr";
            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);
                if (child.id == JJTSLASH) {
                    if (i == 0)
                        emptyElement(e, "xqx:rootExpr");
                }
                else if (child.id == JJTSLASHSLASH) {
                    if (i == 0)
                        emptyElement(e, "xqx:rootExpr");
                    Element e1;
                    e1 = doc.createElementNS(XQX_NS_URI, qname);
                    e.appendChild(e1);

                    simpleElement(e1, "xqx:xpathAxis", "descendant-or-self");
                    emptyElement(e1, "xqx:anyKindTest");
                }
                else {
                    assert child.id == JJTAXISSTEP 
                        || child.id == JJTPOSTFIXEXPR;
                    Element e1 = doc.createElementNS(XQX_NS_URI, qname);
                    e.appendChild(e1);
                    transform(child, e1);
                }
            }
            break;
        }
            
        case JJTPREDICATELIST: {
            Element e = doc.createElementNS(XQX_NS_URI, "xqx:predicates");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTFUNCTIONCALL: {
            Element e = doc.createElementNS(XQX_NS_URI,
                                            "xqx:functionCallExpr");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTARGUMENTLIST: {
            Element e = doc.createElementNS(XQX_NS_URI, "xqx:arguments");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTARGUMENT:
            transformChildren(node, parent);
            break;

        case JJTARGUMENTPLACEHOLDER:
            emptyElement(parent, "xqx:argumentPlaceholder");
            break;

        case JJTPOSTFIXEXPR:
            filterPredicate(node, parent, 0, n - 1);
            break;

        case JJTAXISSTEP:
            transformChildren(node, parent);
            break;

        case JJTFORWARDAXIS:
        case JJTREVERSEAXIS:
            simpleElement(parent, "xqx:xpathAxis", node.m_value);
            break;

        case JJTABBREVREVERSESTEP:
            simpleElement(parent, "xqx:xpathAxis", "parent");
            emptyElement(parent, "xqx:anyKindTest");
            break;

        case JJTABBREVFORWARDSTEP: {
            //  AbbrevForwardStep ::= "@"? NodeTest
            SimpleNode nodeTest = node.getChild(0);
            String optionalAttribIndicator = node.m_value;
            if (optionalAttribIndicator != null
                && optionalAttribIndicator.equals("@")) {
                // "The attribute axis attribute:: can be abbreviated by @."
                simpleElement(parent, "xqx:xpathAxis", "attribute");
            } else {
                // "If the axis name is omitted from an axis step, the
                // default axis is child, with two exceptions: if the
                // axis step contains an AttributeTest or
                // SchemaAttributeTest then the default axis is
                // attribute; if the axis step contains
                // namespace-node() then the default axis is
                // namespace."
                SimpleNode nodeTestChild = nodeTest.getChild(0);
                if (   nodeTestChild.id == JJTATTRIBUTETEST
                    || nodeTestChild.id == JJTSCHEMAATTRIBUTETEST)
                    simpleElement(parent, "xqx:xpathAxis", "attribute");
                else if (nodeTestChild.id == JJTNAMESPACENODETEST)
                    simpleElement(parent, "xqx:xpathAxis", "namespace");
                else
                    simpleElement(parent, "xqx:xpathAxis", "child");
            }
            // The NodeTest within the AbbrevForwardStep:
            transform(nodeTest, parent);
            break;
        }

        case JJTQUANTIFIEDEXPR: {
            Element qe = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(qe);
            
            simpleElement(qe, "xqx:quantifier", node.m_value);

            for (int i = 0; i < n - 1;) {
                SimpleNode typedVariableBinding = node.getChild(i);
                i++;

                Element qeic = doc.createElementNS
                    (XQX_NS_URI, "xqx:quantifiedExprInClause");
                qe.appendChild(qeic);

                Element tvb = doc.createElementNS
                    (XQX_NS_URI, "xqx:typedVariableBinding");
                qeic.appendChild(tvb);

                transformName(typedVariableBinding.getChild(0),
                              tvb, typedVariableBinding.id);

                SimpleNode nextChild = node.getChild(i);
                i++;

                if (nextChild.id == JJTTYPEDECLARATION) {
                    transform(nextChild, tvb);
                    nextChild = node.getChild(i);
                    i++;
                }
                // end typedVariableBinding

                Element se = doc.createElementNS
                    (XQX_NS_URI, "xqx:sourceExpr");
                qeic.appendChild(se);
                transform(nextChild, se);

                // end sourceExpr
                // end quantifiedExprInClause

            }
            
            Element pe = doc.createElementNS(XQX_NS_URI,
                                             "xqx:predicateExpr");
            qe.appendChild(pe);
            transformChildren(node, pe, n - 1, n - 1);
            // end predicateExpr
            // quantifierExpr
            break;
        }

        case JJTCATCHCLAUSE: {
            Element cc = doc.createElementNS(XQX_NS_URI, "xqx:catchClause");
            parent.appendChild(cc);

            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);
                if (child.id == JJTEXPR) {
                    Element ce = doc.createElementNS
                        (XQX_NS_URI, "xqx:catchExpr");
                    cc.appendChild(ce);
                    transform(child, ce);
                }
                else
                    transform(child, cc);
            }
            break;
        }

        case JJTIFEXPR: {
            Element ite = doc.createElementNS
                (XQX_NS_URI, "xqx:ifThenElseExpr");
            parent.appendChild(ite);

            Element ic = doc.createElementNS
                (XQX_NS_URI, "xqx:ifClause");
            ite.appendChild(ic);
            transformChildren(node, ic, 0, 0);

            Element tc = doc.createElementNS
                (XQX_NS_URI, "xqx:thenClause");
            ite.appendChild(tc);
            transformChildren(node, tc, 1, 1);

            Element ec = doc.createElementNS
                (XQX_NS_URI, "xqx:elseClause");
            ite.appendChild(ec);
            transformChildren(node, ec, 2, 2);
            break;
        }

        case JJTFLWOREXPR11: {
            Element e = doc.createElementNS(XQX_NS_URI, "xqx:flworExpr");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTINITIALCLAUSE:
        case JJTINTERMEDIATECLAUSE: {
            transformChildren(node, parent);
            break;
        }

        case JJTRANGEEXPR: {
            Element rse = doc.createElementNS
                (XQX_NS_URI, "xqx:rangeSequenceExpr");
            parent.appendChild(rse);
            Element e = doc.createElementNS
                (XQX_NS_URI, "xqx:startExpr");
            rse.appendChild(e);
            transform(node.getChild(0), e);

            e = doc.createElementNS
                (XQX_NS_URI, "xqx:endExpr");
            rse.appendChild(e);
            transform(node.getChild(1), e);
            break;
        }
         
        case JJTUNARYEXPR: {
            Element e = parent;
            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);
                if (child.id == JJTPLUS) {
                    Element e1 = doc.createElementNS
                        (XQX_NS_URI, "xqx:unaryPlusOp");
                    e.appendChild(e1);
                    e = e1;
                    e1 = doc.createElementNS
                        (XQX_NS_URI, "xqx:operand");
                    e.appendChild(e1);
                    e = e1;
                } else if (child.id == JJTMINUS) {
                    Element e1 = doc.createElementNS
                        (XQX_NS_URI, "xqx:unaryMinusOp");
                    e.appendChild(e1);
                    e = e1;
                    e1 = doc.createElementNS
                        (XQX_NS_URI, "xqx:operand");
                    e.appendChild(e1);
                    e = e1;
                } else {
                    transform(child, e);
                    break;
                }
            }
            break;
        }

            // setOp??
        case JJTSTRINGCONCATEXPR:
        case JJTADDITIVEEXPR:
        case JJTMULTIPLICATIVEEXPR:
        case JJTUNIONEXPR:
        case JJTINTERSECTEXCEPTEXPR:
        case JJTCOMPARISONEXPR:
        case JJTANDEXPR:
        case JJTOREXPR: {
            switch (id) {
            case JJTSTRINGCONCATEXPR:
                qname = "xqx:stringConcatenateOp";
                break;
            case JJTADDITIVEEXPR: {
                String op = node.m_value;
                if (op.equals("+"))
                    qname = "xqx:addOp";
                else if (op.equals("-"))
                    qname = "xqx:subtractOp";
                else {
                    assert false;
                    qname = "xqx:unknownAdditiveOp";
                }
                break;
            }                
            case JJTMULTIPLICATIVEEXPR: {
                String op = node.m_value;
                if (op.equals("*"))
                    qname = "xqx:multiplyOp";
                else if (op.equals("div"))
                    qname = "xqx:divOp";
                else if (op.equals("idiv"))
                    qname = "xqx:idivOp";
                else if (op.equals("mod"))
                    qname = "xqx:modOp";
                else {
                    assert false;
                    qname = "xqx:unknownMultiplicativeOp";
                }
                break;
            }
            case JJTUNIONEXPR:
                qname = "xqx:unionOp";
                break;
            case JJTINTERSECTEXCEPTEXPR: {
                String op = node.m_value;
                if (op.equals("intersect"))
                    qname = "xqx:intersectOp";
                else if (op.equals("except"))
                    qname = "xqx:exceptOp";
                else {
                    assert false;
                    qname = "xqx:unknownIntersectOp";
                }
                break;
            }
            case JJTANDEXPR:
                qname = "xqx:andOp";
                break;
            case JJTOREXPR:
                qname = "xqx:orOp";
                break;
            case JJTCOMPARISONEXPR: {
                String op = node.m_value;
                if (op.equals("eq"))
                    qname = "xqx:eqOp";
                else if (op.equals("ne"))
                    qname = "xqx:neOp";
                else if (op.equals("lt"))
                    qname = "xqx:ltOp";
                else if (op.equals("le"))
                    qname = "xqx:leOp";
                else if (op.equals("gt"))
                    qname = "xqx:gtOp";
                else if (op.equals("ge"))
                    qname = "xqx:geOp";
                else if (op.equals("="))
                    qname = "xqx:equalOp";
                else if (op.equals("!="))
                    qname = "xqx:notEqualOp";
                else if (op.equals("<"))
                    qname = "xqx:lessThanOp";
                else if (op.equals("<="))
                    qname = "xqx:lessThanOrEqualOp";
                else if (op.equals(">"))
                    qname = "xqx:greaterThanOp";
                else if (op.equals(">="))
                    qname = "xqx:greaterThanOrEqualOp";
                else if (op.equals("is"))
                    qname = "xqx:isOp";
                else if (op.equals("<<"))
                    qname = "xqx:nodeBeforeOp";
                else if (op.equals(">>"))
                    qname = "xqx:nodeAfterOp";
                else {
                    assert false;
                    qname = "xqx:unknownComparisonOp";
                }
                break;
            }
            default: {
                assert false;
                qname = "xqx:unknownOp";
            }}
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            Element e1 = doc.createElementNS
                (XQX_NS_URI, "xqx:firstOperand");
            e.appendChild(e1);
            transform(node.getChild(0), e1);
            Element e2 = doc.createElementNS
                (XQX_NS_URI, "xqx:secondOperand");
            e.appendChild(e2);
            transform(node.getChild(1), e2);
            
            break;
        }

        case JJTLETCLAUSE:
        case JJTFORCLAUSE: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTFORBINDING:
        case JJTLETBINDING: {
            boolean is_for = (id == JJTFORBINDING);

            int i = transformClauseItem(node, parent, is_for, 0);
            assert i == n;
            break;
        }

        case JJTALLOWINGEMPTY:
            emptyElement(parent, "xqx:allowingEmpty");
            break;

        case JJTGROUPINGSPECLIST:
            transformChildren(node, parent);
            break;

        case JJTGROUPINGSPEC: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);

            // GroupingSpec ::=
            //     GroupingVariable
            //     (TypeDeclaration? ":=" ExprSingle)?
            //     ("collation" URILiteral)?
            // Gaah, two optional chunks in the RHS.
            int i = 0;

            // chunk 1:  GroupingVariable
            transformChildren(node, e, i, i); i++;

            if (i<n && node.getChild(i).id != JJTURILITERAL) {
                // chunk 2 is present:  TypeDeclaration? ":=" ExprSingle
                Element gvi = doc.createElementNS
                    (XQX_NS_URI, "xqx:groupVarInitialize");
                e.appendChild(gvi);

                if (node.getChild(i).id == JJTTYPEDECLARATION) {
                    transformChildren(node, gvi, i, i); i++;
                }

                Element vv = doc.createElementNS
                    (XQX_NS_URI, "xqx:varValue");
                gvi.appendChild(vv);
                transformChildren(node, vv, i, i); i++;
                // end varValue
                // end groupVarInitialize
            }

            if (i<n) {
                // chunk 3 is present:  "collation" URILiteral
                this.transformChildren(node, e, i, i); i++;
            }

            assert i == n;
            break;
        }

        case JJTGROUPINGVARIABLE:
            this.transformChildren(node, parent);
            break;

        case JJTORDERBYCLAUSE: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);

            if (node.m_value != null && node.m_value.equals("stable")) {
                emptyElement(e, "xqx:stable");
            }
            transformChildren(node, e);
            break;
        }

        case JJTORDERSPECLIST: {
            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);
                Element e = doc.createElementNS
                    (XQX_NS_URI, "xqx:orderBySpec");
                parent.appendChild(e);
                transform(child, e);

                if (child.id == JJTORDERSPEC) {
                    int n2 = child.jjtGetNumChildren();
                    for (int j = 0; j < n2; j++) {
                        SimpleNode child2 = child.getChild(j);
                        if (child2.id == JJTORDERMODIFIER) {
                            if (child2.jjtGetNumChildren() > 0)
                                transform(child2, e);
                        }
                    }
                }
            }
            break;
        }

        case JJTORDERSPEC: {
            Element e = doc.createElementNS
                (XQX_NS_URI, "xqx:orderByExpr");
            parent.appendChild(e);
            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);
                if (child.id != JJTORDERMODIFIER)
                    transform(child, e);
            }
            break;
        }

        case JJTORDERMODIFIER: {
            if (node.jjtGetNumChildren() == 0)
                break;
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTPITEST: {
            Element e = doc.createElementNS(XQX_NS_URI, "xqx:piTest");
            parent.appendChild(e);

            if (node.jjtGetNumChildren() > 0) {
                SimpleNode child = node.getChild(0);
                String content =
                    (child.id == JJTSTRINGLITERAL)
                    ? undelimitStringLiteral(child)
                    : child.m_value;
                simpleElement(e, "xqx:piTarget", content);
            }
            break;
        }

        case JJTSCHEMAIMPORT: {
            Element e = doc.createElementNS(XQX_NS_URI, "xqx:schemaImport");
            parent.appendChild(e);
            
            SimpleNode child = node.getChild(0);
            SimpleNode targetNamespace;
            int start = 0;
            if (child.id == JJTSCHEMAPREFIX 
                && child.jjtGetNumChildren() > 0) {
                
                simpleElement(e, "xqx:namespacePrefix",
                              child.getChild(0).m_value);
                start++;
                targetNamespace = node.getChild(1);
                start++;
            } else if (child.id == JJTSCHEMAPREFIX) {
                emptyElement(e, "xqx:defaultElementNamespace");
                start++;
                targetNamespace = node.getChild(1);
                start++;
            } else {
                targetNamespace = child;
                start++;
            }
            simpleElement(e, "xqx:targetNamespace",
                          undelimitStringLiteral(targetNamespace.getChild(0)));

            for (int i = start; i < n; i++) {
                SimpleNode tl = node.getChild(i);
                simpleElement(e, "xqx:targetLocation",
                              undelimitStringLiteral(tl.getChild(0)));
            }
            
            break;
        }

        case JJTASCENDING:
        case JJTDESCENDING: {
            String content =
                (id == JJTASCENDING)
                ? "ascending"
                : "descending";
            simpleElement(parent, "xqx:orderingKind", content);
            break;
        }

        case JJTGREATEST:
        case JJTLEAST: {
            qname =
                (node.getParent().id == JJTEMPTYORDERDECL)
                ? "xqx:emptyOrderingDecl"
                : "xqx:emptyOrderingMode";
            String content =
                (id == JJTGREATEST)
                ? "empty greatest"
                : "empty least";
            simpleElement(parent, qname, content);
            break;
        }

        case JJTPARENTHESIZEDEXPR:
            if (node.jjtGetNumChildren() == 0)
                emptyElement(parent, "xqx:sequenceExpr");
            else
                transformChildren(node, parent);
            break;

        case JJTDEFAULTNAMESPACEDECL: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);

            simpleElement(e, "xqx:defaultNamespaceCategory", node.m_value);
            transformChildren(node, e);
            break;
        }

        case JJTBOUNDARYSPACEDECL:
            if (node.m_value.equals("preserve"))
                _boundarySpacePolicy = BSP_PRESERVE;
            else
                _boundarySpacePolicy = BSP_STRIP;
            break;

        case JJTOCCURRENCEINDICATOR:
        case JJTORDERINGMODEDECL:
        case JJTPRESERVEMODE:
        case JJTINHERITMODE:
        case JJTCONSTRUCTIONDECL:
            simpleElement(parent, qname, node.m_value);
            break;

        case JJTINSTANCEOFEXPR:
        case JJTTREATEXPR:
        case JJTCASTABLEEXPR:
        case JJTCASTEXPR: {
            qname =
                (id == JJTINSTANCEOFEXPR)
                ? "xqx:instanceOfExpr"
                : qname;
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            Element ae = doc.createElementNS(XQX_NS_URI, "xqx:argExpr");
            e.appendChild(ae);
            transformChildren(node, ae, 0, 0);
            // end argExpr
            if (id != JJTCASTEXPR && id != JJTCASTABLEEXPR) {
                Element st = doc.createElementNS(XQX_NS_URI,
                                                 "xqx:sequenceType");
                e.appendChild(st);
                transformChildren(node, st, 1, 1);
            } else
                transformChildren(node, e, 1, 1);
            break;
        }

        case JJTORDEREDEXPR:
        case JJTUNORDEREDEXPR: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            Element ae = doc.createElementNS(XQX_NS_URI, "xqx:argExpr");
            e.appendChild(ae);
            transformChildren(node, ae);
            break;
        }

        case JJTTYPESWITCHEXPR: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            Element ae = doc.createElementNS(XQX_NS_URI, "xqx:argExpr");
            e.appendChild(ae);
            transformChildren(node, ae, 0, 0);
            // end argExpr

            int startOfDefault = n - 2;
            transformChildren(node, e, 1, startOfDefault - 1);

            if (node.getChild(startOfDefault).id != JJTVARNAME) {
                transformChildren(node, e, startOfDefault, startOfDefault);
                startOfDefault++;
            }
            Element tedc = doc.createElementNS
                (XQX_NS_URI, "xqx:typeswitchExprDefaultClause");
            e.appendChild(tedc);
            if (startOfDefault == n - 2) {
                transformChildren(node, tedc, startOfDefault, startOfDefault);
                startOfDefault++;
            }
            Element re = doc.createElementNS
                (XQX_NS_URI, "xqx:resultExpr");
            tedc.appendChild(re);
            transformChildren(node, re, startOfDefault, startOfDefault);
            // end resultExpr
            // end typeswitchExprDefaultClause
            break;
        }

        case JJTCASECLAUSE: {
            Element e = doc.createElementNS
                (XQX_NS_URI, "xqx:typeswitchExprCaseClause");
            parent.appendChild(e);

            int currentChild = 0;
            if (n == 3) {
                currentChild++;
                transformName(getFirstChildOfFirstChild(node),
                              e, "xqx:variableBinding");
            }
            transformChildren(node, e, currentChild, currentChild);
            currentChild++;
            Element re = doc.createElementNS
                (XQX_NS_URI, "xqx:resultExpr");
            e.appendChild(re);
            transformChildren(node, re, currentChild);
            break;
        }

        case JJTSEQUENCETYPEUNION:
            if (node.jjtGetNumChildren() == 1) {
                transformChildren(node, parent);
                // It would also be correct to treat this case the
                // same as the general case. However, doing so would
                // change the output for all (old) queries involving a
                // TypeswitchExpr, which would be an annoyance for the
                // XQTS.
            } else {
                Element e = doc.createElementNS(XQX_NS_URI, qname);
                parent.appendChild(e);
                transformChildren(node, e);
            }
            break;
            
        case JJTSWITCHEXPR: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            Element ae = doc.createElementNS(XQX_NS_URI, "xqx:argExpr");
            e.appendChild(ae);
            transformChildren(node, ae, 0, 0);
            
            int startOfDefault = n - 1;
            transformChildren(node, e, 1, startOfDefault - 1);
            
            Element sedc = doc.createElementNS
                (XQX_NS_URI, "xqx:switchExprDefaultClause");
            e.appendChild(sedc);
            Element re = doc.createElementNS
                (XQX_NS_URI, "xqx:resultExpr");
            sedc.appendChild(re);
            transformChildren(node, re, startOfDefault, startOfDefault);
            break;
        }

        case JJTSWITCHCASECLAUSE: {
            Element secc = doc.createElementNS
                (XQX_NS_URI, "xqx:switchExprCaseClause");
            parent.appendChild(secc);

            for (int i = 0; i < n - 1; i++) {
                Element sce = doc.createElementNS
                    (XQX_NS_URI, "xqx:switchCaseExpr");
                secc.appendChild(sce);
                transformChildren(node, sce, i, i);
            }

            Element re = doc.createElementNS
                (XQX_NS_URI, "xqx:resultExpr");
            secc.appendChild(re);
            transformChildren(node, re, n - 1);
            break;
        }

        case JJTANNOTATEDDECL: {
            qname = xqxElementName(node.getChild(n - 1).id);
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }
            
        case JJTANNOTATION: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e, 0, 0);
            if ( n > 1 ) {
                Element a = doc.createElementNS(XQX_NS_URI, "xqx:arguments");
                e.appendChild(a);
                transformChildren(node, a, 1);
            }
            break;
        }

        case JJTFUNCTIONDECL: {
            int start = 0;
            transformChildren(node, parent, start, start);
            start++;

            if (node.getChild(start).id == JJTPARAMLIST) {
                transformChildren(node, parent, start, start);
                start++;
            } else
                emptyElement(parent, "xqx:paramList");


            int end = n - 1;
            transformChildren(node, parent, start, end - 1);

            start = end;
            if (node.getChild(end).id == JJTEXTERNAL)
                emptyElement(parent, "xqx:externalDefinition");
            else {
                Element e = doc.createElementNS
                    (XQX_NS_URI, "xqx:functionBody");
                parent.appendChild(e);
                transformChildren(node, e, start, end);
            }
            break;
        }

        case JJTINLINEFUNCTIONEXPR: {
            Element ife = doc.createElementNS
                (XQX_NS_URI, "xqx:inlineFunctionExpr");
            parent.appendChild(ife);
            int start = 0;
            int end = n - 1;

            while (node.getChild(start).id == JJTANNOTATION) {
                transformChildren(node, ife, start, start);
                start++;
            }

            if (node.getChild(start).id == JJTPARAMLIST) {
                transformChildren(node, ife, start, start);
                start++;
            } else
                emptyElement(ife, "xqx:paramList");
            
            if (node.getChild(start).id == JJTSEQUENCETYPE) {
                Element td = doc.createElementNS
                    (XQX_NS_URI, "xqx:typeDeclaration");
                ife.appendChild(td);
                transformChildren(node, td, start, start++);
            }

            Element fb = doc.createElementNS
                (XQX_NS_URI, "xqx:functionBody");
            ife.appendChild(fb);
            transformChildren(node, fb, start, end);
            break;
        }

        case JJTVARDECL: {
            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);
                if (child.id == JJTEXTERNAL) {
                    if (i != n - 1) {
                        Element e = doc.createElementNS
                            (XQX_NS_URI, "xqx:external");
                        parent.appendChild(e);
                        transformChildren(node, e, n - 1, n - 1);
                        i++;
                    }
                    else {
                        emptyElement(parent, "xqx:external");
                    }
                }
                else transform(child, parent);
            }
            break;
        }

        case JJTVARDEFAULTVALUE: {
            Element e = doc.createElementNS(XQX_NS_URI, "xqx:varValue");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTCONTEXTITEMDECL: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);
                switch (child.id) {

                case JJTITEMTYPE: {
                    Element td = doc.createElementNS
                        (XQX_NS_URI, "xqx:typeDeclaration");
                    e.appendChild(td);
                    transform(child, td);
                    break;
                }

                case JJTEXTERNAL: {
                    if (i != n - 1) {
                        Element ex = doc.createElementNS
                            (XQX_NS_URI, "xqx:typeDeclaration");
                        e.appendChild(ex);
                        transformChildren(node, ex, n - 1, n - 1);
                        i++;
                    } else {
                        emptyElement(e, "xqx:external");
                    }
                    break;
                }

                default:
                    this.transform(child, parent);
                }
            }
            break;
        }

        case JJTDECIMALFORMATDECL: {
            SimpleNode child = null;
            int i = 0;

            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            child = node.getChild(i);
            if (child.id == JJTQNAME || child.id == JJTURIQUALIFIEDNAME) {
                transform(child, e);
                i++;
            } else {
                // no QName, so it must be a default declaration
                e.setAttributeNS(XQX_NS_URI, "xqx:default", "true");
            }

            for (; i < n; i++) {
                SimpleNode paramName = node.getChild(i++);
                SimpleNode paramValue = node.getChild(i);
                Element dfp = doc.createElementNS
                    (XQX_NS_URI, "xqx:decimalFormatParam");
                e.appendChild(dfp);
                transform(paramName, dfp);
                transform(paramValue, dfp);
            }
            break;
        }

        case JJTDFPROPERTYNAME: {
            simpleElement(parent, "xqx:decimalFormatParamName", node.m_value);
            break;
        }

        case JJTSLIDINGWINDOWCLAUSE:
        case JJTTUMBLINGWINDOWCLAUSE: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            
            int i = 0;
            SimpleNode child = null;

            // first child is VarName
            // second child may be TypeDeclaration

            child = node.getChild(0);
            Element tvb = doc.createElementNS
                (XQX_NS_URI, "xqx:typedVariableBinding");
            e.appendChild(tvb);
            transformName(child.getChild(0), tvb, child.id);
            i++;

            child = node.getChild(1);

            if (child.id == JJTTYPEDECLARATION) {
                transform(child, tvb);
                i++;
            }
            // end typedVariableBinding

            for (; i < n; i++) {
                child = node.getChild(i);
                if (   child.id == JJTWINDOWSTARTCONDITION
                    || child.id == JJTWINDOWENDCONDITION) {
                    transform(child, e);
                }
                else {
                    Element bs = doc.createElementNS
                        (XQX_NS_URI, "xqx:bindingSequence");
                    e.appendChild(bs);
                    transform(child, bs);
                }
            }
            break;
        }

        case JJTWINDOWSTARTCONDITION: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transform(node.getChild(0), e);
            Element wse = doc.createElementNS
                (XQX_NS_URI, "xqx:winStartExpr");
            e.appendChild(wse);
            transform(node.getChild(1), wse);
            break;
        }

        case JJTWINDOWENDCONDITION: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);

            if (node.m_value != null && node.m_value.equals("only"))
                e.setAttributeNS(XQX_NS_URI, "xqx:onlyEnd", "true");

            transform(node.getChild(0), e);
            Element wee = doc.createElementNS
                (XQX_NS_URI, "xqx:winEndExpr");
            e.appendChild(wee);
            transform(node.getChild(1), wee);
            break;
        }

        case JJTANYKINDTEST:
        case JJTTEXTTEST:
        case JJTCOMMENTTEST:
        case JJTEXTERNAL:
        case JJTCONTEXTITEMEXPR:
            emptyElement(parent, qname);
            break;

        case JJTNAMESPACENODETEST:
            emptyElement(parent, "xqx:namespaceTest");
            break;

        case JJTOPTIONDECL:
        case JJTLIBRARYMODULE:
        case JJTNAMESPACEDECL:
        case JJTCOPYNAMESPACESDECL:
        case JJTMODULEIMPORT:
        case JJTPARAMLIST:
        case JJTPARAM:
        case JJTWHERECLAUSE:
        case JJTGROUPBYCLAUSE:
        case JJTCOUNTCLAUSE:
        case JJTRETURNCLAUSE:
        case JJTTYPEDECLARATION:
        case JJTATTRIBUTEDECLARATION: // unreached
        case JJTELEMENTDECLARATION:   // unreached
        case JJTVARVALUE:
        case JJTWINDOWCLAUSE:
        case JJTTRYCATCHEXPR:
        case JJTTRYCLAUSE:
        case JJTCATCHERRORLIST:
        case JJTPARENTHESIZEDITEMTYPE:
        case JJTSCHEMAPREFIX: // unreached?
        case JJTDOCUMENTTEST:
        case JJTATTRIBUTETEST:
        case JJTELEMENTTEST:
        case JJTWINDOWVARS:
        case JJTVOID: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }
            
        case JJTSCHEMAATTRIBUTETEST: {
            /* Parse tree is:
             * |                           SchemaAttributeTest
             * |                              AttributeDeclaration
             * |                                 AttributeName
             * |                                    QName foo
             */
            
            SimpleNode qn = node.getChild(0).getChild(0).getChild(0);
            transformName(qn, parent, "xqx:schemaAttributeTest");
            break;
        }

        case JJTSCHEMAELEMENTTEST: {
            /* Parse tree is:
             * |                           SchemaElementTest
             * |                              ElementDeclaration
             * |                                 ElementName
             * |                                    QName notDeclared:ncname
             */
            
            SimpleNode qn = node.getChild(0).getChild(0).getChild(0);
            transformName(qn, parent, "xqx:schemaElementTest");
            break;
        }

        case JJTFUNCTIONTEST: {
            int lastChildId = node.getChild(n-1).id;
            qname = xqxElementName(lastChildId);
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTANYFUNCTIONTEST:
            break;

        case JJTTYPEDFUNCTIONTEST: {
            Element e = doc.createElementNS
                (XQX_NS_URI, "xqx:paramTypeList");
            parent.appendChild(e);
            transformChildren(node, e, 0, n - 2);
            transformChildren(node, parent, n - 1);
            break;
        }

        case JJTMODULEDECL: {
            /* Parse tree is:
             * |            ModuleDecl
             * |               NCName prefix
             * |               URILiteral
             * |                  StringLiteral "http://example.com"
             * |               Separator
             */
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            SimpleNode ncname = node.getChild(0);
            simpleElement(e, "xqx:prefix", ncname.m_value);
            transformChildren(node, e, 1, 1);
            break;
        }

        case JJTPRAGMA: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            boolean foundPragmaContents = false;
            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);
                transform(child, e);
                if (child.id == JJTPRAGMACONTENTS)
                    foundPragmaContents = true;
            }
            if (!foundPragmaContents) {
                emptyElement(e, "xqx:pragmaContents");
            }
            break;
        }

        case JJTPRAGMACONTENTS: {
            Element e = doc.createElementNS(XQX_NS_URI, "xqx:pragmaContents");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTEXTENSIONEXPR: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            
            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);
                if(i == (n-2)) {
                    if(child.jjtGetNumChildren() == 0)
                        continue;
                    Element ae = doc.createElementNS
                        (XQX_NS_URI, "xqx:argExpr");
                    e.appendChild(ae);
                    transform(child, ae);
                } else {
                    transform(child, e);
                }
            }
            break;
        }
            
        case JJTVALIDATEEXPR: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            for (int i = 0; i < n; i++) {
                SimpleNode child = node.getChild(i);

                switch (child.id) {
                case JJTTYPENAME:
                case JJTVALIDATIONMODE: {
                    transform(child, e);
                    break;
                }
                case JJTEXPR: {
                    Element ae = doc.createElementNS
                        (XQX_NS_URI, "xqx:argExpr");
                    e.appendChild(ae);
                    transform(child, ae);
                }
                    
                }
            }
            break;
        }

        case JJTVALIDATIONMODE:
            simpleElement(parent, qname, node.m_value);
            break;

        case JJTFUNCTIONBODY:
        case JJTDEFAULTCOLLATIONDECL:
        case JJTEMPTYORDERDECL:
        case JJTSIMPLETYPENAME:
        case JJTTYPENAME:
        case JJTIMPORT:
        case JJTPREDICATE:
        case JJTQUOTATTRVALUECONTENT:
        case JJTAPOSATTRVALUECONTENT:
        case JJTNODETEST:
        case JJTCOMMONCONTENT:
        case JJTENCLOSEDEXPR:
        case JJTCONSTRUCTOR:
        case JJTDIRECTCONSTRUCTOR:
        case JJTCURRENTITEM:
        case JJTPREVIOUSITEM:
        case JJTNEXTITEM:
        case JJTTRYTARGETEXPR:
        case JJTFUNCTIONITEMEXPR:
        case JJTSWITCHCASEOPERAND:
            transformChildren(node, parent);
            break;

        case JJTMINUS:
        case JJTPLUS:
        case JJTOPENQUOT:
        case JJTCLOSEQUOT:
        case JJTOPENAPOS:
        case JJTCLOSEAPOS:
        case JJTVALUEINDICATOR:
        case JJTLBRACE:
        case JJTRBRACE:
        case JJTEMPTYTAGCLOSE:
        case JJTSTARTTAGCLOSE:
        case JJTSEPARATOR:
        case JJTLEFTANGLEBRACKET:
        case JJTS:
            break;

        case JJTSETTER:
            checkDuplicateSetters(node);
            transformChildren(node, parent);
            break;

        case JJTPREDEFINEDENTITYREF:
            parent.appendChild(doc.createTextNode(node.m_value));
            break;

        case JJTCHARREF:
            // What to do if this is invalid?
            parent.appendChild(doc.createTextNode(node.m_value));
            break;

        case JJTLCURLYBRACEESCAPE:
            // Bug fix for problem reported in Andrew Eisenberg mail to Scott
            // Boag 03/28/2006 02:36 PM
            // xw.putText("{{");
            parent.appendChild(doc.createTextNode("{"));
            break;

        case JJTRCURLYBRACEESCAPE:
            // Bug fix for problem reported in Andrew Eisenberg mail to Scott
            // Boag 03/28/2006 02:36 PM
            // xw.putText("}}");
            parent.appendChild(doc.createTextNode("}"));
            break;

        case JJTDIRCOMMENTCONSTRUCTOR: {
            Element e = doc.createElementNS
                (XQX_NS_URI, "xqx:computedCommentConstructor");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTDIRCOMMENTCONTENTS: {
            Element ae = doc.createElementNS
                (XQX_NS_URI, "xqx:argExpr");
            parent.appendChild(ae);
            Element sce = doc.createElementNS
                (XQX_NS_URI, "xqx:stringConstantExpr");
            ae.appendChild(sce);
            Element v = doc.createElementNS
                (XQX_NS_URI, "xqx:value");
            sce.appendChild(v);
            transformChildren(node, sce);
            break;
        }

        case JJTDIRCOMMENTCONTENTDASHCHAR:
            //xw.putTextEscaped(node.m_value, true);
            parent.appendChild(doc.createTextNode(node.m_value));
            break;

        case JJTCHAR:
        case JJTDIRCOMMENTCONTENTCHAR: {
            String charStr = node.m_value;
            if (!charStr.equals("\r")) {
                parent.appendChild(doc.createTextNode(node.m_value));
                //if (node.getParent().id == JJTCDATASECTIONCONTENTS)
                //    xw.putText(charStr);
                //else
                //    xw.putTextEscaped(charStr, true);
            } else {
                SimpleNode sib = getNextSibling(node);
                if (sib == null
                    || sib.id != node.id
                    || !sib.m_value.equals("\n"))
                    parent.appendChild(doc.createTextNode("\n"));
            }
            break;
        }

        case JJTCDATASECTIONCONTENTS:
            transformChildren(node, parent);
            break;
            
        case JJTPROCESSINGINSTRUCTIONEND:
        case JJTPROCESSINGINSTRUCTIONSTART:
        case JJTCDATASECTIONSTART:
        case JJTCDATASECTIONEND:
        case JJTDIRCOMMENTSTART:
        case JJTDIRCOMMENTEND:
            break;

        case JJTCDATASECTION: {
            Element ctc = doc.createElementNS
                (XQX_NS_URI, "xqx:computedTextConstructor");
            parent.appendChild(ctc);
            Element ae = doc.createElementNS
                (XQX_NS_URI, "xqx:argExpr");
            ctc.appendChild(ae);
            Element sce = doc.createElementNS
                (XQX_NS_URI, "xqx:stringConstantExpr");
            ae.appendChild(sce);
            Element v = doc.createElementNS
                (XQX_NS_URI, "xqx:value");
            sce.appendChild(v);
            v.appendChild(doc.createTextNode("<![CDATA["));
            transformChildren(node, v);
            v.appendChild(doc.createTextNode("]]>"));
            break;
        }

        case JJTDIRPICONSTRUCTOR: {
            Element cpc = doc.createElementNS
                (XQX_NS_URI, "xqx:computedPIConstructor");
            parent.appendChild(cpc);
            transformChildren(node, cpc);
            break;
        }

        case JJTPITARGET:
            simpleElement(parent, "xqx:piTarget", node.m_value);
            break;

        case JJTDIRPICONTENTS: {
            Element pve = doc.createElementNS
                (XQX_NS_URI, "xqx:piValueExpr");
            parent.appendChild(pve);
            Element sce = doc.createElementNS
                (XQX_NS_URI, "xqx:stringConstantExpr");
            pve.appendChild(sce);
            Element v = doc.createElementNS
                (XQX_NS_URI, "xqx:value");
            sce.appendChild(v);
            transformChildren(node, v);
            break;
        }

        case JJTCOMPCOMMENTCONSTRUCTOR:
        case JJTCOMPDOCCONSTRUCTOR:
        case JJTCOMPTEXTCONSTRUCTOR: {
            String elemName;
            switch (id) {
            case JJTCOMPDOCCONSTRUCTOR:
                qname = "xqx:computedDocumentConstructor";
                break;
            case JJTCOMPTEXTCONSTRUCTOR:
                qname = "xqx:computedTextConstructor";
                break;
            case JJTCOMPCOMMENTCONSTRUCTOR:
                qname = "xqx:computedCommentConstructor";
                break;
            default:
                qname = "UNKNOWN-" + jjtNodeName[id];
                break;
            }
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            Element ae = doc.createElementNS(XQX_NS_URI, "xqx:argExpr");
            e.appendChild(ae);
            transformChildren(node, ae);
            break;
        }

        case JJTCOMPUTEDCONSTRUCTOR:
            transformChildren(node, parent);
            break;
            
        case JJTCOMPPICONSTRUCTOR:
        case JJTCOMPATTRCONSTRUCTOR:
        case JJTCOMPELEMCONSTRUCTOR: {
            qname = (id == JJTCOMPATTRCONSTRUCTOR) 
                ? "xqx:computedAttributeConstructor"
                : (id == JJTCOMPPICONSTRUCTOR)
                ? "xqx:computedPIConstructor"
                : "xqx:computedElementConstructor";
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            int start = 0;
            if (node.getChild(0).id == JJTLBRACE) {
                qname = (id == JJTCOMPPICONSTRUCTOR)
                    ? "xqx:piTargetExpr"
                    : "xqx:tagNameExpr";
                Element te = doc.createElementNS(XQX_NS_URI, qname);
                e.appendChild(te);

                transformChildren(node, te, 1, 1);
                start += 3;
            } else {
                transformChildren(node, e, 0, 0);
                start++;
            }
            if (id == JJTCOMPATTRCONSTRUCTOR 
                || id == JJTCOMPPICONSTRUCTOR) {
                Element te = doc.createElementNS
                    (XQX_NS_URI,  
                     (id == JJTCOMPATTRCONSTRUCTOR)
                     ? "xqx:valueExpr" : "xqx:piValueExpr");
                e.appendChild(te);
                
                if (getNumExprChildren(node, start) == 0) {
                    emptyElement(te, "xqx:sequenceExpr");
                } else
                    transformChildren(node, te, start);
            } else
                transformChildren(node, e, start);

            break;
        }

        case JJTCOMPNAMESPACECONSTRUCTOR: {
            Element e = doc.createElementNS
                (XQX_NS_URI, "xqx:computedNamespaceConstructor");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTPREFIX: {
            SimpleNode child = node.getChild(0);
            simpleElement(parent, qname, child.m_value);
            break;
        }

        case JJTPREFIXEXPR: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }
         
        case JJTURIEXPR: {
            Element e = doc.createElementNS(XQX_NS_URI,"xqx:URIExpr");
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTCONTENTEXPR: {
            Element e = doc.createElementNS(XQX_NS_URI, qname);
            parent.appendChild(e);
            transformChildren(node, e);
            break;
        }

        case JJTDIRATTRIBUTELIST:
            if (getNumRealChildren(node) > 0) {
                Element e = doc.createElementNS
                    (XQX_NS_URI, "xqx:attributeList");
                parent.appendChild(e);
                Element tn = e;

                for (int i = 0; i < n; i++) {
                    SimpleNode child = node.getChild(i);
                    if (child.id == JJTTAGQNAME) {
                        qname =
                            isNamespaceDecl(child)
                            ? "xqx:namespaceDeclaration"
                            : "xqx:attributeConstructor";
                        tn = doc.createElementNS
                            (XQX_NS_URI, qname);
                        e.appendChild(tn);
                    }
                    transform(child, tn);
                }
                
            }
            break;

        case JJTDIRELEMCONTENT:
            // pushElem("xqx:elementContent", node);
            if (node.getChild(0).id == JJTELEMENTCONTENTCHAR) {
                String charStr = node.getChild(0).m_value;
                if (!charStr.equals("\r")) {
                    //xw.putTextEscaped(charStr);
                    parent.appendChild(doc.createTextNode(charStr));
                }
                else {
                    SimpleNode sib = getNextSibling(node);
                    if (sib == null
                        || sib.id != JJTDIRELEMCONTENT)
                        parent.appendChild(doc.createTextNode("\n"));
                    else {
                        SimpleNode sibChild = sib.getChild(0);
                        if (sibChild.id != JJTELEMENTCONTENTCHAR
                            || !sibChild.m_value.equals("\n"))
                            parent.appendChild(doc.createTextNode("\n"));

                    }
                    
                }
            } else
                transformChildren(node, parent);
            break;
            
        case JJTDIRELEMCONSTRUCTOR:
        case JJTDIRATTRIBUTEVALUE:
            // FIXME: unhandled; the original code in XQueryXConvertor
            // is too much of a mess
            break;

        case JJTESCAPEQUOT:
            parent.appendChild(doc.createTextNode("\""));
            break;

        case JJTESCAPEAPOS:
            parent.appendChild(doc.createTextNode("\'"));
            break;

        case JJTQUOTATTRCONTENTCHAR:
        case JJTAPOSATTRCONTENTCHAR:
            if (node.m_value.equals("\r")) {
                // A.2.3 "End-of-Line Handling" says the XQuery
                // processor "must behave as if it normalized all line
                // breaks on input, before parsing."  The parser in
                // this package doesn't, so we have to make up for
                // that.
                SimpleNode charParent = node.getParent();
                SimpleNode nextSibling = getNextSibling(charParent);
                if (null != nextSibling 
                    && nextSibling.jjtGetNumChildren() == 1) {
                    SimpleNode nextCharNode = nextSibling.getChild(0);
                    if (null != nextCharNode) {
                        if (nextCharNode.m_value.equals("\n"))
                                // This character is #xD and the next
                                // is #xA, so this two-character
                                // sequence must be translated to a
                                // single #xA.  The easiest way to do
                                // that is to just skip over the #xD.
                            break;
                    }
                }
                // This character is #xD and it is not immediately
                // followed by #xA, so this must be translated to a
                // single #xA.
                    node.m_value = "\n";
            }

            // 3.7.1.1 "Attributes" says that in a consecutive
            // sequence of literal characters in attribute content,
            // whitespace must be normalized according to the rules in
            // section 3.3.3 of the XML spec (1.0 or 1.1). This in
            // turn says that #xD, #xA, and #x9 are each normalized to
            // a space character (#x20).
            //
            // Of course, #xD has already been eliminated by the A.2.3
            // normalization, so we only have to deal with #xA and #x9
            // (i.e., \n and \t respectively).
            if (node.m_value.equals("\n") || node.m_value.equals("\t")) {
                node.m_value = " ";
            }
            parent.appendChild(doc.createTextNode(node.m_value));
            //    xw.putTextEscaped(node.m_value);
            break;

        case JJTELEMENTCONTENTCHAR:
            if (node.m_value != null) {
                //xw.putTextEscaped(node.m_value);
                parent.appendChild(doc.createTextNode(node.m_value));
            }
            break;
            
        default: {
            String context = "";
            SimpleNode p = node;
            System.err.println("Unknown ID: "
                               + XParserTreeConstants.jjtNodeName[id]);
            while ((p = p.getParent()) != null) {
                context = p.toString()
                    + ((context.length() == 0) ? "" : (", " + context));
            }
            System.err.println("Context is " + context);
            transformChildren(node, parent);
            break;
        }
        }
        // unreachable
    }

    // =========================================================================

    void checkDuplicateSetters(SimpleNode setter){
        SimpleNode setterChild = setter.getChild(0);
        int childID = setterChild.id;

        // Only check for duplicate boundary-space declarations

        if (childID != XParserTreeConstants.JJTBOUNDARYSPACEDECL)
           return;
        SimpleNode parent = setter.getParent();
        int numParentChildren = parent.jjtGetNumChildren();
        for (int j = 0; j < numParentChildren; j++) {
            SimpleNode setterCandidate = parent.getChild(j);
            if(setterCandidate != setter &&
                    setterCandidate.id == XParserTreeConstants.JJTSETTER &&
                    setterCandidate.getChild(0).id == childID){
                String errorCode;
                String errorMsg;
                if(childID == XParserTreeConstants.JJTBOUNDARYSPACEDECL) {
                    errorCode = "err:XQST0068";
                    errorMsg = "Prolog contains more than one boundary-space declaration.";
                }
                else {
                    errorCode = "err:???";
                    errorMsg = "Unknown setter found!";
                }
                throw new PostParseException(
                        errorCode+" Static Error: "+errorMsg);
            }
        }
    }

    /**
     * @param child
     * @return
     */
    protected boolean isNamespaceDecl(SimpleNode child) {
        return child.m_value.startsWith("xmlns:")
                || child.m_value.equals("xmlns");
    }

    /**
     * @param child
     * @return
     */
    protected boolean isNamespaceDecl(String val) {
        return val.startsWith("xmlns:")
                || val.equals("xmlns");
    }


    /**
     * @param node
     */
    protected SimpleNode getNextSibling(SimpleNode node) {
        int nSiblingsOrSelf = node.getParent().jjtGetNumChildren();
        for (int i = 0; i < nSiblingsOrSelf; i++) {
            SimpleNode siblingOrSelf = node.getParent().getChild(i);
            if (siblingOrSelf == node) {
                if ((i + 1) < nSiblingsOrSelf) {
                    return node.getParent().getChild(i + 1);
                }
                break;
            }
        }
        return null;
    }

    /**
     * @param node
     */
    protected SimpleNode getPreviousSibling(SimpleNode node) {
        int nSiblingsOrSelf = node.getParent().jjtGetNumChildren();
        for (int i = 0; i < nSiblingsOrSelf; i++) {
            SimpleNode siblingOrSelf = node.getParent().getChild(i);
            if (siblingOrSelf == node) {
                if (i > 0)
                    return node.getParent().getChild(i - 1);
                else
                    break;
            }
        }
        return null;
    }

    protected int getNumExprChildren(SimpleNode node) {
        return getNumExprChildren(node, 0);
    }

    protected int getNumExprChildren(SimpleNode node, int start) {
        int count = 0;
        int n = node.jjtGetNumChildren();
        for (int i = start; i < n; i++) {
            SimpleNode child = node.getChild(i);
            if (child.id != JJTS && child.id != JJTLBRACE
                    && child.id != JJTRBRACE)
                count++;
        }
        return count;
    }

    protected int getNumRealChildren(SimpleNode node) {
        int count = 0;
        int n = node.jjtGetNumChildren();
        for (int i = 0; i < n; i++) {
            SimpleNode child = node.getChild(i);
            if (child.id != JJTS)
                count++;
        }
        return count;
    }

    /**
     * @param child
     * @return
     */
    protected SimpleNode getEnclosedExpr(SimpleNode child) {
        return getUnitDescendantOrSelf(child, JJTENCLOSEDEXPR);
    }

    protected SimpleNode getUnitDescendantOrSelf(SimpleNode base, int desired_id)
    // If there exists a node that:
    // -- has id = 'desired_id', and
    // -- is, at its level, the sole descendant-or-self of 'base',
    // then return that node.
    // Otherwise return null.
    {
        SimpleNode node = base;
        while (true) {
            if (node.id == desired_id) return node;
            if (node.jjtGetNumChildren() != 1) return null;
            node = node.getChild(0);
        }
    }

    /**
     * @param child
     * @return
     */
    protected boolean isAttrContentChar(int id) {
        switch (id) {
            case JJTQUOTATTRVALUECONTENT:
            case JJTAPOSATTRVALUECONTENT:
            case JJTESCAPEAPOS:
            case JJTESCAPEQUOT:
                return true;

            case JJTCOMMONCONTENT:
                return true;

            default:
                return false;
        }
    }

    protected boolean isElemContentChar(SimpleNode node) {
        if (node == null)
            return false;
        if (node.id == JJTDIRELEMCONTENT) {
            switch (node.getChild(0).id) {
                case JJTELEMENTCONTENTCHAR:
                    return true;

                case JJTCOMMONCONTENT:
                    return true;

                default:
                    return false;
            }
        } else
            return false;
    }

    /**
     * @param node
     * @return
     */
    protected SimpleNode getFirstChildOfFirstChild(SimpleNode node) {
        if(node.jjtGetNumChildren() <= 0)
            return null;
        SimpleNode firstChild = node.getChild(0);
        if(firstChild.jjtGetNumChildren() <= 0)
            return null;
        return firstChild.getChild(0);
    }

    /**
     * @param node
     * @return
     */
    protected int getFirstChildOfFirstChildID(SimpleNode node) {
        if(node.jjtGetNumChildren() <= 0)
            return -1;
        SimpleNode firstChild = node.getChild(0);
        if(firstChild.jjtGetNumChildren() <= 0)
            return -1;
        return firstChild.getChild(0).id;
    }

}
