package net.lacnic.elections.adminweb.ui.publicresource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class ElectionOfficialResultLetterResourceReference extends ResourceReference {

	private static final long serialVersionUID = -2582517627877256065L;
	private static final ElectionOfficialResultLetterResource RESOURCE = new ElectionOfficialResultLetterResource();

	public ElectionOfficialResultLetterResourceReference() {
		super(ElectionOfficialResultLetterResourceReference.class, "election-official-result-letter");
	}

	@Override
	public IResource getResource() {
		return RESOURCE;
	}
}
