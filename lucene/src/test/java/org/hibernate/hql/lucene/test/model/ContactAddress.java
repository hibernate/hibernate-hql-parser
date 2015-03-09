/*
 * Hibernate OGM, Domain model persistence for NoSQL datastores
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.lucene.test.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * @author Davide D'Alto
 */
@Embeddable
public class ContactAddress {

	private String address;
	private String postCode;
	private List<ContactAddress> alternatives = new ArrayList<ContactAddress>();

	@Field(analyze = Analyze.NO)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Field(analyze = Analyze.NO)
	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	@IndexedEmbedded(depth = 3)
	public List<ContactAddress> getAlternatives() {
		return alternatives;
	}

	public void setAlternatives(List<ContactAddress> alternatives) {
		this.alternatives = alternatives;
	}
}
