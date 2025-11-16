package com.example.devop.demo.application.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendToAll(String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }

    public void sendToUser(String username, String message) {
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/messages",
                message
        );
    }

    public void sendToGroup(Long groupId, String message) {
        messagingTemplate.convertAndSend(
                "/topic/group/" + groupId,
                message
        );
    }
}
