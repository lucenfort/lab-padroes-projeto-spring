package br.com.lucenfort.clientes.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(name = "uk_customer_email", columnNames = "email"))
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotBlank @Size(max = 120)
    private String name;
    @NotBlank @Email @Size(max = 180)
    private String email;
    @Embedded
    private Address address;

    protected Customer() {}
    public Customer(String name, String email, Address address) {
        this.name = name; this.email = email; this.address = address;
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Address getAddress() { return address; }
    public void update(String name, String email, Address address) {
        this.name = name; this.email = email; this.address = address;
    }
}
