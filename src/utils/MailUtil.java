package utils;

import java.io.File;
import java.util.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bioLockJ.ConfigReader;


public class MailUtil
{

	protected static final Logger log = LoggerFactory.getLogger(MailUtil.class);
	
	public static void sendEmailNotification(ConfigReader cReader) throws Exception  
	{
		if(!validEmail(cReader))
		{
			
			String user = cReader.getAProperty(ConfigReader.EMAIL_FROM);
			String to = cReader.getAProperty(ConfigReader.EMAIL_TO);
			String password = cReader.getAProperty(ConfigReader.EMAIL_PASSWORD);
			if(password==null) password = "";
			log.warn("Invalid email info - notifcation will not be sent.");
			log.warn("(EmailFrom/EmailTo/PasswordLength) = (" + user + "/" + to + "/" + password.length());
			return;
		}
		
		String user = cReader.getAProperty(ConfigReader.EMAIL_FROM);
		String to = cReader.getAProperty(ConfigReader.EMAIL_TO);
		String logFilePath = cReader.getAProperty(ConfigReader.LOG_FILE);
		String password = cReader.getAProperty(ConfigReader.EMAIL_PASSWORD);
		
		log.info("Preparing to send notification email with attachment: " + logFilePath);
		
	      Properties props = new Properties();
	      props.put("mail.smtp.auth", "true");
	      props.put("mail.smtp.starttls.enable", "true");
	      props.put("mail.smtp.host", "smtp.gmail.com");
	      props.put("mail.smtp.port", "25");

	      // Get the Session object.
	      Session session = Session.getInstance(props,
	         new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	               return new PasswordAuthentication(user, password);
	            }
	         });

	         // Create a default MimeMessage object.
	         Message message = new MimeMessage(session);
	         message.setFrom(new InternetAddress(user));
	         message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
	         message.setSubject("BioLockJ Job Complete");

	         // MESSAGE BODY
	         BodyPart messageBodyPart = new MimeBodyPart();
	         messageBodyPart.setText(getMessage());
	         
	         // ATTACHMENT
	         BodyPart messageBodyPart2 = new MimeBodyPart();
	         DataSource source = new FileDataSource(logFilePath);
	         messageBodyPart2.setDataHandler(new DataHandler(source));
	         messageBodyPart2.setFileName(logFilePath.substring(logFilePath.lastIndexOf(File.separator) + 1));
	         
	         Multipart multipart = new MimeMultipart();
	         multipart.addBodyPart(messageBodyPart);
	         multipart.addBodyPart(messageBodyPart2);

	         // Send message
	         message.setContent(multipart);
	         Transport.send(message);

	         log.info("Sent message successfully....");
	   }
	
	
	protected static String getMessage() throws Exception
	{
		return " BioLockJ job complete.  Review attached log file for details. " + "\n\n Regards, \n BioLockJ Admin";
	}
	
	public static boolean validEmail(ConfigReader cReader)
	{
		
		String emailFrom = cReader.getAProperty(ConfigReader.EMAIL_FROM);
		String emailTo = cReader.getAProperty(ConfigReader.EMAIL_TO);
		String emailPass = cReader.getAProperty(ConfigReader.EMAIL_PASSWORD);
		
		if(emailFrom==null || emailTo==null || emailPass==null || 
				emailFrom.trim().length()<1 || emailTo.trim().length()<1  || emailPass.trim().length()<1 )
		{
			return false;
		}
		return true;
	}
	

}
