package net.lacnic.elections.adminweb.ui.login;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.ElectionsWebAdminSession;

public class LoginPanel extends Panel {

	private static final long serialVersionUID = 1707397128845200717L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private String username;
	private String password;
	private String totp = "";
	private final String dataSiteKey = AppContext.getInstance().getManagerBeanRemote().getDataSiteKey();

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

			TextField<String> totpField = new TextField<>("totp", new PropertyModel<>(LoginPanel.this, "totp"));
			totpField.setType(String.class);
			add(totpField);

			final WebMarkupContainer captcha = new WebMarkupContainer("reCaptcha") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(isCaptchaEnabled());
				}
			};
			captcha.add(new AttributeModifier("data-sitekey", StringUtils.defaultString(dataSiteKey)));
			captcha.setOutputMarkupPlaceholderTag(true);
			add(captcha);

			add(new Button("submit") {
				private static final long serialVersionUID = -4212490116586366321L;
			});
		}

		@Override
		protected void onValidate() {
			super.onValidate();
			if (!isCaptchaEnabled()) {
				return;
			}

			HttpServletRequest httpServletRequest = (HttpServletRequest) getRequest().getContainerRequest();
			String reCaptchaResponse = httpServletRequest.getParameter("g-recaptcha-response");

			if (StringUtils.isBlank(reCaptchaResponse)) {
				sendCaptchaError("mark");
			} else if (!isValidCaptchaResponse(reCaptchaResponse)) {
				sendCaptchaError("fail");
			}
		}

		@Override
		protected void onSubmit() {
			if (login(getUsername(), getPassword())) {
				appLogger.info("Successful login for user {}", getUsername());
				onLoginSucceeded();
			} else {
				appLogger.info("Failed login for user {}", getUsername());
				onLoginFailed();
			}
		}

		public boolean login(String username, String password) {
			ElectionsWebAdminSession session = (ElectionsWebAdminSession) AuthenticatedWebSession.get();
			session.setTotpAux(StringUtils.trimToEmpty(getTotp()));
			return session.signIn(username,password);
		}

		protected void onLoginSucceeded() {
			continueToOriginalDestination();
			setResponsePage(getApplication().getHomePage());
		}

		protected void onLoginFailed() {
			String loginError = ((ElectionsWebAdminSession) getSession()).getLoginError();
			error(StringUtils.defaultIfBlank(loginError, getString("loginError")));
		}

		private boolean isValidCaptchaResponse(String reCaptchaResponse) {
			return AppContext.getInstance().getManagerBeanRemote().isValidCaptchaResponse(reCaptchaResponse);
		}

		private boolean isCaptchaEnabled() {
			return StringUtils.isNotBlank(dataSiteKey) && AppContext.getInstance().getManagerBeanRemote().shouldShowLoginCaptcha(getUsername(), ElectionsWebAdminSession.getIPClient());
		}

		private void sendCaptchaError(String errorKey) {
			error(getString("loginCaptcha." + errorKey));
		}

		public String getPassword() {
			return password;
		}

		public String getUsername() {
			return username;
		}

		public String getTotp() {
			return totp;
		}
	}

}
