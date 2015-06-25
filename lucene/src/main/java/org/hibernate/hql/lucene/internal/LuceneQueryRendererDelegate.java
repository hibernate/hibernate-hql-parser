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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.hql.ast.origin.hql.resolve.path.PropertyPath;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.ast.spi.SingleEntityQueryBuilder;
import org.hibernate.hql.ast.spi.SingleEntityQueryRendererDelegate;
import org.hibernate.hql.lucene.LuceneQueryParsingResult;
import org.hibernate.hql.lucene.internal.builder.LucenePropertyHelper;
import org.hibernate.hql.lucene.internal.logging.Log;
import org.hibernate.hql.lucene.internal.logging.LoggerFactory;
import org.hibernate.search.ProjectionConstants;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.builtin.DoubleNumericFieldBridge;
import org.hibernate.search.bridge.builtin.FloatNumericFieldBridge;
import org.hibernate.search.bridge.builtin.IntegerNumericFieldBridge;
import org.hibernate.search.bridge.builtin.LongNumericFieldBridge;

/**
 * Renderer delegate which creates Lucene queries targeting a single entity or a projection of the same.
 *
 * @author Gunnar Morling
 */
public class LuceneQueryRendererDelegate extends SingleEntityQueryRendererDelegate<Query, LuceneQueryParsingResult> {

	private static final Log log = LoggerFactory.make();

	private final LucenePropertyHelper propertyHelper;

	private List<SortField> sortFields;

	public LuceneQueryRendererDelegate(EntityNamesResolver entityNames, SingleEntityQueryBuilder<Query> builder, Map<String, Object> namedParameters, LucenePropertyHelper propertyHelper) {
		super( entityNames, builder, namedParameters );
		this.propertyHelper = propertyHelper;
	}

	@Override
	protected void addSortField(PropertyPath propertyPath, String collateName, boolean isAscending) {
		// collateName is ignored
		if ( sortFields == null ) {
			sortFields = new ArrayList<SortField>( 5 );
		}

		int sortType = SortField.STRING;
		FieldBridge fieldBridge = propertyHelper.getFieldBridge( targetTypeName, propertyPath.getNodeNamesWithoutAlias() );
		// Determine sort type based on FieldBridgeType. SortField.BYTE and SortField.SHORT are not covered!
		if ( fieldBridge instanceof IntegerNumericFieldBridge ) {
			sortType = SortField.INT;
		}
		else if ( fieldBridge instanceof LongNumericFieldBridge ) {
			sortType = SortField.LONG;
		}
		else if ( fieldBridge instanceof DoubleNumericFieldBridge ) {
			sortType = SortField.DOUBLE;
		}
		else if ( fieldBridge instanceof FloatNumericFieldBridge ) {
			sortType = SortField.FLOAT;
		}
		sortFields.add( new SortField( propertyPath.asStringPathWithoutAlias(), sortType, !isAscending ) );
	}

	@Override
	public LuceneQueryParsingResult getResult() {
		Sort sort = null;
		if ( sortFields != null ) {
			sort = new Sort( sortFields.toArray( new SortField[sortFields.size()] ) );
		}
		return new LuceneQueryParsingResult( builder.build(), targetTypeName, targetType, projections, sort );
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
