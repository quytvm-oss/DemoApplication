package com.example.devop.demo.shared.mediator;

public interface IVCommandHandler<T extends IVCommand> extends IRequestHandler<T, Void> {
    void execute(T command);

    @Override
    default Void handle(T command) {
        execute(command);
        return null;
    }
}
