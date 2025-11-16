package com.example.devop.demo.shared.mediator;

public interface INotificationHandler <T extends INotification>{
    void handle(T event);
}
