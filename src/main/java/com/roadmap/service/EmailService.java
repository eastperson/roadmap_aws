package com.roadmap.service;

import com.roadmap.dto.email.EmailMessage;

public interface EmailService {

    void sendEmail(EmailMessage emailMessage);
}
