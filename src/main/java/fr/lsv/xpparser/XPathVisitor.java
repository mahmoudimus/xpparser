/*
Copyright (C) 2016 Sylvain Schmitz (ENS Cachan).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License in `LICENSE` for more details.

This software or document includes material copied from or derived
from the XPath/XQuery Applets
(https://www.w3.org/2013/01/qt-applets/), in particular on the file
`XQueryXConverterBase-xquery30.java`.  Copyright (C)2013 W3C(R) (MIT,
ERCIM, Keio, Beihang).  See `LICENSE-W3C` for more details.
 */
package fr.lsv.xpparser;

import java.util.List;
import java.util.LinkedList;
import org.w3c.xqparser.*;

/**
 * Finds XPath fragments inside an XQuery Parse tree.
 */
public class XPathVisitor implements XParserVisitor, XParserTreeConstants {
    private SimpleNode node;
    
    public XPathVisitor(final SimpleNode node) {
        this.node = node;
    }

    /**
     * A NodeList combines a boolean with a List of nodes.
     */
    public class NodeList {
        
        private boolean first;
        private List<SimpleNode> second;

        public NodeList(boolean first, List<SimpleNode> second) {
            this.first = first;
            this.second = second;
        }

        /**
         * Construct an empty node list.
         */
        public NodeList() {
            this.first = false;
            this.second = new LinkedList<SimpleNode>();
        }

        public void setBool(boolean first) {
            this.first = first;
        }
        
        public void setList(List second) {
            this.second = second;
        }

        public boolean getBool() {
            return first;
        }

        public List getList() {
            return second;
        }

        public void set(boolean first, List second) {
            setBool(first);
            setList(second);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NodeList nl = (NodeList) o;

            if (first != nl.first)
                return false;
            if (second != null ? !second.equals(nl.second) : nl.second != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = first? 1: 0;
            result = 31 * result + (second != null ? second.hashCode() : 0);
            return result;
        }
    }

    /**
     * Convenience visitor method.
     * @param node The node to be processed.
     * @return The list of nodes that are actually XPath 3.0 expressions.
     */
    public static List<SimpleNode> visit(final SimpleNode node) {
        NodeList nl = (NodeList)
            node.jjtAccept(new XPathVisitor(node),
                           new LinkedList<SimpleNode>());
        return nl.getList();
    }

    /**
     * Aggregate a NodeList from the results of visiting the children
     * of the node.
     * @param node The node whose children must be processed.
     * @param l    The list of selected nodes in the surrounding context.
     * @return A NodeList with Boolean value true iff all the children
     *         return true, and with the concatenation of nl with the
     *         children's visits.
     */
    private NodeList visitChildren(final SimpleNode node,
                                   List<SimpleNode> l) {
        boolean allTrue = true;
        int nChildren = node.jjtGetNumChildren();
        List<SimpleNode> concatList = l;
        for (int i = 0; i < nChildren; i++) {
            NodeList ret = (NodeList) visit(node.getChild(i), concatList);
            allTrue = allTrue && ret.getBool();
            concatList = ret.getList();
        }
        return new NodeList(allTrue, concatList);
    }
    
    /**
     * If all the children of the node are XPath nodes, then add the
     * node itself.  Otherwise add their concatenated list of XPath
     * nodes.
     * @param node The node whose children must be processed.
     * @param l    The list of selected nodes in the surrounding context.
     * @return A NodeList with Boolean value true iff all the children
     *         return true.
     */
    private NodeList nonTransparentVisit(final SimpleNode node,
                                         List<SimpleNode> l) {
        NodeList nl = visitChildren(node, new LinkedList<SimpleNode>());
        if (nl.getBool()) {
            l.add(node);
            return new NodeList(true, l);
        }
        else {
            l.addAll(nl.getList());
            return new NodeList(false, l);
        }
    }
        
    /**
     * Process a node in the AST and its descendants.
     * @param node  The node to be processed.
     * @param data  A List of other nodes found in the surrounding context.
     * @return The list of nodes that are actually XPath 3.0 expressions.
     */
    public Object visit(final SimpleNode node, Object data) {
        assert(data instanceof List);
        int id = node.id;
        
        switch (id) {
            // process children and return false
            // (not in XPath but might contain some XPath inside)
        case JJTSTART:
        case JJTQUERYLIST:
        case JJTLIBRARYMODULE:
        case JJTMODULE:
        case JJTMAINMODULE:
        case JJTQUERYBODY:
        case JJTVARDECL:
        case JJTVARVALUE:
        case JJTVARDEFAULTVALUE:
        case JJTCONTEXTITEMDECL:
        case JJTANNOTATEDDECL:
        case JJTPROLOG:          
        case JJTWINDOWCLAUSE:
        case JJTTUMBLINGWINDOWCLAUSE:
        case JJTSLIDINGWINDOWCLAUSE:            
        case JJTWINDOWSTARTCONDITION:            
        case JJTWINDOWENDCONDITION:            
        case JJTWHERECLAUSE:
        case JJTGROUPBYCLAUSE:
        case JJTGROUPINGSPECLIST:
        case JJTGROUPINGSPEC:            
        case JJTORDERBYCLAUSE:
        case JJTORDERSPECLIST:
        case JJTORDERSPEC:
        case JJTSWITCHEXPR:
        case JJTSWITCHCASECLAUSE:
        case JJTSWITCHCASEOPERAND:
        case JJTTYPESWITCHEXPR:            
        case JJTCASECLAUSE:            
        case JJTSEQUENCETYPEUNION:
        case JJTTRYCATCHEXPR:
        case JJTTRYCLAUSE:
        case JJTTRYTARGETEXPR:
        case JJTCATCHCLAUSE:            
        case JJTVALIDATEEXPR:            
        case JJTEXTENSIONEXPR:
        case JJTORDEREDEXPR:
        case JJTUNORDEREDEXPR:
        case JJTDIRELEMCONTENT:
        case JJTCOMMONCONTENT:
        case JJTAPOSATTRVALUECONTENT:
        case JJTQUOTATTRVALUECONTENT:
        case JJTDIRATTRIBUTEVALUE:
        case JJTDIRATTRIBUTELIST:
        case JJTDIRELEMCONSTRUCTOR:
        case JJTCONSTRUCTOR:
        case JJTDIRECTCONSTRUCTOR:
        case JJTCOMPUTEDCONSTRUCTOR:
        case JJTCOMPDOCCONSTRUCTOR:
        case JJTCOMPELEMCONSTRUCTOR:            
        case JJTCONTENTEXPR:            
        case JJTCOMPATTRCONSTRUCTOR:            
        case JJTCOMPNAMESPACECONSTRUCTOR:
        case JJTINTERMEDIATECLAUSE:
        case JJTPREFIX:
        case JJTPREFIXEXPR:            
        case JJTURIEXPR:            
        case JJTCOMPTEXTCONSTRUCTOR:            
        case JJTCOMPCOMMENTCONSTRUCTOR:            
        case JJTCOMPPICONSTRUCTOR: {
            NodeList nl = visitChildren(node, (List<SimpleNode>) data);
            return new NodeList(false, nl.getList());
        }

            // ignore children and return true:
            // (typically lexical elements)
        case JJTSTRINGLITERAL:
        case JJTLBRACE:
        case JJTRBRACE:
        case JJTMINUS:
        case JJTPLUS:
        case JJTCHAR:
        case JJTSLASH:
        case JJTSLASHSLASH:
        case JJTFORWARDAXIS:
        case JJTREVERSEAXIS:
        case JJTABBREVREVERSESTEP:
        case JJTWILDCARD:
        case JJTNCNAMECOLONSTAR:
        case JJTSTARCOLONNCNAME:
        case JJTURIQUALIFIEDSTAR:
        case JJTINTEGERLITERAL:
        case JJTDECIMALLITERAL:
        case JJTDOUBLELITERAL:
        case JJTVARNAME:
        case JJTCONTEXTITEMEXPR:
        case JJTARGUMENTPLACEHOLDER:
        case JJTLEFTANGLEBRACKET:            
        case JJTOPENQUOT:
        case JJTCLOSEQUOT:
        case JJTESCAPEQUOT:
        case JJTESCAPEAPOS:
        case JJTQUOTATTRCONTENTCHAR:
        case JJTAPOSATTRCONTENTCHAR:
        case JJTELEMENTCONTENTCHAR:
        case JJTNAMEDFUNCTIONREF:
        case JJTANYKINDTEST:
        case JJTTEXTTEST:
        case JJTCOMMENTTEST:
        case JJTNAMESPACENODETEST:
        case JJTATTRIBNAMEORWILDCARD:
        case JJTSCHEMAATTRIBUTETEST:            
        case JJTATTRIBUTEDECLARATION:            
        case JJTELEMENTTEST:
        case JJTATTRIBUTENAME:
        case JJTELEMENTNAME:
        case JJTSIMPLETYPENAME:
        case JJTTYPENAME:
        case JJTELEMENTNAMEORWILDCARD:
        case JJTSCHEMAELEMENTTEST:
        case JJTELEMENTDECLARATION:
        case JJTANYFUNCTIONTEST:
        case JJTURILITERAL:
            return new NodeList(true, (List<SimpleNode>) data);

            // ignore children and return false:
            // (does not contain any XPath)
        case JJTVERSIONDECL:
        case JJTMODULEDECL:
        case JJTNAMESPACEDECL:
        case JJTANNOTATION:
        case JJTMODULEIMPORT:
        case JJTSEPARATOR:
        case JJTBOUNDARYSPACEDECL:
        case JJTDEFAULTCOLLATIONDECL:
        case JJTBASEURIDECL:
        case JJTCONSTRUCTIONDECL:
        case JJTORDERINGMODEDECL:
        case JJTEMPTYORDERDECL:
        case JJTGREATEST:
        case JJTLEAST:
        case JJTCOPYNAMESPACESDECL:
        case JJTPRESERVEMODE:
        case JJTINHERITMODE:
        case JJTDECIMALFORMATDECL:
        case JJTDFPROPERTYNAME:            
        case JJTSETTER:
        case JJTSCHEMAIMPORT:
        case JJTSCHEMAPREFIX:            
        case JJTIMPORT:            
        case JJTDEFAULTNAMESPACEDECL:
        case JJTEXTERNAL:
        case JJTOPTIONDECL:
        case JJTALLOWINGEMPTY:            
        case JJTWINDOWVARS:
        case JJTCURRENTITEM:
        case JJTPREVIOUSITEM:
        case JJTNEXTITEM:    
        case JJTGROUPINGVARIABLE:        
        case JJTCOUNTCLAUSE:            
        case JJTORDERMODIFIER:
        case JJTASCENDING:
        case JJTDESCENDING:            
        case JJTPOSITIONALVAR:
        case JJTCATCHERRORLIST:
        case JJTVALIDATIONMODE:
        case JJTPRAGMA:
        case JJTPRAGMAOPEN:
        case JJTS:
        case JJTPRAGMACLOSE:
        case JJTPRAGMACONTENTS:            
        case JJTTAGQNAME:
        case JJTEMPTYTAGCLOSE:
        case JJTSTARTTAGCLOSE:
        case JJTENDTAGQNAME:
        case JJTVALUEINDICATOR:
        case JJTOPENAPOS:
        case JJTCLOSEAPOS:
        case JJTPREDEFINEDENTITYREF:            
        case JJTCHARREF:            
        case JJTLCURLYBRACEESCAPE:
        case JJTRCURLYBRACEESCAPE:
        case JJTDIRCOMMENTCONSTRUCTOR:
        case JJTDIRCOMMENTSTART:
        case JJTDIRCOMMENTEND:
        case JJTDIRCOMMENTCONTENTS:
        case JJTDIRCOMMENTCONTENTCHAR:
        case JJTDIRCOMMENTCONTENTDASHCHAR:            
        case JJTCDATASECTION:
        case JJTCDATASECTIONSTART:
        case JJTCDATASECTIONEND:
        case JJTCDATASECTIONCONTENTS:
        case JJTDIRPICONSTRUCTOR:
        case JJTPITARGET:
        case JJTDIRPICONTENTS:
        case JJTPROCESSINGINSTRUCTIONSTART:
        case JJTPROCESSINGINSTRUCTIONEND:
            return new NodeList(false, (List<SimpleNode>) data);

            // transparent:
            // if all children are XPath expressions, return them
        case JJTPOSTFIXEXPR:
        case JJTSINGLETYPE:
        case JJTOCCURRENCEINDICATOR:
        case JJTITEMTYPE:
        case JJTDOCUMENTTEST:
        case JJTPITEST:
        case JJTATTRIBUTETEST:
        case JJTFUNCTIONTEST:
        case JJTPARENTHESIZEDITEMTYPE:
        case JJTURIQUALIFIEDNAME:
        case JJTSEQUENCETYPE:
        case JJTFUNCTIONBODY:
        case JJTENCLOSEDEXPR:
        case JJTEXPR:
        case JJTVOID:
        case JJTAXISSTEP:
        case JJTABBREVFORWARDSTEP:
        case JJTNODETEST:
        case JJTPREDICATELIST:
        case JJTPREDICATE:
        case JJTPARENTHESIZEDEXPR:
        case JJTARGUMENT:
        case JJTNAMETEST:
        case JJTARGUMENTLIST:
        case JJTFUNCTIONITEMEXPR:
        case JJTATOMICORUNIONTYPE:
        case JJTTYPEDFUNCTIONTEST:
        case JJTRETURNCLAUSE:
        case JJTINITIALCLAUSE:
        case JJTFORCLAUSE:
        case JJTLETCLAUSE:
            return visitChildren(node, (List<SimpleNode>) data);

            // non-transparent: if all children are XPath expressions,
            // return the node itself
        case JJTOREXPR:
        case JJTANDEXPR:
        case JJTCOMPARISONEXPR:
        case JJTSTRINGCONCATEXPR:
        case JJTRANGEEXPR:
        case JJTADDITIVEEXPR:
        case JJTMULTIPLICATIVEEXPR:
        case JJTUNIONEXPR:
        case JJTINTERSECTEXCEPTEXPR:
        case JJTINSTANCEOFEXPR:
        case JJTTREATEXPR:
        case JJTCASTABLEEXPR:
        case JJTCASTEXPR:
        case JJTUNARYEXPR:
        case JJTSIMPLEMAPEXPR:
        case JJTPATHEXPR:
        case JJTFUNCTIONCALL:
        case JJTINLINEFUNCTIONEXPR:
        case JJTIFEXPR:
        case JJTQUANTIFIEDEXPR:
        case JJTFLWOREXPR11:
            return nonTransparentVisit(node, (List<SimpleNode>) data);

            // nodes with special work required:
        case JJTQNAME: {
            // some built-in names are not available in XPath
            if (node.m_value != null) {
                if (node.m_value.equals("switch")
                    || node.m_value.equals("typeswitch")
                    || node.m_value.equals("while"))
                    return new NodeList(false, (List<SimpleNode>) data);
                else
                    return new NodeList(true, (List<SimpleNode>) data);
            }
            else
                return nonTransparentVisit(node, (List<SimpleNode>) data);
        }
            
        case JJTFUNCTIONQNAME: {
            // some built-in function names are not available in XPath
            if (node.m_value != null) {
                /*if (node.m_value.equals("NaN")
                    || node.m_value.equals("after")
                    || node.m_value.equals("all")
                    || node.m_value.equals("allowing")
                    || node.m_value.equals("any")
                    || node.m_value.equals("at")
                    || node.m_value.equals("base-uri")
                    || node.m_value.equals("before")
                    || node.m_value.equals("block")
                    || node.m_value.equals("boundary-space")
                    || node.m_value.equals("by")
                    || node.m_value.equals("case")
                    || node.m_value.equals("catch")
                    || node.m_value.equals("collation")
                    || node.m_value.equals("construction")
                    //|| node.m_value.equals("contains")
                    || node.m_value.equals("content")
                    || node.m_value.equals("context")
                    || node.m_value.equals("copy")
                    || node.m_value.equals("copy-namespaces")
                    || node.m_value.equals("declare")
                    || node.m_value.equals("default")
                    || node.m_value.equals("delete")
                    || node.m_value.equals("diacritics")
                    || node.m_value.equals("different")
                    || node.m_value.equals("distance")
                    //|| node.m_value.equals("div")
                    || node.m_value.equals("document")
                    //|| node.m_value.equals("empty")
                    || node.m_value.equals("encoding")
                    || node.m_value.equals("end")
                    || node.m_value.equals("entire")
                    || node.m_value.equals("exactly")
                    || node.m_value.equals("exit")
                    || node.m_value.equals("first")
                    || node.m_value.equals("from")
                    || node.m_value.equals("ft-option")
                    || node.m_value.equals("ftand")
                    || node.m_value.equals("ftnot")
                    || node.m_value.equals("ftor")
                    || node.m_value.equals("function")
                    || node.m_value.equals("group")
                    || node.m_value.equals("grouping-separator")
                    || node.m_value.equals("import")
                    || node.m_value.equals("infinity")
                    || node.m_value.equals("inherit")
                    || node.m_value.equals("insensitive")
                    || node.m_value.equals("insert")
                    || node.m_value.equals("into")
                    || node.m_value.equals("key")
                    || node.m_value.equals("language")
                    //|| node.m_value.equals("last")
                    || node.m_value.equals("lax")
                    || node.m_value.equals("levels")
                    || node.m_value.equals("lowercase")
                    || node.m_value.equals("minus-sign")
                    || node.m_value.equals("modify")
                    || node.m_value.equals("next")
                    || node.m_value.equals("no")
                    || node.m_value.equals("no-inherit")
                    || node.m_value.equals("no-preserve")
                    || node.m_value.equals("nodes")
                    || node.m_value.equals("only")
                    || node.m_value.equals("order")
                    || node.m_value.equals("ordered")
                    || node.m_value.equals("ordering")
                    || node.m_value.equals("paragraph")
                    || node.m_value.equals("paragraphs")
                    || node.m_value.equals("pattern-separator")
                    || node.m_value.equals("per-mille")
                    || node.m_value.equals("percent")
                    || node.m_value.equals("phrase")
                    || node.m_value.equals("preserve")
                    || node.m_value.equals("previous")
                    || node.m_value.equals("relationship")
                    || node.m_value.equals("rename")
                    || node.m_value.equals("replace")
                    || node.m_value.equals("returning")
                    || node.m_value.equals("revalidation")
                    || node.m_value.equals("same")
                    || node.m_value.equals("schema")
                    || node.m_value.equals("score")
                    || node.m_value.equals("sensitive")
                    || node.m_value.equals("sentence")
                    || node.m_value.equals("sentences")
                    || node.m_value.equals("skip")
                    || node.m_value.equals("sliding")
                    || node.m_value.equals("stable")
                    || node.m_value.equals("start")
                    || node.m_value.equals("stemming")
                    || node.m_value.equals("stop")
                    || node.m_value.equals("strict")
                    || node.m_value.equals("strip")
                    || node.m_value.equals("thesaurus")
                    || node.m_value.equals("times")
                    || node.m_value.equals("try")
                    || node.m_value.equals("tumbling")
                    || node.m_value.equals("type")
                    || node.m_value.equals("unordered")
                    || node.m_value.equals("updating")
                    || node.m_value.equals("uppercase")
                    || node.m_value.equals("using")
                    || node.m_value.equals("validate")
                    || node.m_value.equals("value")
                    || node.m_value.equals("variable")
                    || node.m_value.equals("version")
                    || node.m_value.equals("weight")
                    || node.m_value.equals("when")
                    || node.m_value.equals("where")
                    || node.m_value.equals("wildcards")
                    || node.m_value.equals("window")
                    || node.m_value.equals("with")
                    || node.m_value.equals("without")
                    || node.m_value.equals("word")
                    || node.m_value.equals("words")
                    || node.m_value.equals("xquery")
                    || node.m_value.equals("zero-digit"))
                    return new NodeList(false, (List) data);
                    else*/
                    return new NodeList(true, (List) data);
            }
            else
                return nonTransparentVisit(node, (List) data);
        }

        case JJTFORBINDING:
        case JJTLETBINDING:
        case JJTFUNCTIONDECL:  {
            // throw away the ParamList but keep the FunctionBody
            int nChildren = node.jjtGetNumChildren();
            NodeList nl =
                (NodeList) visit(node.getChild(nChildren - 1), data);
            // more than two children means a positional variable
            return new NodeList((nChildren <= 2) && nl.getBool(),
                                nl.getList());
        }

        case JJTTYPEDECLARATION: {
            // TypeDeclaration is only valid inside a Param in XPath
            if (node.getParent().id != JJTPARAM)
                return new NodeList(false, (List) data);                
            else {
                NodeList nl = visitChildren(node, (List<SimpleNode>) data);
                return new NodeList(nl.getBool(), (List) data);
            }
        }
            
        case JJTNCNAME:
        case JJTPARAMLIST:
        case JJTPARAM: {
            NodeList nl = visitChildren(node, (List<SimpleNode>) data);
            return new NodeList(nl.getBool(), (List) data);
        }
                
        default:
            assert(false);
            return new NodeList(false, (List<SimpleNode>) data);
        }
    }        
}
