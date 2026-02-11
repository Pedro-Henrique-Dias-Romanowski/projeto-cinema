package com.romanowski.pedro.service.email;

import com.romanowski.pedro.service.email.exceptions.EmailException;
import com.romanowski.pedro.utils.Constantes;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void test() {
        System.out.println("Mail sender: " + mailSender);
    }

    @Async
    public void enviarEmail(String emailUsuario, String assunto, String conteudo) {
        String emailDoUsuario = emailUsuario.trim();
        String emailOrigem = Constantes.EMAIL_ORIGEM.trim();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        try {
            helper.setFrom(emailOrigem, Constantes.NOME_ENVIADOR);
            helper.setTo(emailDoUsuario);
            helper.setSubject(assunto);
            helper.setText(conteudo, false);
        } catch(MessagingException | UnsupportedEncodingException e){
            throw new EmailException("Erro ao enviar email " + e.getMessage());
        }

        mailSender.send(message);
    }
}
