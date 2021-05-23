package net.lacnic.elections.admin.web.commons;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class BotonRevisionAuditor extends Panel {

	private static final long serialVersionUID = 9177980088598096272L;

	private WebMarkupContainer container;
	private AjaxLink<Void> preguntar;
	private Link<Void> confirmar;
	private AjaxLink<Void> cancelar;

	public BotonRevisionAuditor(String id) {
		super(id);
		setOutputMarkupPlaceholderTag(true);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		add(container);

		preguntar = new AjaxLink<Void>("preguntar") {

			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(true);
				preguntar.setVisible(false);
				target.add(BotonRevisionAuditor.this);
			}
		};
		preguntar.setOutputMarkupPlaceholderTag(true);
		add(preguntar);

		confirmar = new Link<Void>("confirmar") {

			private static final long serialVersionUID = 6428362413490662299L;

			@Override
			public void onClick() {
				onConfirmar();
			}
		};
		confirmar.setOutputMarkupPlaceholderTag(true);
		container.add(confirmar);

		cancelar = new AjaxLink<Void>("cancelar") {

			private static final long serialVersionUID = 5505159324988683470L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(false);
				preguntar.setVisible(true);
				target.add(container);
				target.add(preguntar);
			}
		};
		cancelar.setOutputMarkupPlaceholderTag(true);
		container.add(cancelar);

	}

	public abstract void onConfirmar();

}