package com.example.bank.controller;

import com.example.bank.exception.ResourceNotFoundException;
import com.example.bank.model.Notification;
import com.example.bank.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Получить все уведомления пользователя
    @GetMapping
    public List<Notification> getUserNotifications(@RequestParam Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    // Получить только непрочитанные уведомления пользователя
    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(@RequestParam Long userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }

    // Отметить уведомление как прочитанное
    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification", "id" , String.valueOf(id)));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
