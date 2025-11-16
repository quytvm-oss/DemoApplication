package com.example.devop.demo.shared.mediator;

import com.example.devop.demo.shared.exception.HandlerNotFoundException;
import com.example.devop.demo.shared.exception.ValidationException;
import com.example.devop.demo.shared.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class Mediator implements IMediator {
    private final ApplicationContext context;

    // Cache: key = requestClass/eventClass, value = handler(s)
    private final Map<Class<?>, Object> requestHandlerCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Object>> notificationHandlerCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Object>> validatorCache = new ConcurrentHashMap<>();

    public Mediator(ApplicationContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public <T extends IRequest<R>, R> R send(T request) {
        long startTime = System.currentTimeMillis();

        try {
            runValidators(request);
            var handler = (IRequestHandler<T, R>) requestHandlerCache.computeIfAbsent(
                request.getClass(),
                key -> findHandler(IRequestHandler.class, request)
            );
            if (handler == null) {
                throw new HandlerNotFoundException("No handler found for " + request.getClass().getName());
            }
            R result = handler.handle(request);

            log.debug("Request {} handled in {}ms",
                    request.getClass().getSimpleName(),
                    System.currentTimeMillis() - startTime);

            return result;
        } catch (Exception e) {
            log.error("Request {} failed", request.getClass().getSimpleName(), e);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends INotification> void publish(T event) {
        List<Object> cachedHandlers = notificationHandlerCache.computeIfAbsent(
                event.getClass(),
                key -> (List<Object>) (List<?>) findHandlers(INotificationHandler.class, event)
        );

        List<INotificationHandler<T>> handlers = (List<INotificationHandler<T>>) (List<?>) cachedHandlers;
        //handlers.forEach(h -> h.handle(event));
        handlers.forEach(h -> {
            try {
                h.handle(event);
            } catch (Exception e) {
                log.error("Event handler failed for {}", event.getClass().getName(), e);
                // Không throw, để các handler khác chạy tiếp
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends IRequest<?>> void runValidators(T request) {
        List<Object> cachedValidators = validatorCache.computeIfAbsent(
                request.getClass(),
                key -> (List<Object>) (List<?>) findHandlers(IValidator.class, request)
        );

        List<IValidator<T>> validators = (List<IValidator<T>>) (List<?>) cachedValidators;
        //validators.forEach(v -> v.validate(request));
        List<String> errors = new ArrayList<>();

        validators.forEach(v -> {
            try {
                v.validate(request);
            } catch (ValidationException e) {
                errors.add(e.getMessage());
            }
        });

        if (!errors.isEmpty()) {
            throw new ValidationException(errors, ErrorCode.VALIDATION);
        }
    }


    @SuppressWarnings("unchecked")
    private <H, T> H findHandler(Class<H> handlerType, T instance) {
        var handlers = context.getBeansOfType(handlerType);

        for (var handler : handlers.values()) {
            if (isHandlerFor(handler, handlerType, instance)) {
                return (H) handler;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <H, T> List<H> findHandlers(Class<H> handlerType, T instance) {
        var handlers = context.getBeansOfType(handlerType);
        List<H> result = new ArrayList<>();

        for (var handler : handlers.values()) {
            if (isHandlerFor(handler, handlerType, instance)) {
                result.add((H) handler);
            }
        }

        return result;
    }

    private <H, T> boolean isHandlerFor(H handler, Class<?> handlerInterface, T instance) {
        ResolvableType type = ResolvableType.forClass(handler.getClass()).as(handlerInterface);
        Class<?> resolvedType = type.getGeneric(0).resolve();
        return resolvedType != null && resolvedType.isInstance(instance);
    }

    // ---------------- CLEAR CACHE (useful for testing) ----------------
    public void clearCache() {
        requestHandlerCache.clear();
        notificationHandlerCache.clear();
        validatorCache.clear();
    }

}
