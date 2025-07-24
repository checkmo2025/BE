package checkmo.config;

import checkmo.config.properties.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class MailConfig {

    private final MailProperties mailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(mailProperties.getSmtp().getHost());
        mailSender.setPort(mailProperties.getSmtp().getPort());

        mailSender.setUsername(mailProperties.getAuth().getUsername());
        mailSender.setPassword(mailProperties.getAuth().getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", mailProperties.getAuth().isEnable());
        props.put("mail.smtp.starttls.enable", mailProperties.getSmtp().isStarttlsEnable());
        props.put("mail.smtp.timeout", mailProperties.getSmtp().getTimeout());
        props.put("mail.smtp.connectiontimeout", mailProperties.getSmtp().getConnectionTimeout());
        props.put("mail.smtp.writetimeout", mailProperties.getSmtp().getWriteTimeout());

        return mailSender;
    }
}
