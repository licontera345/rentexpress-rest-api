package com.pinguela.rentexpres.service;

public interface MailService {

	public boolean send(String email, String asunto, String mensaje);

}
