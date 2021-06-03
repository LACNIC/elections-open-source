package net.lacnic.elections.adminweb.ui.admin.election.census;

import java.io.File;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.ui.components.ButtonResendVoteEmail;
import net.lacnic.elections.adminweb.ui.components.ButtonUpdateToken;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.LinksUtils;

public class CensusListPanel extends Panel {

	private static final long serialVersionUID = -1239455534678268981L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private List<UserVoter> usuariosPadron;
	private File archivo;

	public CensusListPanel(String id, Election eleccion) {
		super(id);
		usuariosPadron = AppContext.getInstance().getManagerBeanRemote().getElectionUserVoters(eleccion.getElectionId());

		add(new Label("cantidad", String.valueOf(usuariosPadron.size())));

		DownloadLink downloadLink = new DownloadLink("exportarPadron", new PropertyModel<>(CensusListPanel.this, "archivo")) {

			private static final long serialVersionUID = 5415706945162526592L;

			@Override
			public void onClick() {
				setArchivo(AppContext.getInstance().getManagerBeanRemote().exportCensus(eleccion.getElectionId()));
				super.onClick();
			}
		};
		add(downloadLink);
		downloadLink.setVisible(eleccion.isElectorsSet());

		final ListView<UserVoter> usuariosPadronDataView = new ListView<UserVoter>("usuarioPadronList", usuariosPadron) {
			private static final long serialVersionUID = 1786359392545666490L;

			@Override
			protected void populateItem(final ListItem<UserVoter> item) {
				try {
					final UserVoter actual = item.getModelObject();

					item.add(new Label("idioma", actual.getLanguage()));
					item.add(new Label("nombre", actual.getName()));
					item.add(new Label("mail", actual.getMail()));
					item.add(new Label("cantidadVotos", String.valueOf(actual.getVoteAmount())));
					item.add(new Label("pais", actual.getCountry()));
					item.add(new Label("orgId", actual.getOrgID()));
					item.add(new Label("voto", (actual.isVoted() ? "SI" : "NO")));
					String calcularLinkVotar = LinksUtils.buildVoteLink(actual.getVoteToken());
					Label textoLinkVotar = new Label("textoLinkVotar", calcularLinkVotar);
					ExternalLink linkvotar = new ExternalLink("linkVotar", calcularLinkVotar);
					linkvotar.add(textoLinkVotar);
					item.add(linkvotar);

					ButtonUpdateToken botonActualizarToken = new ButtonUpdateToken("actualizar") {

						private static final long serialVersionUID = 3609140813722818708L;

						@Override
						public void onConfirm() {

							try {
								AppContext.getInstance().getManagerBeanRemote().updateUserVoterToken(actual.getUserVoterId(), actual.getName(), eleccion.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								getSession().info(getString("censusManagementUserListExitoToken"));
								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(eleccion.getElectionId()));
							} catch (Exception e) {
								appLogger.error(e);
							}

						}
					};
					item.add(botonActualizarToken);

					ButtonResendVoteEmail botonReenviarEmailVotacion = new ButtonResendVoteEmail("enviarLink") {

						private static final long serialVersionUID = -4628772989608517427L;

						@Override
						public void onConfirm() {

							try {
								AppContext.getInstance().getManagerBeanRemote().resendUserVoterElectionMail(actual, eleccion, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								getSession().info(getString("censusManagementUserListExitoLink"));
								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(eleccion.getElectionId()));
							} catch (Exception e) {
								appLogger.error(e);
							}

						}
					};
					item.add(botonReenviarEmailVotacion);

					ButtonDeleteWithConfirmation botonConConfirmacionEliminar = new ButtonDeleteWithConfirmation("eliminar",item.getIndex()) {

						private static final long serialVersionUID = -6583106894827434879L;

						@Override
						public void onConfirm() {

							try {
								AppContext.getInstance().getManagerBeanRemote().removeUserVoter(actual, eleccion.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
								getSession().info(getString("censusManagementUserListExitoDelete"));
								setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(eleccion.getElectionId()));
							} catch (Exception e) {
								appLogger.error(e);
							}
						}
					};
					item.add(botonConConfirmacionEliminar);

					Link<Void> editarUsuarioPadron = new Link<Void>("editarUsuarioPadron") {

						private static final long serialVersionUID = 8268292966477896858L;

						@Override
						public void onClick() {
							setResponsePage(EditUserVoterDashboard.class, UtilsParameters.getUser(actual.getUserVoterId()));
						}

					};
					editarUsuarioPadron.setMarkupId("editarUsu"+item.getIndex());
					item.add(editarUsuarioPadron);

				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(usuariosPadronDataView);
	}

	public File getArchivo() {
		return archivo;
	}

	public void setArchivo(File archivo) {
		this.archivo = archivo;
	}
}