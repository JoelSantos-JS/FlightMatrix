package com.joel.br.FlightMatrix.services;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {


    private JavaMailSender mailSender;



    public boolean enviarEmail(String destinatario, String conteudo, String assunto) {
        try {
            log.info("Enviando email para: {}, assunto: {}", destinatario, assunto);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(conteudo, true); // true indica que é HTML

            mailSender.send(message);
            log.info("Email enviado com sucesso");

            return true;
        } catch (MessagingException e) {
            log.error("Erro ao enviar email: {}", e.getMessage(), e);
            return false;
        }
    }



    public boolean enviarEmailTextoPlano(String destinatario, String assunto, String conteudo) {
        try {
            log.info("Enviando email em texto plano para: {}, assunto: {}", destinatario, assunto);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(conteudo, false); // false indica que é texto plano

            mailSender.send(message);
            log.info("Email em texto plano enviado com sucesso");

            return true;
        } catch (MessagingException e) {
            log.error("Erro ao enviar email em texto plano: {}", e.getMessage(), e);
            return false;
        }
    }
}
