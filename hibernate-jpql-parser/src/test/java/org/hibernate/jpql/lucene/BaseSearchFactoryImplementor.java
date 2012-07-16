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

import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.hibernate.search.backend.impl.batch.BatchBackend;
import org.hibernate.search.backend.spi.Worker;
import org.hibernate.search.batchindexing.MassIndexerProgressMonitor;
import org.hibernate.search.engine.impl.FilterDef;
import org.hibernate.search.engine.spi.DocumentBuilderContainedEntity;
import org.hibernate.search.engine.spi.EntityIndexBinder;
import org.hibernate.search.engine.spi.SearchFactoryImplementor;
import org.hibernate.search.engine.spi.TimingSource;
import org.hibernate.search.exception.ErrorHandler;
import org.hibernate.search.filter.FilterCachingStrategy;
import org.hibernate.search.indexes.IndexReaderAccessor;
import org.hibernate.search.indexes.impl.IndexManagerHolder;
import org.hibernate.search.query.dsl.QueryContextBuilder;
import org.hibernate.search.query.engine.spi.HSQuery;
import org.hibernate.search.query.engine.spi.TimeoutExceptionFactory;
import org.hibernate.search.spi.InstanceInitializer;
import org.hibernate.search.stat.Statistics;
import org.hibernate.search.stat.spi.StatisticsImplementor;


/**
 * Useful base class to create mocks of SearchFactory
 */
public class BaseSearchFactoryImplementor implements SearchFactoryImplementor {

	@Override
	public EntityIndexBinder getIndexBindingForEntity(Class<?> entityType) {
		return null;
	}

	@Override
	public void addClasses(Class<?>... classes) {
	}

	@Override
	public Worker getWorker() {
		return null;
	}

	@Override
	public void close() {
	}

	@Override
	public HSQuery createHSQuery() {
		return null;
	}

	@Override
	public boolean isStopped() {
		return false;
	}

	@Override
	public ErrorHandler getErrorHandler() {
		return null;
	}

	@Override
	public TimeoutExceptionFactory getDefaultTimeoutExceptionFactory() {
		return null;
	}

	@Override
	public void optimize() {
	}

	@Override
	public void optimize(Class entityType) {
	}

	@Override
	public Analyzer getAnalyzer(String name) {
		return null;
	}

	@Override
	public Analyzer getAnalyzer(Class<?> clazz) {
		return null;
	}

	@Override
	public QueryContextBuilder buildQueryBuilder() {
		return null;
	}

	@Override
	public Statistics getStatistics() {
		return null;
	}

	@Override
	public IndexReaderAccessor getIndexReaderAccessor() {
		return null;
	}

	@Override
	public Map<Class<?>, EntityIndexBinder> getIndexBindingForEntity() {
		return null;
	}

	@Override
	public <T> DocumentBuilderContainedEntity<T> getDocumentBuilderContainedEntity(Class<T> entityType) {
		return null;
	}

	@Override
	public FilterCachingStrategy getFilterCachingStrategy() {
		return null;
	}

	@Override
	public FilterDef getFilterDefinition(String name) {
		return null;
	}

	@Override
	public String getIndexingStrategy() {
		return null;
	}

	@Override
	public int getFilterCacheBitResultsSize() {
		return 0;
	}

	@Override
	public Set<Class<?>> getIndexedTypesPolymorphic(Class<?>[] classes) {
		return null;
	}

	@Override
	public BatchBackend makeBatchBackend(MassIndexerProgressMonitor progressMonitor) {
		return null;
	}

	@Override
	public boolean isJMXEnabled() {
		return false;
	}

	@Override
	public StatisticsImplementor getStatisticsImplementor() {
		return null;
	}

	@Override
	public boolean isDirtyChecksEnabled() {
		return false;
	}

	@Override
	public IndexManagerHolder getAllIndexesManager() {
		return null;
	}

	@Override
	public InstanceInitializer getInstanceInitializer() {
		return null;
	}

	@Override
	public TimingSource getTimingSource() {
		return null;
	}

}
