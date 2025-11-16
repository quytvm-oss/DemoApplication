package com.example.devop.demo.application.cqrs.commands.customer;

import com.example.devop.demo.shared.mediator.ICommand;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCustomerCommand implements ICommand<Long> {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
}
