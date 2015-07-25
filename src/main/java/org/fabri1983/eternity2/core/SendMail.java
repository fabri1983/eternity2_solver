/**
 * Copyright (c) 2015 Fabricio Lettieri fabri1983@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.fabri1983.eternity2.core;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail implements Runnable {

	private String body,subject;
	private boolean enviado;
	private String recipient= "fabrilett1983@yahoo.com"; //a quien le mando el mail
	
	/**
	 * Constructor, che!!
	 */
	public SendMail (){
		enviado= false;
	}
	
	public void setDatos (String body, String subj){
		this.body= body;
		this.subject= subj;
	}
	
	public void send (){
		try {
			String SMTP_HOST_NAME= "smtp.gmail.com";
			int SMTP_HOST_PORT= 465;
			String SMTP_AUTH_USER= "fabri1983@gmail.com";
			String SMTP_AUTH_PWD= "1badongo9";

	        Properties props = new Properties();

	        props.put("mail.transport.protocol", "smtps");
	        props.put("mail.smtps.host", SMTP_HOST_NAME);
	        props.put("mail.smtps.auth", "true");
	        props.put("mail.smtps.quitwait", "false"); // para deshacerse de una excepcion rara del SSL

	        Session mailSession = Session.getDefaultInstance(props);
	        mailSession.setDebug(false); // si es true imprime informacion de las cosas que suceden

	        MimeMessage message = new MimeMessage(mailSession);
	        message.setSubject(subject);
	        message.setText(this.body);
	        message.addRecipient(Message.RecipientType.TO,new InternetAddress(recipient));
	        message.setFrom(new InternetAddress("fabri1983@gmail.com","E2Solver@e2.com"));
	        // Adding multiple destinators
	        //Address adr[]= {new InternetAddress("fabri1983@gmail.com","GeorgeBush@ThePentagon.gov")};
	        //message.addFrom(adr);
	        
	        Transport transport = mailSession.getTransport();
	        transport.connect(SMTP_HOST_NAME, SMTP_HOST_PORT, SMTP_AUTH_USER, SMTP_AUTH_PWD);
	        System.out.print("Enviando mensaje... ");
	        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
	        System.out.println("Enviado!");
	        transport.close();
	        enviado= true;
		} catch(Exception e) {
			System.out.println("ERROR: Fallo al enviar email.\nCausa: " + e);			
		}
	}

	public boolean enviado () {
		return enviado;
	}

	public void run () {
		send();
	}
}
