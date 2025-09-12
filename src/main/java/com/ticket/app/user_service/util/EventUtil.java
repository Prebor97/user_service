package com.ticket.app.user_service.util;

import com.ticket.app.eventdto.UserEvents;
import com.ticket.app.user_service.model.UserInfo;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventUtil {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventUtil(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNormalEvent(UserInfo user, String subject){
        UserEvents events = new UserEvents();
        events.setSubject(subject);
        events.setUserId(user.getUserId());
        List<String> mails = new ArrayList<>();
        mails.add(user.getEmail());
        events.setEmail(mails);
        events.setName(user.getUserProfile().getLastName()+" "+user.getUserProfile().getFirstName());
        kafkaTemplate.send("user-topics",events);
    }

    public void sendLastLoggedEvent(UserInfo user, String subject){
        UserEvents events = new UserEvents();
        events.setSubject(subject);
        events.setUserId(user.getUserId());
        List<String> mails = new ArrayList<>();
        mails.add(user.getEmail());
        events.setEmail(mails);
        events.setName(user.getUserProfile().getLastName()+" "+user.getUserProfile().getFirstName());
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a");
        String formattedDateTime = now.format(formatter);
        events.setLoginDate(formattedDateTime);
        kafkaTemplate.send("user-topics",events);
    }

    public void sendPasswordResetEvent(UserInfo user, String subject, String token){
        UserEvents events = new UserEvents();
        events.setSubject(subject);
        events.setUserId(user.getUserId());
        List<String> mails = new ArrayList<>();
        mails.add(user.getEmail());
        events.setEmail(mails);
        events.setName(user.getUserProfile().getLastName()+" "+user.getUserProfile().getFirstName());
        events.setToken(token);
        kafkaTemplate.send("user-topics",events);
    }

    public void sendAminCreatedEvent(UserInfo user, String subject){
        UserEvents events = new UserEvents();
        events.setSubject(subject);
        events.setUserId(user.getUserId());
        List<String> mails = new ArrayList<>();
        mails.add(user.getEmail());
        events.setEmail(mails);
        events.setName(user.getUserProfile().getLastName()+" "+user.getUserProfile().getFirstName());
        events.setPassword(user.getPassword());
        kafkaTemplate.send("user-topics",events);
    }

    public void sendAccountDeletionEvent(UserInfo user, List<String> mails, String deletionDate, String subject){
        UserEvents events = new UserEvents();
        events.setSubject(subject);
        events.setUserId(user.getUserId());
        events.setName(user.getUserProfile().getLastName()+" "+user.getUserProfile().getFirstName());
        events.setEmail(mails);
        events.setLoginDate(deletionDate);
        kafkaTemplate.send("user-topics",events);
    }

    public void EventUserEvent(String userId){
        UserEvents events = new UserEvents();
        events.setUserId(userId);
        kafkaTemplate.send("event-user-topics", events);
    }
}
