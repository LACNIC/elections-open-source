package net.lacnic.elections.admin.dashboard.admin;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.SupraEleccion;
import net.lacnic.elections.domain.TemplateEleccion;
import net.lacnic.elections.domain.TipoDestinatario;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardEnviarEmailPaso2 extends DashboardAdminBasePage {

	private static final long serialVersionUID = -5648589978016911231L;
	private TipoDestinatario tipoDestinatario;

	public DashboardEnviarEmailPaso2(final TemplateEleccion template, PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		final List<TipoDestinatario> tiposDestinatarios = Arrays.asList(TipoDestinatario.values());

		RadioGroup<TipoDestinatario> destinatarios = new RadioGroup<>("destinatarios", new PropertyModel<>(this, "tipoDestinatario"));

		destinatarios.add(new ListView<TipoDestinatario>("tiposDestinatarios", tiposDestinatarios) {

			private static final long serialVersionUID = -1145609116531304514L;

			protected void populateItem(ListItem<TipoDestinatario> it) {

				it.add(new Radio<TipoDestinatario>("radio", it.getModel()).add(new Label("label", it.getModelObject().toString())));
			}
		});

		Form<Void> form = new Form<Void>("form") {

			private static final long serialVersionUID = -7748138031113319982L;

			@Override
			protected void onSubmit() {

				boolean isSupra = false;
				boolean mailPadron = false;
				boolean padronIgual = true;				
				SupraEleccion supraElec;

				if ((getTipoDestinatario() != null) && (getTipoDestinatario().compareTo(TipoDestinatario.VOTANTES) == 0)) {
					mailPadron = true;
					isSupra = AppContext.getInstance().getManagerBeanRemote().isSupraEleccion(template.getEleccion().getIdEleccion());
					// Si es elección conjunta y se va a enviar mail al padron, debeo validar si son iguales 
					if (isSupra) {
						supraElec = AppContext.getInstance().getManagerBeanRemote().obtenerSupraEleccion(template.getEleccion().getIdEleccion());
						padronIgual = AppContext.getInstance().getManagerBeanRemote().isPadronesIguales(supraElec); 
					};				
				};

				if (getTipoDestinatario() == null) {
					error(getString("sendMail2Error"));
				} else if (mailPadron && isSupra && !padronIgual) { 
					error(getString("sendMailSupraPadronDifError"));
				} else {
					template.setTipoDestinatario(tipoDestinatario);
					setResponsePage(new DashboardPreviewDestinatarios(template, params));
				}

			}
		};
		form.add(new BookmarkablePageLink<>("cancelar", DashboardPlantillasVer.class, UtilsParameters.getId(template.getEleccion().getIdEleccion())));

		add(form);
		form.add(destinatarios);

	}

	public TipoDestinatario getTipoDestinatario() {
		return tipoDestinatario;
	}

	public void setTipoDestinatario(TipoDestinatario tipoMensaje) {
		this.tipoDestinatario = tipoMensaje;
	}

}