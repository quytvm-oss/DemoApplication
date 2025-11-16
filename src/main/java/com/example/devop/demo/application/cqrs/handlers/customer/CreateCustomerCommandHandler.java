package com.example.devop.demo.application.cqrs.handlers.customer;

import com.example.devop.demo.application.cqrs.commands.customer.CreateCustomerCommand;
import com.example.devop.demo.domain.customer.Customer;
import com.example.devop.demo.infrastructure.idGen.SnowflakeIdCustomGenerator;
import com.example.devop.demo.infrastructure.persistence.CustomerRepository;
import com.example.devop.demo.shared.mediator.ICommandHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CreateCustomerCommandHandler implements ICommandHandler<CreateCustomerCommand, Long> {

    private final CustomerRepository _customerRepository;
    private  final ApplicationEventPublisher eventPublisher;

    public CreateCustomerCommandHandler(CustomerRepository customerRepository, ApplicationEventPublisher eventPublisher) {
        this._customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Long handle(CreateCustomerCommand request) {
        var customer = new Customer(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPhone(),
            request.getAddress()
        );
        customer.setId(SnowflakeIdCustomGenerator.nextId());
        var createdUser = _customerRepository.save(customer);
        return createdUser.getId();
    }
}
