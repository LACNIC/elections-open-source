package net.lacnic.siselecciones.admin.web.commons;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.siselecciones.admin.app.Contexto;

/**
 * Panel de login.
 * 
 * @see org.apache.wicket.authentication.panel.SignInPanel
 */
public class LoginPanel extends Panel {

	private static final long serialVersionUID = 1707397128845200717L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private String username;
	private String password;

	public LoginPanel(String id) {
		super(id);
		SignInForm form = new SignInForm("form");
		add(form);
	}

	boolean showCaptcha = Contexto.getInstance().getManagerBeanRemote().isProd();

	public final class SignInForm extends Form<Void> {

		private static final long serialVersionUID = 8351498360339049712L;

		public SignInForm(final String id) {
			super(id);
			TextField<String> usernameField = new TextField<>("username", new PropertyModel<>(LoginPanel.this, "username"));
			usernameField.setType(String.class);
			usernameField.setRequired(true);
			add(usernameField);

			PasswordTextField passwordField = new PasswordTextField("password", new PropertyModel<>(LoginPanel.this, "password"));
			passwordField.setType(String.class);
			add(passwordField);

			final WebMarkupContainer captcha = new WebMarkupContainer("reCapthca");
			add(captcha.setVisibilityAllowed(showCaptcha));

			SubmitLink submitButton = new SubmitLink("submit") {

				private static final long serialVersionUID = -4212490116586366321L;

				@Override
				public void onSubmit() {
					if (showCaptcha) {
						HttpServletRequest httpServletRequest = (HttpServletRequest) getRequest().getContainerRequest();
						String reCaptchaResponse = httpServletRequest.getParameter("g-recaptcha-response");
						appLogger.info(reCaptchaResponse);
						boolean isValidReCaptcha = isValidCaptchaResponse(reCaptchaResponse);

						if (!isValidReCaptcha) {
							error(getString("areYouRobot"));
							return;
						}
					}
					if (signIn(getUsername(), getPassword())) {
						appLogger.info("Login exitoso de usuario " + getUsername());
						onSignInSucceeded();
					} else {
						appLogger.info("Login fallido de usuario " + getUsername());
						onSignInFailed();
					}

				}
			};
			add(submitButton);

		}

		public String getPassword() {
			return password;
		}

		public String getUsername() {
			return username;
		}

		protected void onSignInSucceeded() {
			continueToOriginalDestination();
			setResponsePage(getApplication().getHomePage());
		}

		protected void onSignInFailed() {
			error(getString("loginError"));
		}

		public boolean signIn(String username, String password) {
			return AuthenticatedWebSession.get().signIn(username, UtilsString.wantHashMd5(password));
		}

		private boolean isValidCaptchaResponse(String reCaptchaResponse) {
			return Contexto.getInstance().getManagerBeanRemote().isValidCaptchaResponse(reCaptchaResponse);
		}
	}

}
