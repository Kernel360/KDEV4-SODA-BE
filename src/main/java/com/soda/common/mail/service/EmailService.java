package com.soda.common.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("classpath:templates/verification-email.html")
    private Resource verificationEmailTemplate;

    private static final String VERIFICATION_CODE_PLACEHOLDER = "{{code}}";

    public void sendVerificationEmail(String to, String code) throws MessagingException, IOException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("이메일 인증");

            String text = readTemplate(verificationEmailTemplate).replace(VERIFICATION_CODE_PLACEHOLDER, code);
            helper.setText(text, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw e;
        }
    }

    private String readTemplate(Resource resource) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw e;
        }
    }
}
