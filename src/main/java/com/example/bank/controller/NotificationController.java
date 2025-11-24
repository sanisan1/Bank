package com.example.bank.controller;

import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.model.Notification;
import com.example.bank.model.NotificationResponse;
import com.example.bank.repository.NotificationRepository;
import com.example.bank.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Получить только непрочитанные уведомления пользователя
    @GetMapping("/unread")
    public List<NotificationResponse> getUnreadNotifications() {
        return notificationService.getUnreadNotification();
    }
    //Получить все увдомления
    @GetMapping("/all")
    public List<NotificationResponse> getAllNotifications() {
        return notificationService.getAlldNotification();
    }
}
