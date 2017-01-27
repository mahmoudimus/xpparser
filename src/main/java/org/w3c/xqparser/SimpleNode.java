/*
This software or document includes material copied from or derived 
from the XPath/XQuery Applets (https://www.w3.org/2013/01/qt-applets/).
Copyright © 2013 W3C® (MIT, ERCIM, Keio, Beihang).
*/
package org.w3c.xqparser;

// ONLY EDIT THIS FILE IN THE GRAMMAR ROOT DIRECTORY!
// THE ONE IN THE ${spec}-src DIRECTORY IS A COPY!!!
public class SimpleNode implements Node {
    protected Node parent;

    protected Node[] children;

    public int id;

    protected XParser parser;
    
    public int beginLine, beginColumn, endLine, endColumn;

    public SimpleNode(int i) {
        id = i;
    }

    public SimpleNode(XParser p, int i) {
        this(i);
        parser = p;
    }

    // -------------------------------------------------------------------------
    // The methods in this section are needed to implement the Node interface.

    public void jjtOpen() {
        beginLine = parser.token.beginLine;
        int offset = 0;
        if (parser.token.image != null) {
            offset = parser.token.image.length() - 1;
        }
        beginColumn = parser.token.beginColumn + offset;
    }

    public void jjtClose() {
        endLine = parser.token.endLine;
        endColumn = parser.token.endColumn;
    }

    public void jjtSetParent(Node n) {
        parent = n;
    }

    public Node jjtGetParent() {
        return parent;
    }

    public void jjtAddChild(Node n, int i) {
        if (id == XParserTreeConstants.JJTNCNAME
                && ((SimpleNode) n).id == XParserTreeConstants.JJTQNAME) {
            m_value = ((SimpleNode) n).m_value;
            if (m_value.indexOf(':') >= 0)
                throw new TokenMgrError(
                        "Parse Error: NCName can not contain ':'!", TokenMgrError.LEXICAL_ERROR);
	    	// The TokenMgrError should perhaps more properly be a ParseException,
		// but that would require the method to declare it in its 'throws' clause,
		// which would violate the Node interface that this class must implement.
            return;
        }
        // Don't expose the functionQName as a child of a QName!
        else if (id == XParserTreeConstants.JJTQNAME
                && ((SimpleNode) n).id == XParserTreeConstants.JJTFUNCTIONQNAME) {
            m_value = ((SimpleNode) n).m_value;
            return;
        }
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }

    public Node jjtGetChild(int i) {
        return children[i];
    }

    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }

    // This method is needed because the source to jjtree
    // specifies the option VISITOR=true.
    /** Accept the visitor. * */
    public Object jjtAccept(XParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    // -------------------------------------------------------------------------

    /** Accept the visitor. * */
    public Object childrenAccept(XParserVisitor visitor, Object data) {
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                children[i].jjtAccept(visitor, data);
            }
        }
        return data;
    }

    /*
     * You can override these two methods in subclasses of SimpleNode to
     * customize the way the node appears when the tree is dumped. If your
     * output uses more than one line you should override toString(String),
     * otherwise overriding toString() is probably all you need to do.
     */

    public String toString() {
        return XParserTreeConstants.jjtNodeName[id];
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

    /*
     * Override this method if you want to customize how the node dumps out its
     * children.
     */

    public void dump(String prefix) {
        dump(prefix, System.out);
    }

    public void dump(String prefix, java.io.PrintStream ps) {
        ps.print(toString(prefix));
        printValue(ps);
        ps.print(" ["+(beginLine+1)+":"+beginColumn+" - "+endLine+":"+endColumn+"]");
        ps.println();
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode) children[i];
                if (n != null) {
                    n.dump(prefix + "   ", ps);
                }
            }
        }
    }

    // Manually inserted code begins here

    public SimpleNode getChild(int i) {
        return (SimpleNode)children[i];
    }

    public SimpleNode getParent() {
	return (SimpleNode)parent;
    }

    public String m_value;

    public void processToken(Token t) {
        m_value = t.image;
    }

    public void processValue(String val) {
        m_value = val;
    }

    public void printValue(java.io.PrintStream ps) {
        if (null != m_value)
            ps.print(" " + m_value);
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(String m_value) {
        this.m_value = m_value;
    }

    private Object _userValue;

    protected Object getUserValue() {
        return _userValue;
    }

    protected void setUserValue(Object userValue) {
        _userValue = userValue;
    }

}

