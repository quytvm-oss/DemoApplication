package com.example.devop.demo.shared.mediator;

public interface IQueryHandler <T extends IQuery<R>, R> extends IRequestHandler<T, R> {
}

