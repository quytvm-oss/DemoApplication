package com.example.devop.demo.shared.mediator;


public interface IRequestHandler<T extends IRequest<R>, R> {
    R handle(T request);
}
