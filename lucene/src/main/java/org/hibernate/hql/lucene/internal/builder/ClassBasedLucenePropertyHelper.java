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
package org.hibernate.hql.lucene.internal.builder;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.List;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.hibernate.hql.ast.spi.EntityNamesResolver;
import org.hibernate.hql.lucene.internal.logging.Log;
import org.hibernate.hql.lucene.internal.logging.LoggerFactory;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.engine.metadata.impl.DocumentFieldMetadata;
import org.hibernate.search.engine.metadata.impl.EmbeddedTypeMetadata;
import org.hibernate.search.engine.metadata.impl.PropertyMetadata;
import org.hibernate.search.engine.metadata.impl.TypeMetadata;
import org.hibernate.search.engine.spi.EntityIndexBinding;
import org.hibernate.search.spi.SearchIntegrator;

/**
 * Provides functionality for dealing with Lucene-mapped properties of indexed Java types.
 *
 * @author Gunnar Morling
 */
public class ClassBasedLucenePropertyHelper extends LucenePropertyHelper {

	private static final Log log = LoggerFactory.make();

	private final SearchIntegrator searchFactory;
	private final EntityNamesResolver entityNames;

	public ClassBasedLucenePropertyHelper(SearchIntegrator searchFactory, EntityNamesResolver entityNames) {
		this.searchFactory = searchFactory;
		this.entityNames = entityNames;
	}

	@Override
	public FieldBridge getFieldBridge(String entityType, List<String> propertyPath) {
		Class<?> type = getType( entityType );
		String[] propertyPathAsArray = propertyPath.toArray( new String[propertyPath.size()] );

		EntityIndexBinding entityIndexBinding = getIndexBinding( searchFactory, type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPathAsArray ) ) {
			return entityIndexBinding.getDocumentBuilder().getIdBridge();
		}

		PropertyMetadata metadata = getLeafTypeMetadata( type, propertyPathAsArray ).getPropertyMetadataForProperty( propertyPathAsArray[propertyPathAsArray.length - 1] );

		// TODO Consider properties with several fields
		return getFieldMetadata( metadata ).iterator().next().getFieldBridge();
	}

	private Class<?> getType(String typeName) {
		Class<?> type = entityNames.getClassFromName( typeName );
		if ( type == null ) {
			throw new IllegalStateException( "Unknown entity name " + type );
		}

		return type;
	}

	public boolean exists(Class<?> type, List<String> propertyPath) {
		return exists( type, propertyPath.toArray( new String[propertyPath.size()] ) );
	}

	public boolean exists(Class<?> type, String... propertyPath) {
		EntityIndexBinding entityIndexBinding = getIndexBinding( type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPath ) ) {
			return true;
		}

		TypeMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();

		for ( int i = 0; i < propertyPath.length - 1; i++ ) {
			Iterable<EmbeddedTypeMetadata> embeddedTypeMetadata = getEmbeddedTypeMetadata( metadata );
			metadata = getEmbeddedTypeMetadata( embeddedTypeMetadata, propertyPath[i] );
			if ( metadata == null ) {
				return false;
			}
		}

		PropertyMetadata propertyMetadataForProperty = metadata.getPropertyMetadataForProperty( propertyPath[propertyPath.length - 1] );
		boolean b = getEmbeddedTypeMetadata( getEmbeddedTypeMetadata( metadata ), propertyPath[propertyPath.length - 1] ) != null;
		return propertyMetadataForProperty != null || b;
	}

	private TypeMetadata getLeafTypeMetadata(Class<?> type, String... propertyPath) {
		EntityIndexBinding entityIndexBinding = getIndexBinding( searchFactory, type );
		TypeMetadata leafTypeMetadata = entityIndexBinding.getDocumentBuilder().getMetadata();

		for ( int i = 0; i < propertyPath.length; i++ ) {
			Iterable<EmbeddedTypeMetadata> embeddedTypeMetadata = getEmbeddedTypeMetadata( leafTypeMetadata );
			TypeMetadata metadata = getEmbeddedTypeMetadata( embeddedTypeMetadata, propertyPath[i] );
			if ( metadata != null ) {
				leafTypeMetadata = metadata;
			}
		}

		return leafTypeMetadata;
	}

	private EmbeddedTypeMetadata getEmbeddedTypeMetadata(Iterable<EmbeddedTypeMetadata> embeddedTypeMetadata, String name) {
		for ( EmbeddedTypeMetadata metadata : embeddedTypeMetadata ) {
			if ( metadata.getEmbeddedFieldName().equals( name ) ) {
				return metadata;
			}
		}

		return null;
	}

	public boolean isAnalyzed(Class<?> type, List<String> propertyPath) {
		return isAnalyzed( type, propertyPath.toArray( new String[propertyPath.size()] ) );
	}

	public boolean isAnalyzed(Class<?> type, String... propertyPath) {
		EntityIndexBinding entityIndexBinding = getIndexBinding( type );

		if ( isIdentifierProperty( entityIndexBinding, propertyPath ) ) {
			return false;
		}

		TypeMetadata metadata = getLeafTypeMetadata( type, propertyPath );

		Index index = getFieldMetadata( metadata.getPropertyMetadataForProperty( propertyPath[propertyPath.length - 1] ) ).iterator().next().getIndex();
		return EnumSet.of( Field.Index.ANALYZED, Field.Index.ANALYZED_NO_NORMS ).contains( index );
	}

	public boolean isEmbedded(Class<?> type, List<String> propertyPath) {
		return isEmbedded( type, propertyPath.toArray( new String[propertyPath.size()] ) );
	}

	/**
	 * Determines whether the given property path denotes an embedded entity (not a property of such entity).
	 *
	 * @param type the indexed type
	 * @param propertyPath the path of interest
	 * @return {@code true} if the given path denotes an embedded entity of the given indexed type, {@code false}
	 * otherwise.
	 */
	public boolean isEmbedded(Class<?> type, String... propertyPath) {
		if ( propertyPath.length == 0 ) {
			return false;
		}

		EntityIndexBinding entityIndexBinding = getIndexBinding( type );
		TypeMetadata metadata = entityIndexBinding.getDocumentBuilder().getMetadata();

		for ( int i = 0; i < propertyPath.length; i++ ) {
			Iterable<EmbeddedTypeMetadata> embeddedTypeMetadata = getEmbeddedTypeMetadata( metadata );
			metadata = getEmbeddedTypeMetadata( embeddedTypeMetadata, propertyPath[i] );
			if ( metadata == null ) {
				break;
			}
		}

		return metadata != null;
	}

	private boolean isIdentifierProperty(EntityIndexBinding entityIndexBinding, String... propertyPath) {
		return propertyPath.length == 1 && propertyPath[0].equals( entityIndexBinding.getDocumentBuilder().getIdentifierName() );
	}

	private EntityIndexBinding getIndexBinding(Class<?> type) {
		return getIndexBinding( searchFactory, type );
	}

	private EntityIndexBinding getIndexBinding(SearchIntegrator searchFactory, Class<?> type) {
		EntityIndexBinding entityIndexBinding = searchFactory.getIndexBinding( type );

		if ( entityIndexBinding == null ) {
			throw log.getNoIndexedEntityException( type.getCanonicalName() );
		}

		return entityIndexBinding;
	}

	private Iterable<EmbeddedTypeMetadata> getEmbeddedTypeMetadata(TypeMetadata metadata) {
		return HsearchWorkaround.getEmbeddedTypeMetadata( metadata );
	}

	private Iterable<DocumentFieldMetadata> getFieldMetadata(PropertyMetadata metadata) {
		return HsearchWorkaround.getFieldMetadata( metadata );
	}

	// TODO HQLPARSER-61 Remove once we are on HSEARCH 5.3.0.Beta2; Due to signature changes in
	// getEmbeddedTypeMetadata() and getFieldMetadata() we cannot compile against HSEARCH 5.3.0.Beta1 (which is needed
	// for ISPN use) and run against HSEARCH 5.2 at OGM runtime; Therefore the methods are invoked through reflection
	// until the signatures have been reverted in HSEARCH 5.3.0.Beta2
	private static class HsearchWorkaround {

		private static final Method GET_EMBEDDED_TYPE_METADATA;
		private static final Method GET_FIELD_METADATA;

		static {
			try {
				GET_EMBEDDED_TYPE_METADATA = TypeMetadata.class.getMethod( "getEmbeddedTypeMetadata" );
				GET_FIELD_METADATA = PropertyMetadata.class.getMethod( "getFieldMetadata" );
			}
			catch (Exception e) {
				throw new RuntimeException( e );
			}
		}

		@SuppressWarnings("unchecked")
		private static Iterable<EmbeddedTypeMetadata> getEmbeddedTypeMetadata(TypeMetadata metadata) {
			try {
				return (Iterable<EmbeddedTypeMetadata>) GET_EMBEDDED_TYPE_METADATA.invoke( metadata );
			}
			catch (Exception e) {
				throw new RuntimeException( e );
			}
		}

		@SuppressWarnings("unchecked")
		public static Iterable<DocumentFieldMetadata> getFieldMetadata(PropertyMetadata metadata) {
			try {
				return (Iterable<DocumentFieldMetadata>) GET_FIELD_METADATA.invoke( metadata );
			}
			catch (Exception e) {
				throw new RuntimeException( e );
			}
		}
	}
}
