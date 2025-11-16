package com.example.devop.demo.domain.customer;

import com.example.devop.demo.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customers", schema = "customer")
public class Customer extends BaseEntity {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
}
