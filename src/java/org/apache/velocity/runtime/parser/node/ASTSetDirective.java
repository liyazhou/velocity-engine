/* Generated By:JJTree: Do not edit this line. ASTSetDirective.java */

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/**
 * Node for the #set directive
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ASTSetDirective.java,v 1.9 2000/12/08 03:19:00 geirm Exp $
 */

package org.apache.velocity.runtime.parser.node;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.Context;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.exception.ReferenceException;
import org.apache.velocity.runtime.parser.*;

public class ASTSetDirective extends SimpleNode
{
    private Node right;
    private ASTReference left;
    boolean bDeprecated = true;
    String lit = "";

    public ASTSetDirective(int id)
    {
        super(id);
    }

    public ASTSetDirective(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *  simple init.  We can get the RHS and LHS as the the tree structure is static
     */
    public Object init(Context context, Object data) throws Exception
    {
        /*
         *  init the tree correctly
         */

        super.init( context, data );

        right = getRightHandSide();
        left = getLeftHandSide();

        /*
         *   yechy but quick : so we can warn users they are using what will be 
         *  the deprecated version of #set
         *  If we see a '(', then it is [most likely] the inline kind
         *
         */
        Token t = getFirstToken();
        Token tLast = getLastToken();

        while( t != null && t != tLast ) 
        {
            lit += t;

            if (t.toString().indexOf("(") != -1)
            {
                bDeprecated = false;
                break;
            }

            t = t.next;
        }
 
        return data;
    }        

    /**
     *   puts the value of the RHS into the context under the key of the LHS
     */
    public boolean render(Context context, Writer writer)
        throws IOException
    {
        /*
         *  get the RHS node, and it's value
         */

        Object value = right.value(context);

        /*
         * it's an error if we don't have a value of some sort
         */

        if ( value  == null)
        {
            Runtime.error(new ReferenceException("#set", right));
            return false;
        }                

        /*
         *  if the LHS is simple, just punch the value into the context
         *  otherwise, use the setValue() method do to it.
         *  Maybe we should always use setValue()
         */
        
        if (left.jjtGetNumChildren() == 0)
            context.put(left.getFirstToken().image.substring(1), value);
        else
            left.setValue(context, value);
    
        if (bDeprecated)
            Runtime.warn("Deprecated form of #set directive in " + context.getCurrentTemplateName() 
                     + " [line "+left.getLine() + "] : " + lit);

        return true;
    }

    /**
     *  returns the ASTReference that is the LHS of the set statememt
     */
    private ASTReference getLeftHandSide()
    {
        return (ASTReference) jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
    }

    /**
     *  returns the RHS Node of the set statement
     */
    private Node getRightHandSide()
    {
        return jjtGetChild(0).jjtGetChild(0).jjtGetChild(1).jjtGetChild(0);
    }
}
