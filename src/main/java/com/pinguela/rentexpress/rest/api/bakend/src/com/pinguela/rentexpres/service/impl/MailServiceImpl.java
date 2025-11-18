package com.pinguela.rentexpres.service.impl;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pinguela.rentexpres.config.ConfigManager;
import com.pinguela.rentexpres.service.MailService;

public class MailServiceImpl implements MailService {

    private static final Logger logger = LogManager.getLogger(MailServiceImpl.class);
    private static final String MAIL_EMAIL = ConfigManager.getStringValue("mail.email");
    private static final String MAIL_PASSWORD = ConfigManager.getStringValue("mail.password");
    private static final String MAIL_SMTP_SERVER_NAME = ConfigManager.getStringValue("mail.smtp.server.name");
    private static final int MAIL_SMTP_SERVER_PORT = Integer.parseInt(ConfigManager.getStringValue("mail.smtp.server.port"));

    @Override
    public boolean send(String destinatario, String asunto, String cuerpo) {
        try {
            Email email = new SimpleEmail();
            email.setHostName(MAIL_SMTP_SERVER_NAME);
            email.setSmtpPort(MAIL_SMTP_SERVER_PORT);
            email.setAuthenticator(new DefaultAuthenticator(MAIL_EMAIL, MAIL_PASSWORD));
            email.setStartTLSEnabled(true);
            email.setFrom(MAIL_EMAIL);
            email.setSubject(asunto);
            email.setMsg(cuerpo);
            email.addTo(destinatario);
            email.send();

            logger.info("Correo enviado a: " + destinatario + " | Asunto: " + asunto);
            return true;

        } catch (EmailException e) {
            logger.error("Error al enviar el correo a " + destinatario + ": " + e.getMessage(), e);
            return false;
        }
    }
}

