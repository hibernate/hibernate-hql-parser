/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.hibernate.hql.lucene.internal;

import java.util.Map;

import org.apache.lucene.search.Query;
import org.hibernate.hql.ast.origin.hql.resolve.path.PropertyPath;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.ast.spi.SingleEntityQueryBuilder;
import org.hibernate.hql.ast.spi.SingleEntityQueryRendererDelegate;
import org.hibernate.hql.lucene.LuceneQueryParsingResult;
import org.hibernate.search.ProjectionConstants;

/**
 * Renderer delegate which creates Lucene queries targeting a single entity or a projection of the same.
 *
 * @author Gunnar Morling
 */
public class LuceneQueryRendererDelegate extends SingleEntityQueryRendererDelegate<Query, LuceneQueryParsingResult> {

	public LuceneQueryRendererDelegate(EntityNamesResolver entityNames, SingleEntityQueryBuilder<Query> builder, Map<String, Object> namedParameters) {
		super( entityNames, builder, namedParameters );
	}

	@Override
	public LuceneQueryParsingResult getResult() {
		return new LuceneQueryParsingResult( builder.build(), targetType, projections );
	}

	@Override
	public void setPropertyPath(PropertyPath propertyPath) {
		if ( status == Status.DEFINING_SELECT ) {
			if ( propertyPath.getNodes().size() == 1 && propertyPath.getNodes().get( 0 ).isAlias() ) {
				projections.add( ProjectionConstants.THIS );
			}
			else {
				projections.add( propertyPath.asStringPathWithoutAlias() );
			}
		}
		else {
			this.propertyPath = propertyPath;
		}
	}
}
