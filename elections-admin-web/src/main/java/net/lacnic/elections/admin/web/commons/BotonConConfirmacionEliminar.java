package net.lacnic.elections.admin.web.commons;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class BotonConConfirmacionEliminar extends Panel {

	private static final long serialVersionUID = 9177980088598096272L;

	private AjaxLink<Void> preguntar;
	private WebMarkupContainer container;
	private Link<Void> confirmar;
	private AjaxLink<Void> cancelar;


	public BotonConConfirmacionEliminar(String id, long i) {
		super(id);

		setOutputMarkupPlaceholderTag(true);

		preguntar = new AjaxLink<Void>("preguntar") {

			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				container.setVisible(true);
				preguntar.setVisible(false);
				target.add(BotonConConfirmacionEliminar.this);
			}
		};
		preguntar.setOutputMarkupPlaceholderTag(true);
		add(preguntar);
		preguntar.setMarkupId(id + i);

		confirmar = new Link<Void>("confirmar") {

			private static final long serialVersionUID = -5497166159069241885L;

			@Override
			public void onClick() {
				// dejar asi
				onConfirmar();
			}
		};
		confirmar.setMarkupId("confirmarEliminar" + i);

		cancelar = new AjaxLink<Void>("cancelar") {

			private static final long serialVersionUID = 5860568364324870195L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				container.setVisible(false);
				preguntar.setVisible(true);
				target.add(container);
				target.add(preguntar);
			}
		};
		cancelar.setMarkupId("cancelarEliminar" + i);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		container.add(confirmar);
		container.add(cancelar);

		add(container);
	}

	public abstract void onConfirmar();

	public AjaxLink<Void> getPreguntar() {
		return preguntar;
	}

	public void setPreguntar(AjaxLink<Void> preguntar) {
		this.preguntar = preguntar;
	}

	public Link<Void> getConfirmar() {
		return confirmar;
	}

	public void setConfirmar(Link<Void> confirmar) {
		this.confirmar = confirmar;
	}

	public AjaxLink<Void> getCancelar() {
		return cancelar;
	}

	public void setCancelar(AjaxLink<Void> cancelar) {
		this.cancelar = cancelar;
	}

	public WebMarkupContainer getContainer() {
		return container;
	}

	public void setContainer(WebMarkupContainer container) {
		this.container = container;
	}

}