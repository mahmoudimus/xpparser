/*
  A converter to XQueryX for XPath 3.0.
  Based on XQueryXConverterBase-xquery30.java, which is Copyright W3C.

  @author Sylvain Schmitz <schmitz@lsv.fr>
 */
package org.w3c.xqparser;

import java.util.Stack;

/**
 * Transforms an XQuery AST into a XQueryX XML stream.
 */
public class XQueryXConverter_xpath30 extends XQueryXConverter {

    private static final String[][] xsd_attributes = {
        {"xmlns:xqx", "http://www.w3.org/2005/XQueryX"},
        {"xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"},
        {"xsi:schemaLocation",
         "http://www.w3.org/2005/XQueryX xpathx.xsd"}
    };
    Stack _openXMLElemStack = new Stack();

    public XQueryXConverter_xpath30(ConversionController cc, XMLWriter xw)
    {
        super(cc, xw);
    }
    
    /**
     * Process a node in the AST and its descendants.
     * @param node  The node to be processed.
     * @return true
     */
    protected boolean transformNode(final SimpleNode node) {
        int id = node.id;
        String xqx_element_name_g = mapNodeIdToXqxElementName(id);
        // The "g" stands for both:
        // "generated" (in that the name is generated mechanically from the
        //     name of the non-terminal that this node is an instance of); and
        // "guess" (in that we don't know at this point whether the name will
        //     be useful/appropriate).

        switch (id) {
        case JJTSTART:
            xw.putXMLDecl();
            cc.transformChildren(node);
            return true;
            
        case JJTXPATH:
            xw.putStartTag(node, "xqx:module", xsd_attributes, true);
            xw.putStartTag(node, "xqx:mainModule");
            xw.putStartTag(node, "xqx:queryBody");
            cc.transformChildren(node);
            xw.putEndTag(node);
            xw.putEndTag(node);
            xw.putEndTag(node);
            return true;
            
        case JJTPARAMLIST:
        case JJTPARAM:
            xw.putStartTag(node, xqx_element_name_g);
            cc.transformChildren(node);
            xw.putEndTag(node);
            return true;
            
        case JJTFUNCTIONBODY:
        case JJTENCLOSEDEXPR:
            cc.transformChildren(node);
            return true;
            
        case JJTLBRACE:
        case JJTRBRACE:
            return true;
            
        case JJTEXPR:
            if (getNumExprChildren(node) > 1) {
                xw.putStartTag(node, "xqx:sequenceExpr");
                cc.transformChildren(node);
                xw.putEndTag(node);
            }
            else {
                cc.transformChildren(node);
            }
            return true;
            
        case JJTVOID:
            // node sometimes has children
            if (node.jjtGetNumChildren() == 0) {
                xw.putEmptyElement(node, xqx_element_name_g);
            } else {
                xw.putStartTag(node, xqx_element_name_g);
                cc.transformChildren(node);
                xw.putEndTag(node);
            }
            return true;
            
        case JJTFOREXPR: {
            xw.putStartTag(node, "xqx:flworExpr");
            xw.putStartTag(node, "xqx:forClause");
            int n_children = node.jjtGetNumChildren();
            for (int i = 0; i < n_children - 1; i++)
                cc.transform(node.getChild(i));
            xw.putEndTag(node);
            xw.putStartTag(node, "xqx:returnClause");
            cc.transform(node.getChild(n_children - 1));
            xw.putEndTag(node);
            xw.putEndTag(node);
            return true;
        }
            
        case JJTSIMPLEFORBINDING:
            xw.putStartTag(node, "xqx:forClauseItem");
            xw.putStartTag(node, "xqx:typedVariableBinding");
            cc.transformChildren(node.getChild(0));
            xw.putEndTag(node);
            xw.putStartTag(node, "xqx:forExpr");
            cc.transform(node.getChild(1));
            xw.putEndTag(node);
            xw.putEndTag(node);
            return true;
            
        case JJTLETEXPR: {
            xw.putStartTag(node, "xqx:flworExpr");
            xw.putStartTag(node, "xqx:letClause");
            int n_children = node.jjtGetNumChildren();
            for (int i = 0; i < n_children - 1; i++)
                cc.transform(node.getChild(i));
            xw.putEndTag(node);
            xw.putStartTag(node, "xqx:returnClause");
            cc.transform(node.getChild(n_children - 1));
            xw.putEndTag(node);
            xw.putEndTag(node);
            return true;
        }
            
        case JJTSIMPLELETCLAUSE:
            cc.transformChildren(node);
            return true;
            
        case JJTSIMPLELETBINDING:
            xw.putStartTag(node, "xqx:letClauseItem");
            xw.putStartTag(node, "xqx:typedVariableBinding");
            cc.transformChildren(node.getChild(0));
            xw.putEndTag(node);
            xw.putStartTag(node, "xqx:letExpr");
            cc.transform(node.getChild(1));
            xw.putEndTag(node);
            xw.putEndTag(node);
            return true;
            
        case JJTQUANTIFIEDEXPR: {
            xw.putStartTag(node, xqx_element_name_g);
            xw.putSimpleElement(node, "xqx:quantifier", node.m_value);
            
            int n = node.jjtGetNumChildren();
            for (int i = 0; i < n - 1;) {
                SimpleNode typedVariableBinding = node.getChild(i);
                i++;
                
                xw.putStartTag(node, "xqx:quantifiedExprInClause");
                xw.putStartTag(node, "xqx:typedVariableBinding");
                transform_name(typedVariableBinding.getChild(0),
                               typedVariableBinding.id);
                
                SimpleNode nextChild = node.getChild(i);
                i++;
                xw.putEndTag(node); // xqx:typedVariableBinding
                
                xw.putStartTag(node, "xqx:sourceExpr");
                
                cc.transform(nextChild);
                
                xw.putEndTag(node); // xqx:sourceExpr
                xw.putEndTag(node); // xqx:quantifiedExprInClause
                
            }
            xw.putStartTag(node, "xqx:predicateExpr");
            cc.transformChildren(node, n - 1, n - 1);
            xw.putEndTag(node); // xqx:predicateExpr
            xw.putEndTag(node); // xqx:quantifiedExpr
            return true;
        }
            
        case JJTIFEXPR:
            xw.putStartTag(node, "xqx:ifThenElseExpr");
            
            xw.putStartTag(node, "xqx:ifClause");
            cc.transformChildren(node, 0, 0);
            xw.putEndTag(node);
            
            xw.putStartTag(node, "xqx:thenClause");
            cc.transformChildren(node, 1, 1);
            xw.putEndTag(node);
            
            xw.putStartTag(node, "xqx:elseClause");
            cc.transformChildren(node, 2, 2);
            xw.putEndTag(node);
            
            xw.putEndTag(node);
            return true;
            
        case JJTOREXPR:
        case JJTANDEXPR:
        case JJTCOMPARISONEXPR:
        case JJTSTRINGCONCATEXPR:
        case JJTADDITIVEEXPR:
        case JJTMULTIPLICATIVEEXPR:
        case JJTUNIONEXPR:
        case JJTINTERSECTEXCEPTEXPR: {
            String elemName;
            switch (id) {
            case JJTSTRINGCONCATEXPR:
                elemName = "xqx:stringConcatenateOp";
                break;
            case JJTADDITIVEEXPR: {
                String op = node.m_value;
                if (op.equals("+"))
                    elemName = "xqx:addOp";
                else if (op.equals("-"))
                    elemName = "xqx:subtractOp";
                else
                    elemName = "JJTADDITIVEEXPR UNKNOWN EXPR!";
            }
                break;
            case JJTMULTIPLICATIVEEXPR: {
                String op = node.m_value;
                if (op.equals("*"))
                    elemName = "xqx:multiplyOp";
                else if (op.equals("div"))
                    elemName = "xqx:divOp";
                else if (op.equals("idiv"))
                    elemName = "xqx:idivOp";
                else if (op.equals("mod"))
                    elemName = "xqx:modOp";
                else
                    elemName = "JJTMULTIPLICATIVEEXPR UNKNOWN EXPR: " + op;
            }
                break;
            case JJTUNIONEXPR:
                elemName = "xqx:unionOp";
                break;
            case JJTINTERSECTEXCEPTEXPR: {
                String op = node.m_value;
                if (op.equals("intersect"))
                    elemName = "xqx:intersectOp";
                else if (op.equals("except"))
                    elemName = "xqx:exceptOp";
                else
                    elemName = "JJTINTERSECTEXCEPTEXPR UNKNOWN EXPR: " + op;
            }
                break;
            case JJTANDEXPR:
                elemName = "xqx:andOp";
                break;
            case JJTOREXPR:
                elemName = "xqx:orOp";
                break;
            case JJTCOMPARISONEXPR: {
                String op = node.m_value;
                if (op.equals("eq"))
                    elemName = "xqx:eqOp";
                else if (op.equals("ne"))
                    elemName = "xqx:neOp";
                else if (op.equals("lt"))
                    elemName = "xqx:ltOp";
                else if (op.equals("le"))
                    elemName = "xqx:leOp";
                else if (op.equals("gt"))
                    elemName = "xqx:gtOp";
                else if (op.equals("ge"))
                    elemName = "xqx:geOp";
                else if (op.equals("="))
                    elemName = "xqx:equalOp";
                else if (op.equals("!="))
                    elemName = "xqx:notEqualOp";
                else if (op.equals("<"))
                    elemName = "xqx:lessThanOp";
                else if (op.equals("<="))
                    elemName = "xqx:lessThanOrEqualOp";
                else if (op.equals(">"))
                    elemName = "xqx:greaterThanOp";
                else if (op.equals(">="))
                    elemName = "xqx:greaterThanOrEqualOp";
                else if (op.equals("is"))
                    elemName = "xqx:isOp";
                else if (op.equals("<<"))
                    elemName = "xqx:nodeBeforeOp";
                else if (op.equals(">>"))
                    elemName = "xqx:nodeAfterOp";
                else
                    elemName = "JJTCOMPARISONEXPR UNKNOWN: " + op;
            }
                break;
                    default:
                        elemName = "???";
                        break;
            }
            xw.putStartTag(node, elemName);
            
            {
                xw.putStartTag(node, "xqx:firstOperand");
                cc.transform(node.getChild(0));
                xw.putEndTag(node);
            }
            {
                xw.putStartTag(node, "xqx:secondOperand");
                cc.transform(node.getChild(1));
                xw.putEndTag(node);
            }
            
            xw.putEndTag(node);
            return true;
        }
            
        case JJTRANGEEXPR: {
            xw.putStartTag(node, "xqx:rangeSequenceExpr");
            {
                xw.putStartTag(node, "xqx:startExpr");
                cc.transform(node.getChild(0));
                xw.putEndTag(node);
            }
            {
                xw.putStartTag(node, "xqx:endExpr");
                cc.transform(node.getChild(1));
                xw.putEndTag(node);
            }
            
            xw.putEndTag(node);
            return true;
        }
            
        case JJTINSTANCEOFEXPR:
        case JJTTREATEXPR:
        case JJTCASTABLEEXPR:
        case JJTCASTEXPR: {
            String xqx_element_name =
                (id == JJTINSTANCEOFEXPR)
                ? "xqx:instanceOfExpr"
                : xqx_element_name_g;
            xw.putStartTag(node, xqx_element_name);
            xw.putStartTag(node, "xqx:argExpr");
            cc.transformChildren(node, 0, 0);
            xw.putEndTag(node);
            if (id != JJTCASTEXPR && id != JJTCASTABLEEXPR) {
                xw.putStartTag(node, "xqx:sequenceType");
                cc.transformChildren(node, 1, 1);
                xw.putEndTag(node);
            } else {
                cc.transformChildren(node, 1, 1);
            }
            xw.putEndTag(node);
            return true;
        }
            
        case JJTUNARYEXPR: {
            int nUnarys = 0;
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode child = node.getChild(i);
                if (child.id == JJTPLUS) {
                    xw.putStartTag(node, "xqx:unaryPlusOp");
                    xw.putStartTag(node, "xqx:operand");
                    nUnarys++;
                } else if (child.id == JJTMINUS) {
                    xw.putStartTag(node, "xqx:unaryMinusOp");
                    xw.putStartTag(node, "xqx:operand");
                    nUnarys++;
                } else {
                    cc.transform(child);
                    for (int j = 0; j < nUnarys; j++) {
                        xw.putEndTag(node);
                        xw.putEndTag(node);
                    }
                    break;
                }
            }
            return true;
        }
        case JJTMINUS:
        case JJTPLUS:
            return true;
            
        case JJTSIMPLEMAPEXPR:
            // XQueryConverterBase-xquery30 binarizes this, but the
            // most recent schema for XqueryX allows unboundedly many
            // children.
            xw.putStartTag(node, "xqx:simpleMapExpr");
            cc.transformChildren(node);
            xw.putEndTag(node);
            return true;
            
        case JJTPATHEXPR: {
            // First, check whether we can translate this node
            // without an xqx:pathExpr element.
            // If the PathExpr's parent is a SimpleMapExpr,
            // then we can't shortcut, because xqx:simpleMapExpr's children
            // must be xqx:pathExprs.
            if (getParentID(node) != JJTSIMPLEMAPEXPR
                && node.jjtGetNumChildren() == 1) {
                SimpleNode only_child = node.getChild(0);
                if (only_child.id == JJTPOSTFIXEXPR
                    && only_child.jjtGetNumChildren() == 1) {
                    SimpleNode only_grandchild = only_child.getChild(0);
                    // only_grandchild is some kind of PrimaryExpr node,
                    // so it doesn't need to be in a pathExpr.
                    cc.transform(only_grandchild);
                    return true;
                }
            }
            xw.putStartTag(node, xqx_element_name_g);
            
            String xqx_element_name_for_next_step = "xqx:stepExpr";
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                SimpleNode child = node.getChild(i);
                if(child.id == JJTSLASH){
                    if (i==0) xw.putEmptyElement(child, "xqx:rootExpr");
                    xqx_element_name_for_next_step = "xqx:stepExpr";
                }
                else if(child.id == JJTSLASHSLASH){
                    if (i==0) xw.putEmptyElement(child, "xqx:rootExpr");
                    xw.putStartTag(child, "xqx:stepExpr");
                    xw.putSimpleElement(child, "xqx:xpathAxis", "descendant-or-self");
                    xw.putEmptyElement(child, "xqx:anyKindTest");
                    xw.putEndTag(child);
                    xqx_element_name_for_next_step = "xqx:stepExpr";
                }
                else {
                    assert child.id == JJTAXISSTEP || child.id == JJTPOSTFIXEXPR;
                    xw.putStartTag(child, xqx_element_name_for_next_step);
                    cc.transform(child);
                    xw.putEndTag(child);
                    xqx_element_name_for_next_step = null;
                }
            }
            xw.putEndTag(node);
            return true;
        }
            
        case JJTSLASH:
        case JJTSLASHSLASH:
            // handled in JJTPATHEXPR
            return true;
            
        case JJTAXISSTEP:
            cc.transformChildren(node);
            return true;
            
        case JJTFORWARDAXIS:
        case JJTREVERSEAXIS: {
                String axisStr = node.m_value;
                xw.putSimpleElement(node, "xqx:xpathAxis", axisStr);
                return true;
            }
            
        case JJTABBREVFORWARDSTEP: {
            //  AbbrevForwardStep ::= "@"? NodeTest
            SimpleNode nodeTest = node.getChild(0);
            String optionalAttribIndicator = node.m_value;
            if (optionalAttribIndicator != null
                && optionalAttribIndicator.equals("@")) {
                // "The attribute axis attribute:: can be abbreviated by @."
                xw.putSimpleElement(node, "xqx:xpathAxis", "attribute");
            } else {
                // "If the axis name is omitted from an axis step,
                // the default axis is child, with two exceptions:
                // if the axis step contains an AttributeTest or SchemaAttributeTest
                // then the default axis is attribute;
                // if the axis step contains namespace-node()
                // then the default axis is namespace."
                // [But XQuery does not support the namespace axis,
                // so the second exception does not apply.]
                SimpleNode nodeTest_child = nodeTest.getChild(0);
                if (
                    nodeTest_child.id == JJTATTRIBUTETEST
                    || nodeTest_child.id == JJTSCHEMAATTRIBUTETEST
                    ) {
                    xw.putSimpleElement(node, "xqx:xpathAxis", "attribute");
                } else {
                    xw.putSimpleElement(node, "xqx:xpathAxis", "child");
                }
            }
            
            // The NodeTest within the AbbrevForwardStep:
            cc.transform(nodeTest);
            return true;
        }
            
        case JJTABBREVREVERSESTEP:
            xw.putSimpleElement(node, "xqx:xpathAxis", "parent");
            xw.putEmptyElement(node, "xqx:anyKindTest");
            return true;
            
        case JJTNODETEST:
            cc.transformChildren(node);
            return true;
            
        case JJTNAMETEST:
            if (getChildID(node, 0) == JJTWILDCARD) {
                cc.transformChildren(node);
                return true;
            } else {
                transform_name(node.getChild(0), "xqx:nameTest");
                return true;
            }
            
        case JJTWILDCARD:
            if (node.jjtGetNumChildren() == 0) {
                xw.putEmptyElement(node, "xqx:Wildcard");
            } else {
                xw.putStartTag(node, "xqx:Wildcard");
                cc.transformChildren(node);
                xw.putEndTag(node);
                }
            return true;
            
        case JJTNCNAMECOLONSTAR: {
            String ncname_colon_star = node.m_value;
            int i = ncname_colon_star.indexOf(':');
            String ncname = ncname_colon_star.substring(0, i);
            String star = ncname_colon_star.substring(i + 1);
            assert star.equals("*");
            xw.putSimpleElement(node, "xqx:NCName", ncname);
            xw.putEmptyElement(node, "xqx:star");
            return true;
        }
                
        case JJTSTARCOLONNCNAME: {
            String star_colon_ncname = node.m_value;
            int i = star_colon_ncname.indexOf(':');
            String star = star_colon_ncname.substring(0, i);
            String ncname = star_colon_ncname.substring(i + 1);
            assert star.equals("*");
            xw.putEmptyElement(node, "xqx:star");
            xw.putSimpleElement(node, "xqx:NCName", ncname);
            return true;
        }
            
        case JJTURIQUALIFIEDSTAR: {
            String uri_qualified_star = node.m_value;
            int i = uri_qualified_star.lastIndexOf('}');
            String uri = uri_qualified_star.substring(2, i);
            String star = uri_qualified_star.substring(i + 1);
            assert star.equals("*");
            xw.putSimpleElement(node, "xqx:uri", uri);
            xw.putEmptyElement(node, "xqx:star");
            return true;
        }
            
        case JJTARGUMENTLIST:
            xw.putStartTag(node, "xqx:arguments");
            cc.transformChildren(node);
            xw.putEndTag(node);
            return true;
            
        case JJTARGUMENT:
            cc.transformChildren(node);
            return true;
            
        case JJTARGUMENTPLACEHOLDER:
            xw.putEmptyElement(node, "xqx:argumentPlaceholder");
            return true;
            
        case JJTPREDICATELIST:
            xw.putStartTag(node, "xqx:predicates");
            cc.transformChildren(node);
            xw.putEndTag(node);
            return true;
            
        case JJTPREDICATE:
            cc.transformChildren(node);
            return true;
            
        case JJTSTRINGLITERAL:
        case JJTINTEGERLITERAL:
        case JJTDECIMALLITERAL:
        case JJTDOUBLELITERAL: {            
            String elemName;
            switch (id) {
            case JJTINTEGERLITERAL:
                elemName = "xqx:integerConstantExpr";
                break;
            case JJTDECIMALLITERAL:
                elemName = "xqx:decimalConstantExpr";
                break;
            case JJTDOUBLELITERAL:
                elemName = "xqx:doubleConstantExpr";
                break;
            case JJTSTRINGLITERAL:
                elemName = "xqx:stringConstantExpr";
                break;
            default:
                elemName = "UNKNOWN!";
                break;
            }
            
            String content =
                (id == JJTSTRINGLITERAL)
                ? undelimitStringLiteral(node)
                : node.m_value;
            
            xw.putStartTag(node, elemName);
            
            xw.putSimpleElement(node, "xqx:value", content);
            
            xw.putEndTag(node);
            return true;
        }
            
        case JJTVARNAME:
            xw.putStartTag(node, "xqx:varRef");
            transform_name(node.getChild(0), "xqx:name");
            xw.putEndTag(node);
            return true;
            
        case JJTPARENTHESIZEDEXPR:
            if (node.jjtGetNumChildren() == 0) {
                xw.putEmptyElement(node, "xqx:sequenceExpr");
                return true;
            } else {
                cc.transformChildren(node);
                return true;
            }
            
        case JJTANYKINDTEST:
        case JJTTEXTTEST:
        case JJTCOMMENTTEST:
        case JJTCONTEXTITEMEXPR:
            xw.putEmptyElement(node, xqx_element_name_g);
            return true;
            
        case JJTNAMESPACENODETEST:
            xw.putEmptyElement(node, "xqx:namespaceTest");
            return true;
            
        case JJTSINGLETYPE: {
            boolean optionality = (node.m_value != null);
            xw.putStartTag(node, xqx_element_name_g);
            cc.transformChildren(node);
            if (optionality) {
                xw.putEmptyElement(node, "xqx:optional");
            }
            xw.putEndTag(node);
            return true;
        }
            
            
        case JJTFUNCTIONCALL:
            xw.putStartTag(node, "xqx:functionCallExpr");
            cc.transformChildren(node);
            xw.putEndTag(node);
            return true;
            
        case JJTFUNCTIONITEMEXPR:
            cc.transformChildren(node);
            return true;
            
        case JJTNAMEDFUNCTIONREF:
            xw.putStartTag(node, "xqx:namedFunctionRef");
            cc.transformChildren(node);
            xw.putEndTag(node);
            return true;
                
        case JJTINLINEFUNCTIONEXPR: {
            xw.putStartTag(node, "xqx:inlineFunctionExpr");
            int start = 0;
            int end = node.jjtGetNumChildren() - 1;
            
            if (getChildID(node, start) == JJTPARAMLIST) {
                cc.transformChildren(node, start, start);
                start++;
            } else {
                xw.putEmptyElement(node, "xqx:paramList");
            }
            
            if (getChildID(node, start) == JJTSEQUENCETYPE) {
                xw.putStartTag(node, "xqx:typeDeclaration");
                cc.transformChildren(node, start, start++);
                xw.putEndTag(node);
            }
            
            xw.putStartTag(node, "xqx:functionBody");
            cc.transformChildren(node, start, end);
            xw.putEndTag(node); // xqx:functionBody
            
            xw.putEndTag(node); // xqx:inlineFunctionExpr
            
            return true;
        }
            
        case JJTTYPEDECLARATION:
            xw.putStartTag(node, xqx_element_name_g);
            cc.transformChildren(node);
            xw.putEndTag(node);
            return true;
            
        case JJTSEQUENCETYPE: {
            int pid = getParentID(node);
            boolean shouldBeSeqType = (pid == JJTTYPEDFUNCTIONTEST);
            if (shouldBeSeqType) {
                xw.putStartTag(node, "xqx:sequenceType");
            }
            if (node.m_value != null && node.m_value.equals("empty-sequence")) {
                xw.putEmptyElement(node, "xqx:voidSequenceType");
            } else {
                cc.transformChildren(node);
            }
            if (shouldBeSeqType) {
                xw.putEndTag(node);
            }
            return true;
        }
            
        case JJTOCCURRENCEINDICATOR:
            xw.putSimpleElement(node, xqx_element_name_g, node.m_value);
            return true;
            
        case JJTITEMTYPE:
            if (node.m_value != null && node.m_value.equals("item")) {
                xw.putEmptyElement(node, "xqx:anyItemType");
                return true;
            } else {
                cc.transformChildren(node);
                return true;
            }
            
        case JJTATOMICORUNIONTYPE:
            // handled in JJTQNAME
            cc.transformChildren(node);
            return true;
            
        case JJTDOCUMENTTEST:
        case JJTATTRIBUTETEST:
        case JJTELEMENTTEST:
            // node sometimes has children
            if (node.jjtGetNumChildren() == 0) {
                xw.putEmptyElement(node, xqx_element_name_g);
            } else {
                xw.putStartTag(node, xqx_element_name_g);
                cc.transformChildren(node);
                xw.putEndTag(node);
            }
            return true;
            
        case JJTPITEST: {
            if (node.jjtGetNumChildren() > 0) {
                xw.putStartTag(node, "xqx:piTest");
                SimpleNode child = node.getChild(0);
                String content =
                    (child.id == JJTSTRINGLITERAL)
                    ? undelimitStringLiteral(child)
                    : child.m_value;
                xw.putSimpleElement(child, "xqx:piTarget", content);
                
                xw.putEndTag(node);
            } else {
                xw.putEmptyElement(node, "xqx:piTest");
            }
            return true;
        }
            
        case JJTATTRIBNAMEORWILDCARD:
        case JJTELEMENTNAMEORWILDCARD:
            if (node.m_value != null && node.m_value.equals("*")) {
                String xqx_element_name =
                    mapNodeIdToXqxElementName(
                                              (id == JJTATTRIBNAMEORWILDCARD)
                                              ? JJTATTRIBUTENAME
                                              : JJTELEMENTNAME
                                              );
                xw.putStartTag(node, xqx_element_name);
                xw.putEmptyElement(node, "xqx:star");
                xw.putEndTag(node);
                return true;
            } else {
                cc.transformChildren(node);
                return true;
            }
            
        case JJTATTRIBUTENAME:
        case JJTELEMENTNAME:
        case JJTATTRIBUTEDECLARATION:
        case JJTELEMENTDECLARATION:
        case JJTPARENTHESIZEDITEMTYPE:
            xw.putStartTag(node, xqx_element_name_g);
            cc.transformChildren(node);
            xw.putEndTag(node);
            return true;
            
        case JJTURIQUALIFIEDNAME:
        case JJTNCNAME:
        case JJTQNAME: {
                int pid = getParentID(node);
                String xqx_element_name;
                if (pid == JJTTYPENAME)
                {
                    // The TypeName's parent could be:
                    //  - an AttributeTest, or an ElementTest,
                    //    for which xqx:typeName is the correct result,
                    //  or
                    //  - a SimpleTypeName (in a SingleType), and
                    //    backwards-compatibility for xqx:singleType means
                    //    that the correct result is xqx:atomicType.
                    if (node.getParent().getParent().id == JJTSIMPLETYPENAME)
                        xqx_element_name = "xqx:atomicType";
                    else
                        xqx_element_name = "xqx:typeName";
                }
                else if (pid == JJTATOMICORUNIONTYPE) // XXX
                    xqx_element_name = "xqx:atomicType";
                else if (pid == JJTFUNCTIONCALL || pid == JJTNAMEDFUNCTIONREF )
                    xqx_element_name = "xqx:functionName";
                else
                    xqx_element_name = "xqx:varName";
                transform_name(node, xqx_element_name);
                return true;
            }
            
        case JJTSCHEMAATTRIBUTETEST:
            {
                /* Parse tree is:
                 * |                           SchemaAttributeTest
                 * |                              AttributeDeclaration
                 * |                                 AttributeName
                 * |                                    QName foo
                 */
                
                SimpleNode qn = node.getChild(0).getChild(0).getChild(0);
                transform_name(qn, "xqx:schemaAttributeTest");
                return true;
             }
            
        case JJTSCHEMAELEMENTTEST:
            {
               /* Parse tree is:
                * |                           SchemaElementTest
                * |                              ElementDeclaration
                * |                                 ElementName
                * |                                    QName notDeclared:ncname
                */

                SimpleNode qn = node.getChild(0).getChild(0).getChild(0);
                transform_name(qn, "xqx:schemaElementTest");
                return true;
             }
        case JJTSIMPLETYPENAME:
        case JJTTYPENAME:
            cc.transformChildren(node);
            return true;
            
        case JJTFUNCTIONTEST: {
            int lastChildId = getChildID(node, node.jjtGetNumChildren()-1);
            String lastChild_xqx_element_name_g
                = mapNodeIdToXqxElementName(lastChildId);
            xw.putStartTag(node, lastChild_xqx_element_name_g);
            cc.transformChildren(node);
            xw.putEndTag(node);
            return true;
        }
            
        case JJTANYFUNCTIONTEST:
            return true;
            
        case JJTTYPEDFUNCTIONTEST: {
            int nChildren = node.jjtGetNumChildren();
            xw.putStartTag(node, "xqx:paramTypeList");
            cc.transformChildren(node, 0, nChildren - 2);
            xw.putEndTag(node);
            cc.transformChildren(node, nChildren - 1);
            return true;
        }
            
        case JJTFUNCTIONQNAME:
            transform_name(node, "xqx:functionName");
            return true;
            
        case JJTPOSTFIXEXPR: {
            int end = node.jjtGetNumChildren() - 1;
            filterPredicate(node, 0, end);
            return true;
        }
            
        default: {
            String context = "";
            SimpleNode parent = node;
            System.err.println("Unknown ID: "
                               + XParserTreeConstants.jjtNodeName[id]);
            while ((parent = parent.getParent()) != null) {
                context = parent.toString()
                    + ((context.length() == 0) ? "" : (", " + context));
            }
            System.err.println("Context is " + context);
            cc.transformChildren(node);
            return true;
        }
        // unreachable
        }
    }
    
    protected void filterPredicate(SimpleNode node, int start, int end) {

        int childID = getChildID(node, start);
        SimpleNode child = node.getChild(start);

        int predicates = 0;
        for (int j = end; getChildID(node, j) == JJTPREDICATE; j--)
           predicates++;

        xw.putStartTag(node, "xqx:filterExpr");

        dynamicFunctionInvocation(node, start, end - predicates);

        xw.putEndTag(node);

        if (predicates != 0) {
           xw.putStartTag(node, "xqx:predicates");
           cc.transformChildren(node, end - predicates + 1, end);
           xw.putEndTag(node);
        }

    }

    protected void dynamicFunctionInvocation(SimpleNode node, int start, int end) {

        int childID = getChildID(node, start);
        SimpleNode child = node.getChild(start);

        if (end == start) {
           if (childID == JJTPARENTHESIZEDEXPR) {
              xw.putStartTag(node, "xqx:sequenceExpr");
              cc.transform(child);
              xw.putEndTag(node);
              }
           else
              cc.transform(child);
        }
        else {

           SimpleNode dfi = node.getChild(end--);

           int predicates = 0;
           for (int j = end; getChildID(node, j) == JJTPREDICATE; j--)
              predicates++;

           xw.putStartTag(node, "xqx:dynamicFunctionInvocationExpr");

           xw.putStartTag(node, "xqx:functionItem");

           dynamicFunctionInvocation(node, start, end - predicates);

           xw.putEndTag(node);

           if (predicates != 0) {
              xw.putStartTag(node, "xqx:predicates");
              cc.transformChildren(node, end - predicates + 1, end);
              xw.putEndTag(node);
           }

           if (dfi.jjtGetNumChildren() != 0) {
              xw.putStartTag(node, "xqx:arguments");
              cc.transformChildren(dfi);
              xw.putEndTag(node);
           }

           xw.putEndTag(node);
        }

    }
    // override XQueryXConverter.transform_name
    protected void transform_name(SimpleNode name_node, String xqx_element_name)
    {
        if (name_node.id == JJTURIQUALIFIEDNAME)
        {
            String uqn_string = name_node.m_value;
            int rbrace_index = uqn_string.lastIndexOf('}');
            String uri = uqn_string.substring(2, rbrace_index);
            String local_name = uqn_string.substring(rbrace_index+1);
            String[][] attributes = new String[][] {{"xqx:URI", uri}};
            xw.putStartTag(name_node, xqx_element_name, attributes, false);
            xw.putTextEscaped(local_name);
            xw.putEndTag(name_node, false);
        }
        else
        {
            super.transform_name(name_node, xqx_element_name);
        }
    }
    
    protected int getNumExprChildren(SimpleNode node) {
        return getNumExprChildren(node, 0);
    }

    protected int getNumExprChildren(SimpleNode node, int start) {
        int count = 0;
        int n = node.jjtGetNumChildren();
        for (int i = start; i < n; i++) {
            SimpleNode child = node.getChild(i);
            if (child.id != JJTLBRACE && child.id != JJTRBRACE)
                count++;
        }
        return count;
    }
}
