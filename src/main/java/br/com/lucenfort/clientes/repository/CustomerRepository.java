package br.com.lucenfort.clientes.repository;

import br.com.lucenfort.clientes.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
}
