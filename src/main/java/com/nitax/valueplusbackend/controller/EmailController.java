package com.nitax.valueplusbackend.controller;

import com.nitax.valueplusbackend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

  private final EmailService emailService;

  @GetMapping("/send")
  public String sendTestEmail(@RequestParam String to) {
    String subject = "This is a test email from Spring Boot.";
    String text = "Sending this email to test connection.";

    emailService.sendSimpleMessage(to, subject, text);

    return "Email sent successfully!";
  }
}
