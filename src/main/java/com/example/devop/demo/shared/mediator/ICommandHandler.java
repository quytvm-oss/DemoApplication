package com.example.devop.demo.shared.mediator;

public interface ICommandHandler<T extends ICommand<R>, R> extends IRequestHandler<T, R> {
}

