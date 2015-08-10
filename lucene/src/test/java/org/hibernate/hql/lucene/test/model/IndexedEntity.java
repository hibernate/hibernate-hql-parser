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
package org.hibernate.hql.lucene.test.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2012 Red Hat Inc.
 */
@Indexed
public class IndexedEntity {

	private String id;
	private String name;
	private long position;
	private long code;
	private String text;
	private String title;
	private Author author;
	private List<ContactDetail> contactDetails = new ArrayList<ContactDetail>();
	private List<ContactDetail> alternativeContactDetails = new ArrayList<ContactDetail>();

	@Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Field(analyze = Analyze.NO, indexNullAs = Field.DEFAULT_NULL_TOKEN)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Field
	public long getPosition() {
		return position;
	}

	@Field(analyze = Analyze.NO, indexNullAs = Field.DEFAULT_NULL_TOKEN)
	public long getCode() {
		return code;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	@Field
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Field(name = "title", analyze = Analyze.NO)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@IndexedEmbedded(indexNullAs = Field.DEFAULT_NULL_TOKEN)
	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	@ElementCollection
	@IndexedEmbedded
	public List<ContactDetail> getContactDetails() {
		return contactDetails;
	}

	public void setContactDetails(List<ContactDetail> contactDetails) {
		this.contactDetails = contactDetails;
	}

	@ElementCollection
	@IndexedEmbedded
	public List<ContactDetail> getAlternativeContactDetails() {
		return alternativeContactDetails;
	}

	public void setAlternativeContactDetails(List<ContactDetail> alternativeContactDetails) {
		this.alternativeContactDetails = alternativeContactDetails;
	}
}
