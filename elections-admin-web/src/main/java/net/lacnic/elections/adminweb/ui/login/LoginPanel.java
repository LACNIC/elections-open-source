package net.lacnic.elections.adminweb.ui.login;

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

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.wicket.util.UtilsString;


public class LoginPanel extends Panel {

	private static final long serialVersionUID = 1707397128845200717L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private String username;
	private String password;
	boolean showCaptcha = AppContext.getInstance().getManagerBeanRemote().isProd();


	public LoginPanel(String id) {
		super(id);
		LoginForm form = new LoginForm("form");
		add(form);
	}


	public final class LoginForm extends Form<Void> {
		private static final long serialVersionUID = 8351498360339049712L;

		public LoginForm(final String id) {
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
					if (login(getUsername(), getPassword())) {
						appLogger.info("Successful login for user " + getUsername());
						onLoginSucceeded();
					} else {
						appLogger.info("Failed login for user " + getUsername());
						onLoginFailed();
					}
				}
			};
			add(submitButton);
		}

		public boolean login(String username, String password) {
			return AuthenticatedWebSession.get().signIn(username, UtilsString.wantHashMd5(password));
		}

		protected void onLoginSucceeded() {
			continueToOriginalDestination();
			setResponsePage(getApplication().getHomePage());
		}

		protected void onLoginFailed() {
			error(getString("loginError"));
		}

		private boolean isValidCaptchaResponse(String reCaptchaResponse) {
			return AppContext.getInstance().getManagerBeanRemote().isValidCaptchaResponse(reCaptchaResponse);
		}


		public String getPassword() {
			return password;
		}

		public String getUsername() {
			return username;
		}
	}

}
