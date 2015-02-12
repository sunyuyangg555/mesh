package com.gentics.cailun.core.rest.model;

import java.util.Collection;
import java.util.HashSet;

import lombok.NoArgsConstructor;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import com.gentics.cailun.core.rest.model.relationship.BasicRelationships;
import com.gentics.cailun.core.rest.model.relationship.Linked;

@NodeEntity
@NoArgsConstructor
public class LocalizedContent extends File {

	private static final long serialVersionUID = 1100206059138098335L;

	@RelatedToVia(type = BasicRelationships.LINKED, direction = Direction.OUTGOING, elementClass = Linked.class)
	private Collection<Linked> links = new HashSet<>();

	@Indexed
	@Fetch
	protected String content;

	@RelatedTo(elementClass = Language.class, direction = Direction.OUTGOING, type = BasicRelationships.HAS_LANGUAGE)
	protected Language language;

	public LocalizedContent(String name) {
		setName(name);
	}

	public void linkTo(LocalizedContent page) {
		// TODO maybe extract information about link start and end to speedup rendering of page with links
		Linked link = new Linked(this, page);
		this.links.add(link);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
