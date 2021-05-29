package net.lacnic.elections.ejb.commons.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.Session;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.jboss.ejb3.annotation.TransactionTimeout;

import net.lacnic.elections.domain.Email;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.EJBFactory;
import net.lacnic.elections.utils.MailHelper;

@Stateless
public class ProcesosAutomaticos {

	public static boolean corriendo = false;
	public static int intentos = 0;
	
	private static final Logger appLogger = LogManager.getLogger("ejbAppLogger");

	@TransactionTimeout(35000)
	@Schedules({ @Schedule(second = "0", minute = "*/10", hour = "*", persistent = false) })
	public void envioEmail() {
		try {
			if (!corriendo) {
				corriendo = true;
				appLogger.info("Execute Mail Envios Elecciones");
				List<Email> emails = EJBFactory.getInstance().getEnvioMailsEJB().obtenerEmailsParaEnviar();
				EJBFactory.getInstance().getEnvioMailsEJB().marcarEmailsComoEnviados();
				List<Email> emailsProblematicos = new ArrayList<>();
				String host = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.EMAIL_HOST);
				String usuario = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.EMAIL_USER);
				String clave = EJBFactory.getInstance().getParametrosEleccionesEJB().obtenerParametro(Constants.EMAIL_PASSWORD);
				MailHelper.setSmtpHost(host);
				MailHelper.setUser(usuario);
				MailHelper.setPass(clave);
				Session session = MailHelper.initSession();

				for (int i = 0; i < emails.size(); i++) {
					Email email = emails.get(i);
					try {
						appLogger.info("ENVIANDO EMAIL " + email.getSubject() + " a " + email.getRecipients());
						if (!MailHelper.sendMail(session, email.getEmailFrom(), email.getRecipients(), email.getCc(), email.getBcc(), email.getSubject(), email.getBody())) {
							emailConProblema(emailsProblematicos, email);
						}
						if (i % 500 == 0)
							Thread.sleep(5000);

					} catch (MessagingException e) {
						emailConProblema(emailsProblematicos, email);
						appLogger.error(e);
					} catch (Exception e) {
						emailConProblema(emailsProblematicos, email);
						appLogger.error(e);
					}
				}
				EJBFactory.getInstance().getEnvioMailsEJB().reagendar(emailsProblematicos);
				appLogger.info("FIN Execute Mail Envios Elecciones");
				corriendo = false;
			} else {
				if (intentos < 8) {
					intentos = intentos + 1;
					appLogger.info("WARM Esta corriendo un Job en este momento y se ha postegado este envío por " + intentos + " vez.");
				} else {
					appLogger.info("WARM Ultima vez que se posterga el job, es el " + intentos + " intentos");
					intentos = 0;
					corriendo = false;
				}
			}
		} catch (Exception e1) {
			appLogger.error(e1);
		}
	}

	private void emailConProblema(List<Email> emailsProblematicos, Email email) {
		emailsProblematicos.add(email);
		appLogger.info("ERROR enviando email a " + email.getRecipients());
	}

	@Schedules({ @Schedule(second = "0", minute = "*/10", hour = "*", persistent = false) })
	public void correrActualizarHC() {
		EJBFactory.getInstance().getMonitorEleccionesEJB().actualizarHCDatosWS();
	}

	@TransactionTimeout(35000)
	@Schedules({ @Schedule(second = "0", minute = "15", hour = "4", persistent = false) })
	public void moverEmailsaHistoricos() {
		EJBFactory.getInstance().getEnvioMailsEJB().moverEmailsaHistoricos();
	}

	@TransactionTimeout(35000)
	@Schedules({ @Schedule(second = "0", minute = "15", hour = "5", persistent = false) })
	public void purgarTablas() {
		EJBFactory.getInstance().getEnvioMailsEJB().purgarTablas();
	}

	public static int getIntentos() {
		return intentos;
	}

	public static void setIntentos(int intentos) {
		ProcesosAutomaticos.intentos = intentos;
	}

}