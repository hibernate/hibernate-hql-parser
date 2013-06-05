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
package org.hibernate.hql.lucene.test.internal.builder.model;

import java.util.Date;

import javax.persistence.Id;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.Resolution;

/**
 * @author Gunnar Morling
 */
@Indexed
public class IndexedEntity {

	private String id;
	private String name;
	private String description;

	private int i;
	private long l;
	private float f;
	private double d;

	private Date date;

	@Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Field(analyze = Analyze.NO)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Field(analyze = Analyze.YES)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Field(analyze = Analyze.NO)
	@NumericField
	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	@Field(analyze = Analyze.NO)
	@NumericField
	public long getL() {
		return l;
	}

	public void setL(long l) {
		this.l = l;
	}

	@Field(analyze = Analyze.NO)
	@NumericField
	public float getF() {
		return f;
	}

	public void setF(float f) {
		this.f = f;
	}

	@Field(analyze = Analyze.NO)
	@NumericField
	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	@Field(analyze = Analyze.NO)
	@DateBridge(resolution = Resolution.DAY)
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
