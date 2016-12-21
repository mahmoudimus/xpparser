package org.w3c.xqparser;

import java.util.List;
import java.util.LinkedList;

/**
 * Finds XPath fragments inside an XQuery Parse tree.
 */
public class XPathVisitor implements XParserVisitor {
    private SimpleNode node;
    
    public XPathVisitor(final SimpleNode node) {
        this.node = node;
    }

    /**
     * A NodeList combines a boolean with a List of nodes.
     */
    public class NodeList {
        
        private boolean first;
        private List second;

        public NodeList(boolean first, List second) {
            this.first = first;
            this.second = second;
        }

        /**
         * Construct an empty node list.
         */
        public NodeList() {
            this.first = false;
            this.second = new LinkedList();
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
    public static List visit(final SimpleNode node) {
        NodeList nl = (NodeList) node.jjtAccept(new XPathVisitor(node),
                                                new LinkedList());
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
    private NodeList visitChildren(final SimpleNode node, List l) {
        boolean allTrue = true;
        int nChildren = node.jjtGetNumChildren();
        List concatList = l;
        for (int i = 0; i < nChildren; i++) {
            NodeList ret = visit(node.getChild(i), concatList);
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
    private NodeList nonTransparentVisit(final SimpleNode node, List l) {
        NodeList nl = visitChildren(node, new LinkedList());
        if (nl.getBool())
            return new NodeList(true,
                                ((List) data).add(node));
        else
            return new NodeList(false,
                                ((List) data).addAll(nl.getList()));
    }
        
    /**
     * Process a node in the AST and its descendants.
     * @param node  The node to be processed.
     * @param data  A List of other nodes found in the surrounding context.
     * @return The list of nodes that are actually XPath 3.0 expressions.
     */
    public NodeList visit(final SimpleNode node, Object data) {
        assert(data instanceof List);
        int id = node.id;
        
        switch (id) {
            // process children and return false
            // (not in XPath but might contain some XPath inside)
        case JJTSTART:
        case JJTQUERYLIST:
        case JJTMODULE:
        case JJTMAINMODULE:
        case JJTQUERYBODY:
        case JJTVARDECL:
        case JJTVARVALUE:
        case JJTVARDEFAULTVALUE:
        case JJTCONTEXTITEMDECL:
        case JJTANNOTATEDDECL:
        case JJTPROLOG:
        case JJTFUNCTIONDECL:            
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
        case JJTPREFIX:
        case JJTPREFIXEXPR:            
        case JJTURIEXPR:            
        case JJTCOMPTEXTCONSTRUCTOR:            
        case JJTCOMPCOMMENTCONSTRUCTOR:            
        case JJTCOMPPICONSTRUCTOR: {
            NodeList nl = visitChildren(node, (List) data);
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
            return new NodeList(true, (List) data);

            // ignore children and return false:
            // (does not contain any XPath)
        case JJTVERSIONDECL:
        case JJTLIBRARYMODULE:
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
            return new NodeList(false, (List) data);

            // transparent:
            // if all children are XPath expressions, return them
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
        case JJTFUNCTIONITEMEXPR:
        case JJTATOMICORUNIONTYPE:
        case JJTTYPEDFUNCTIONTEST:
        case JJTRETURNCLAUSE:
        case JJTFORBINDING:
        case JJTLETBINDING:
        case JJTINITIALCLAUSE:
        case JJTINTERMEDIATECLAUSE:
        case JJTFORCLAUSE:
        case JJTLETCLAUSE:
            return visitChildren(node, (List) data);

            // non-transparent: if all children are XPath expressions,
            // return the node itself
        case JJTPARAMLIST:
        case JJTPARAM:
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
        case JJTNAMETEST:
        case JJTPOSTFIXEXPR:
        case JJTARGUMENTLIST:
        case JJTSEQUENCETYPE:
        case JJTFUNCTIONCALL:
        case JJTINLINEFUNCTIONEXPR:
        case JJTSINGLETYPE:
        case JJTOCCURRENCEINDICATOR:
        case JJTITEMTYPE:
        case JJTDOCUMENTTEST:
        case JJTPITEST:
        case JJTATTRIBUTETEST:
        case JJTFUNCTIONTEST:
        case JJTPARENTHESIZEDITEMTYPE:
        case JJTURIQUALIFIEDNAME:
        case JJTNCNAME:
        case JJTIFEXPR:
        case JJTQUANTIFIEDEXPR:
        case JJTFLWOREXPR11:
            return nonTransparentVisit(node, (List) data);

            // nodes with special work required:
        case JJTQNAME: {
            // some built-in names are not available in XPath
            if (node.m_value != null) {
                if (node.m_value.equals("switch")
                    || node.m_value.equals("typeswitch")
                    || node.m_value.equals("while"))
                    return new NodeList(false, (List) data);
                else
                    return new NodeList(true, (List) data);
            }
            else
                return nonTransparentVisit(node, (List) data);
        }
            
        case JJTFUNCTIONQNAME: {
            // some built-in function names are not available in XPath
            if (node.m_value != null) {
                if (node.m_value.equals("NaN")
                    || node.value.equals("after")
                    || node.value.equals("all")
                    || node.value.equals("allowing")
                    || node.value.equals("any")
                    || node.value.equals("at")
                    || node.value.equals("base-uri")
                    || node.value.equals("before")
                    || node.value.equals("block")
                    || node.value.equals("boundary-space")
                    || node.value.equals("by")
                    || node.value.equals("case")
                    || node.value.equals("catch")
                    || node.value.equals("collation")
                    || node.value.equals("construction")
                    || node.value.equals("contains")
                    || node.value.equals("content")
                    || node.value.equals("context")
                    || node.value.equals("copy")
                    || node.value.equals("copy-namespaces")
                    || node.value.equals("declare")
                    || node.value.equals("default")
                    || node.value.equals("delete")
                    || node.value.equals("diacritics")
                    || node.value.equals("different")
                    || node.value.equals("distance")
                    || node.value.equals("div")
                    || node.value.equals("document")
                    || node.value.equals("empty")
                    || node.value.equals("encoding")
                    || node.value.equals("end")
                    || node.value.equals("entire")
                    || node.value.equals("exactly")
                    || node.value.equals("exit")
                    || node.value.equals("first")
                    || node.value.equals("from")
                    || node.value.equals("ft-option")
                    || node.value.equals("ftand")
                    || node.value.equals("ftnot")
                    || node.value.equals("ftor")
                    || node.value.equals("function")
                    || node.value.equals("group")
                    || node.value.equals("grouping-separator")
                    || node.value.equals("import")
                    || node.value.equals("infinity")
                    || node.value.equals("inherit")
                    || node.value.equals("insensitive")
                    || node.value.equals("insert")
                    || node.value.equals("into")
                    || node.value.equals("key")
                    || node.value.equals("language")
                    || node.value.equals("last")
                    || node.value.equals("lax")
                    || node.value.equals("levels")
                    || node.value.equals("lowercase")
                    || node.value.equals("minus-sign")
                    || node.value.equals("modify")
                    || node.value.equals("next")
                    || node.value.equals("no")
                    || node.value.equals("no-inherit")
                    || node.value.equals("no-preserve")
                    || node.value.equals("nodes")
                    || node.value.equals("only")
                    || node.value.equals("order")
                    || node.value.equals("ordered")
                    || node.value.equals("ordering")
                    || node.value.equals("paragraph")
                    || node.value.equals("paragraphs")
                    || node.value.equals("pattern-separator")
                    || node.value.equals("per-mille")
                    || node.value.equals("percent")
                    || node.value.equals("phrase")
                    || node.value.equals("preserve")
                    || node.value.equals("previous")
                    || node.value.equals("relationship")
                    || node.value.equals("rename")
                    || node.value.equals("replace")
                    || node.value.equals("returning")
                    || node.value.equals("revalidation")
                    || node.value.equals("same")
                    || node.value.equals("schema")
                    || node.value.equals("score")
                    || node.value.equals("sensitive")
                    || node.value.equals("sentence")
                    || node.value.equals("sentences")
                    || node.value.equals("skip")
                    || node.value.equals("sliding")
                    || node.value.equals("stable")
                    || node.value.equals("start")
                    || node.value.equals("stemming")
                    || node.value.equals("stop")
                    || node.value.equals("strict")
                    || node.value.equals("strip")
                    || node.value.equals("thesaurus")
                    || node.value.equals("times")
                    || node.value.equals("try")
                    || node.value.equals("tumbling")
                    || node.value.equals("type")
                    || node.value.equals("unordered")
                    || node.value.equals("updating")
                    || node.value.equals("uppercase")
                    || node.value.equals("using")
                    || node.value.equals("validate")
                    || node.value.equals("value")
                    || node.value.equals("variable")
                    || node.value.equals("version")
                    || node.value.equals("weight")
                    || node.value.equals("when")
                    || node.value.equals("where")
                    || node.value.equals("wildcards")
                    || node.value.equals("window")
                    || node.value.equals("with")
                    || node.value.equals("without")
                    || node.value.equals("word")
                    || node.value.equals("words")
                    || node.value.equals("xquery")
                    || node.value.equals("zero-digit"))
                    return new NodeList(false, (List) data);
                else
                    return new NodeList(true, (List) data);
            }
            else
                return nonTransparentVisit(node, (List) data);
        }

        case JJTTYPEDECLARATION: {
            // TypeDeclaration is only valid inside a Param in XPath
            if (node.getParent().id != JJTPARAM) {
                NodeList nl = visitChildren(node, (List) data);
                return new NodeList(false, nl.getList());                
            }
            else
                return nonTransparentVisit(node, (List) data);
        }            
                
        default:
            assert(false);
            return new NodeList(false, (List) data);
        }
    }        
}
