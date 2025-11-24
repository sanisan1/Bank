package com.example.bank.service;

import com.example.bank.mapper.NotificationMapper;
import com.example.bank.model.Notification;
import com.example.bank.model.NotificationResponse;
import com.example.bank.model.user.User;
import com.example.bank.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    //Получение непрочитанных уведомок
    @Transactional
    public List<NotificationResponse> getUnreadNotification() {
        User user = userService.getCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalse(user.getUserId());
        readNotification(notifications);
        return notifications.stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
    }
    //Получение прочитанных уведомок
    @Transactional
    public List<NotificationResponse> getAlldNotification() {
        User user = userService.getCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserId(user.getUserId());
        readNotification(notifications);
        return notifications.stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());

    }



    private void readNotification(List<Notification> notifications) {
        notifications.forEach(notif -> notif.setRead(true));
    }



}

