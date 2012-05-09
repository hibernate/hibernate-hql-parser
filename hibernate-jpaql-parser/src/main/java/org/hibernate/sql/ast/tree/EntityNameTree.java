package org.hibernate.sql.ast.tree;

import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.hibernate.sql.ast.common.HibernateTree;

public class EntityNameTree extends HibernateTree {

	private List entityNames = null;
	private String outputText = null;

	public EntityNameTree(EntityNameTree entityNameTree, String outputText) {
		super(entityNameTree.getToken());
		this.outputText = outputText;
	}

	public EntityNameTree(int tokenType, Token token, String tokenText,
			List entityNames) {
		super(token);
		Token newToken = createToken(token);
		newToken.setType(tokenType);
		newToken.setText(tokenText);
		this.token = newToken;
		this.entityNames = entityNames;
	}

	private Token createToken(Token fromToken) {
		return new CommonToken(fromToken);
	}

	public int getEntityCount() {
		return entityNames.size();
	}

	public String getEntityName(int index) {
		return (String) entityNames.get(index);
	}

	public String toString() {
		if (outputText == null) {
			outputText = entityNames.get(0).toString();
		}
		return outputText;
	}
}