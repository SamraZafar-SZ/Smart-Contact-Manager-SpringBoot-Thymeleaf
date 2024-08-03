package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Component
public class sessionUtilityBean {
    
    @Bean
    public void removeMessageFromSession() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            // Handle the case where there are no request attributes available
            System.out.println("No request attributes available.");
            return;
        }

        HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession(false);
        if (session != null) {
            session.removeAttribute("message");
        }
    }
}