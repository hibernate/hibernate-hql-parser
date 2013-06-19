/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
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
package org.hibernate.hql.ast.origin.hql.resolve.path;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.hql.internal.util.Strings;

/**
 * Reprents a path of properties represented by {@link PathedPropertyReferenceSource}s used in a SELECT or WHERE clause,
 * e.g. {@code foo.bar.baz}.
 *
 * @author Gunnar Morling
 */
public class PropertyPath {

	LinkedList<PathedPropertyReferenceSource> path = new LinkedList<PathedPropertyReferenceSource>();

	public void appendNode(PathedPropertyReferenceSource property) {
		path.add( property );
	}

	public PathedPropertyReferenceSource getLastNode() {
		return path.getLast();
	}

	public List<PathedPropertyReferenceSource> getNodes() {
		return path;
	}

	@Override
	public String toString() {
		return "PropertyPath [path=" + path + "]";
	}

	public String asStringPathWithoutAlias() {
		if ( path.isEmpty() ) {
			return null;
		}

		return Strings.join( getNodeNamesWithoutAlias(), "." );
	}

	private List<String> getNodeNamesWithoutAlias() {
		List<String> nodeNames = new ArrayList<String>();

		for ( PathedPropertyReferenceSource node : path ) {
			if ( !node.isAlias() ) {
				nodeNames.add( node.getName() );
			}
		}

		return nodeNames;
	}
}
