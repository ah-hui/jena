/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.hash;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.layout2.LoaderTuplesNodes;
import com.hp.hpl.jena.sdb.layout2.NodeLayout2;
import com.hp.hpl.jena.sdb.layout2.SQLBridgeFactory2;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.StoreBaseHSQL;


public class StoreTriplesNodesHashHSQL extends StoreBaseHSQL
{
    public StoreTriplesNodesHashHSQL(SDBConnection connection, StoreDesc desc)
    {
        // HSQL can't handle complex RHS of a left join so no optional spotting. 
        super(connection, desc,
              new FmtLayout2HashHSQL(connection),
              //new LoaderHashHSQL(connection),
              new LoaderTuplesNodes(connection, TupleLoaderHashHSQL.class),
              new QueryCompilerFactoryHash(),
              new SQLBridgeFactory2(),
              new TableDescTriples(),
              new TableDescQuads(),
              new TableNodesHash()) ; 
        
        ((LoaderTuplesNodes) this.getLoader()).setStore(this);
    }

    public long getSize(Node node) {
		String lex = NodeLayout2.nodeToLex(node);
        int typeId = NodeLayout2.nodeToType(node);

        String lang = "";
        String datatype = "";

        if (node.isLiteral())
        {
            lang = node.getLiteralLanguage();
            datatype = node.getLiteralDatatypeURI();
            if (datatype == null)
                datatype = "";
        }

        long hash = NodeLayout2.hash(lex, lang, datatype, typeId);
        try {
        	ResultSet res = getConnection().exec("SELECT COUNT(*) FROM " + getQuadTableDesc().getTableName() + " WHERE g = " + hash).get();
        	res.next();
        	long result = res.getLong(1);
        	res.close();
        	return result;
        } catch (SQLException e) {
        	throw new SDBExceptionSQL("Failed to get graph size", e);
        }
	}
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */