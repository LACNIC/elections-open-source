package net.lacnic.siselecciones.admin.web.commons;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class BotonConConfirmacionConSubmit extends Panel {

	private static final long serialVersionUID = 9177980088598096272L;

	private AjaxLink<Void> preguntar;
	private WebMarkupContainer container;
	private SubmitLink confirmar;
	private AjaxLink<Void> cancelar;

	public BotonConConfirmacionConSubmit(String id, String texto) {
		super(id);

		setOutputMarkupPlaceholderTag(true);

		preguntar = new AjaxLink<Void>("preguntar") {

			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				container.setVisible(true);
				preguntar.setVisible(false);
				target.add(BotonConConfirmacionConSubmit.this);
			}
		};
		preguntar.setOutputMarkupPlaceholderTag(true);
		preguntar.add(new Label("texto", texto));
		add(preguntar);

		confirmar = new SubmitLink("confirmar") {

			private static final long serialVersionUID = 1047312492872431555L;

			@Override
			public void onSubmit() {
				onConfirmar();
			}
		};
		confirmar.setOutputMarkupPlaceholderTag(true);

		cancelar = new AjaxLink<Void>("cancelar") {

			private static final long serialVersionUID = -2842318140284006499L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				container.setVisible(false);
				preguntar.setVisible(true);
				target.add(container);
				target.add(preguntar);
			}
		};
		cancelar.setOutputMarkupPlaceholderTag(true);

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

	public SubmitLink getConfirmar() {
		return confirmar;
	}

	public void setConfirmar(SubmitLink confirmar) {
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