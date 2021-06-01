package net.lacnic.elections.admin.dashboard.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.error.Error404;
import net.lacnic.elections.admin.dashboard.error.Error500;
import net.lacnic.elections.admin.dashboard.error.ErrorVotacionNoComenzada;
import net.lacnic.elections.admin.dashboard.error.ErrorVotacionNoPublica;
import net.lacnic.elections.admin.panel.user.DetalleVoteEleccionSimplePanel;
import net.lacnic.elections.admin.web.bases.DashboardPublicBasePage;
import net.lacnic.elections.admin.wicket.util.ImageResource;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.UserVoter;

public class DashboardVotarEleccionSimple extends DashboardPublicBasePage {

	private static final long serialVersionUID = -867241975964848115L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private UserVoter upd;
	private List<Candidate> elegidos;
	private WebMarkupContainer containerPregunta;
	private WebMarkupContainer containerConfirmar;
	private WebMarkupContainer containerCandidatos;
	private FeedbackPanel feedbackPanel;


	public DashboardVotarEleccionSimple(PageParameters params) {
		super(params);
		if (upd.isVoted()) {
			setResponsePage(DashboardYaVoto.class, params);
		} else {
			setLocale();
			setElegidos(new ArrayList<>());
			add(new DetalleVoteEleccionSimplePanel("detalleVoteEleccionPanel", getEleccion(), upd));

			feedbackPanel = new FeedbackPanel("feedbackPanel");
			feedbackPanel.setOutputMarkupPlaceholderTag(true);
			add(feedbackPanel);

			add(new Label("candidatosAleatorios", getString("candidatos_aleatorios")).setVisible(getEleccion().isRandomOrderCandidates()));
			add(new Label("maximo", String.valueOf(getEleccion().getMaxCandidates())));

			containerCandidatos = new WebMarkupContainer("containerCandidatos");
			containerCandidatos.setOutputMarkupPlaceholderTag(true);
			add(containerCandidatos);

			ListView<Candidate> candidatosDataView = new ListView<Candidate>("candidatosList", AppContext.getInstance().getManagerBeanRemote().getElectionCandidatesOrdered(getEleccion().getElectionId())) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(final ListItem<Candidate> item) {
					final Candidate candidato = item.getModelObject();
					WebMarkupContainer wmc = new WebMarkupContainer("wmc");
					if (!elegidos.contains(candidato)) {
						wmc.add(new AttributeModifier("class", "candidate-box-white"));
					} else {
						wmc.add(new AttributeModifier("class", "candidate-box-green"));
					}
					wmc.setMarkupId("candidatosEleccion" + item.getIndex());
					item.add(wmc);

					AjaxLink<Void> candidatoLink = new AjaxLink<Void>("candidatoLink") {

						private static final long serialVersionUID = -3578083844361956035L;

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (elegidos.contains(candidato)) {
								wmc.add(new AttributeModifier("class", "candidate-box-white"));
								elegidos.remove(candidato);
							} else {
								wmc.add(new AttributeModifier("class", "candidate-box-green"));
								elegidos.add(candidato);
							}
							target.add(item);

						}
					};
					wmc.add(candidatoLink);

					candidatoLink.add(new Label("nombre", candidato.getName()));
					Label bio = new Label("bio", candidato.getBio(getIdioma()));
					bio.setEscapeModelStrings(false);
					candidatoLink.add(bio);
					candidatoLink.add(new NonCachingImage("foto", new ImageResource(candidato.getPictureInfo(), candidato.getPictureExtension())));
					item.setOutputMarkupId(true);

					String linkTexto = candidato.getLink(getIdioma());
					ExternalLink externalLink = new ExternalLink("link", linkTexto);
					externalLink.setVisible(linkTexto != null && !linkTexto.isEmpty());
					wmc.add(externalLink);
				}
			};
			containerCandidatos.add(candidatosDataView);
			initBoton();
		}
	}

	@Override
	public Class validarToken(PageParameters params) {

		String token1 = params.get("token1").toString();
		String token2 = params.get("token2").toString();

		if ((token1 == null || token1.equals("")) && (token2 == null || token2.equals(""))) {
			upd = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(getToken());
		} else {
			if (token1 == null || token1.equals(""))
				upd = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(token2);
			else
				upd = AppContext.getInstance().getVoterBeanRemote().verifyUserVoterAccess(token1);
		}

		if (upd == null) {
			AppContext.getInstance().getVoterBeanRemote().saveFailedAccessIp(getIP());
			return Error404.class;
		} else {

			setEleccion(upd.getElection());

			if (!getEleccion().isStarted()) {
				setResponsePage(ErrorVotacionNoComenzada.class, getPageParameters());
			} else if (!getEleccion().isEnabledToVote()) {
				return ErrorVotacionNoPublica.class;
			}
		}
		return null;
	}

	public void initBoton() {
		containerPregunta = new WebMarkupContainer("containerPregunta");
		containerPregunta.setOutputMarkupPlaceholderTag(true);
		add(containerPregunta);

		AjaxLink<Void> preguntar = new AjaxLink<Void>("preguntar") {
			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (isOkForVote()) {
					containerConfirmar.setVisible(true);
					containerPregunta.setVisible(false);
					containerCandidatos.setEnabled(false);
					target.add(containerConfirmar);
					target.add(containerPregunta);
					target.add(containerCandidatos);
				}
				target.add(feedbackPanel);

			}
		};
		containerPregunta.add(preguntar);

		containerConfirmar = new WebMarkupContainer("containerConfirmar");
		containerConfirmar.setOutputMarkupPlaceholderTag(true);
		containerConfirmar.setVisible(false);
		add(containerConfirmar);

		Link<Void> confirmar = new Link<Void>("confirmar") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				if (isOkForVote()) {
					try {
						AppContext.getInstance().getVoterBeanRemote().vote(getElegidos(), upd, getIP());
						setResponsePage(DashboardVotar.class, UtilsParameters.getToken(upd.getVoteToken()));
					} catch (Exception e) {
						appLogger.error(e);
						setResponsePage(Error500.class);
					}
				}
			}
		};
		containerConfirmar.add(confirmar);

		AjaxLink<Void> cancelar = new AjaxLink<Void>("cancelar") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				containerConfirmar.setVisible(false);
				containerPregunta.setVisible(true);
				containerCandidatos.setEnabled(true);
				target.add(containerConfirmar);
				target.add(containerPregunta);
				target.add(containerCandidatos);
			}
		};
		containerConfirmar.add(cancelar);
	}

	private boolean isOkForVote() {
		try {
			if (AppContext.getInstance().getVoterBeanRemote().userAlreadyVoted(upd.getUserVoterId())) {
				setResponsePage(new DashboardYaVoto(UtilsParameters.getToken(upd.getVoteToken())));
				return false;
			} else {
				if (elegidos.isEmpty()) {
					error(getString("ningunCandidato_v"));
					return false;
				} else {
					if (elegidos.size() > getEleccion().getMaxCandidates()) {
						error(getString("muchosCandidatos_v") + getEleccion().getMaxCandidates());
						return false;
					}
				}
			}
		} catch (Exception e) {
			appLogger.error(e);
			error(e.getMessage());
		}
		return true;

	}

	private void setLocale() {
		if (getIdioma().toLowerCase().contains("en") || getIdioma().toLowerCase().contains("english"))
			SecurityUtils.setLocale("EN");
		else if (getIdioma().toLowerCase().contains("pt") || getIdioma().toLowerCase().contains("portuguese"))
			SecurityUtils.setLocale("PT");
		else
			SecurityUtils.setLocale("ES");

	}

	@Override
	public String getIdioma() {
		if (upd != null)
			return upd.getLanguage();
		else
			return SecurityUtils.getLocale().getDisplayName();
	}

	public List<Candidate> getElegidos() {
		return elegidos;
	}

	public void setElegidos(List<Candidate> elegidos) {
		this.elegidos = elegidos;
	}
}