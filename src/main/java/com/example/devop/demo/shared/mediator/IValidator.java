package com.example.devop.demo.shared.mediator;

public interface IValidator<T extends IRequest<?>> {
    void validate(T request);
}
