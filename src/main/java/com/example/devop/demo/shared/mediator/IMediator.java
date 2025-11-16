package com.example.devop.demo.shared.mediator;


public interface IMediator {

    <T extends IRequest<R>, R> R send(T request);

    <T extends INotification> void publish(T event);

    void clearCache();
}
