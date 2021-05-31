package net.lacnic.elections.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.admin.web.panel.usuariopadron.CamposUsuarioPadronPanel;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardEditarUsuarioPadron extends DashboardAdminBasePage {

	private static final long serialVersionUID = -4584362258132685785L;
	private UserVoter up;
	private String nombre;
	private String email;
	private Integer cantVotos;
	private String orgID;
	private String pais;
	private String idioma;

	public DashboardEditarUsuarioPadron(PageParameters params) {
		super(params);
		long idUsuarioPadron = UtilsParameters.getUserAsLong(params);
		up = AppContext.getInstance().getManagerBeanRemote().obtenerUsuarioPadron(idUsuarioPadron);
		nombre = up.getName();
		email = up.getMail();
		cantVotos = up.getVoteAmount();
		orgID = up.getOrgID();
		pais = up.getCountry();
		idioma = up.getLanguage();

		add(new FeedbackPanel("feedback"));
		Form<Void> formUsuario = new Form<>("formUsuario");
		formUsuario.add(new CamposUsuarioPadronPanel("camposUsuarioPadronPanel", up));
		add(formUsuario);

		formUsuario.add(new Button("editar") {

			private static final long serialVersionUID = -5373277597924225186L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				// TODO revisar comparación de pais y orgID redundante
				if (!(email.equalsIgnoreCase(up.getMail())) || !(nombre.equalsIgnoreCase(up.getName())) || !(cantVotos.equals(up.getVoteAmount())) 
						|| (orgID != null && !(orgID.equalsIgnoreCase(up.getOrgID()))) || (up.getOrgID() != null && !(up.getOrgID().equalsIgnoreCase(orgID))) 
						|| (pais != null && !(pais.equalsIgnoreCase(up.getCountry()))) || (up.getCountry() != null && !(up.getCountry().equalsIgnoreCase(pais))) 
						|| !(idioma.equalsIgnoreCase(up.getLanguage()))) {
					try {
						AppContext.getInstance().getManagerBeanRemote().editarUsuarioPadron(getUp(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
						getSession().info(getString("censusManagementUserEditExito"));
						setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(up.getElection().getElectionId()));
					} catch (CensusValidationException e) {
						error(getString(e.getMessage()));
					}
				}
			}

		});

		formUsuario.add(new Link<Void>("cancelar") {

			private static final long serialVersionUID = 8213865900998891288L;

			@Override
			public void onClick() {

				setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(up.getElection().getElectionId()));
			}

		});
	}

	public UserVoter getUp() {
		return up;
	}

	public void setUp(UserVoter up) {
		this.up = up;
	}

}