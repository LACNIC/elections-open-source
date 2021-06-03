package net.lacnic.elections.adminweb.dashboard.admin;

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

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.RecipientType;

@AuthorizeInstantiation("elections-only-one")
public class DashboardEnviarEmailPaso2 extends DashboardAdminBasePage {

	private static final long serialVersionUID = -5648589978016911231L;
	private RecipientType tipoDestinatario;

	public DashboardEnviarEmailPaso2(final ElectionEmailTemplate template, PageParameters params) {
		super(params);
		add(new FeedbackPanel("feedback"));

		final List<RecipientType> tiposDestinatarios = Arrays.asList(RecipientType.values());

		RadioGroup<RecipientType> destinatarios = new RadioGroup<>("destinatarios", new PropertyModel<>(this, "tipoDestinatario"));

		destinatarios.add(new ListView<RecipientType>("tiposDestinatarios", tiposDestinatarios) {

			private static final long serialVersionUID = -1145609116531304514L;

			protected void populateItem(ListItem<RecipientType> it) {

				it.add(new Radio<RecipientType>("radio", it.getModel()).add(new Label("label", it.getModelObject().toString())));
			}
		});

		Form<Void> form = new Form<Void>("form") {

			private static final long serialVersionUID = -7748138031113319982L;

			@Override
			protected void onSubmit() {

				boolean isSupra = false;
				boolean mailPadron = false;
				boolean padronIgual = true;				
				JointElection supraElec;

				if ((getTipoDestinatario() != null) && (getTipoDestinatario().compareTo(RecipientType.VOTERS) == 0)) {
					mailPadron = true;
					isSupra = AppContext.getInstance().getManagerBeanRemote().isJointElection(template.getElection().getElectionId());
					// Si es elección conjunta y se va a enviar mail al padron, debeo validar si son iguales 
					if (isSupra) {
						supraElec = AppContext.getInstance().getManagerBeanRemote().getJointElectionForElection(template.getElection().getElectionId());
						padronIgual = AppContext.getInstance().getManagerBeanRemote().electionsCensusEqual(supraElec); 
					};				
				};

				if (getTipoDestinatario() == null) {
					error(getString("sendMail2Error"));
				} else if (mailPadron && isSupra && !padronIgual) { 
					error(getString("sendMailSupraPadronDifError"));
				} else {
					template.setRecipientType(tipoDestinatario);
					setResponsePage(new DashboardPreviewDestinatarios(template, params));
				}

			}
		};
		form.add(new BookmarkablePageLink<>("cancelar", DashboardPlantillasVer.class, UtilsParameters.getId(template.getElection().getElectionId())));

		add(form);
		form.add(destinatarios);

	}

	public RecipientType getTipoDestinatario() {
		return tipoDestinatario;
	}

	public void setTipoDestinatario(RecipientType tipoMensaje) {
		this.tipoDestinatario = tipoMensaje;
	}

}