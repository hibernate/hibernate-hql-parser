/* 
 * Hibernate, Relational Persistence for Idiomatic Java
 * 
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.hibernate.jpql.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.sql.DefaultParsingContext;
import org.hibernate.sql.ast.common.ParserContext;

public class TestingParserContext extends DefaultParsingContext implements ParserContext {
	
	//map of <entityName,List entityImplementors>
	private final HashMap<String,List> knownEntities = new HashMap<String,List>();
	
	public TestingParserContext(String... validEntities) {
		for (int i = 0; i < validEntities.length; i++) {
			String entityName = validEntities[i];
			ArrayList implementors = new ArrayList();
			implementors.add( entityName );
			knownEntities.put( validEntities[i], implementors );
		}
	}

	public List getEntityImplementors(String entityName) {
		return knownEntities.get( entityName );
	}

}
