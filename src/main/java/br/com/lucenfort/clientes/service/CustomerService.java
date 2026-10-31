package br.com.lucenfort.clientes.service;

import br.com.lucenfort.clientes.domain.Address;
import br.com.lucenfort.clientes.domain.Customer;
import br.com.lucenfort.clientes.exception.ApiException;
import br.com.lucenfort.clientes.repository.CustomerRepository;
import br.com.lucenfort.clientes.viacep.AddressResponse;
import br.com.lucenfort.clientes.viacep.ViaCepClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository repository;
    private final ViaCepClient viaCepClient;

    public CustomerService(CustomerRepository repository, ViaCepClient viaCepClient) {
        this.repository = repository; this.viaCepClient = viaCepClient;
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public Customer findById(UUID id) {
        return repository.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", "Cliente nao encontrado"));
    }

    @Transactional
    public Customer create(String name, String email, String cep) {
        if (repository.existsByEmailIgnoreCase(email))
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email ja cadastrado");
        return save(new Customer(name, email, fetchAddress(cep)));
    }

    @Transactional
    public Customer update(UUID id, String name, String email, String cep) {
        Customer customer = findById(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id))
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email ja cadastrado");
        customer.update(name, email, fetchAddress(cep));
        return save(customer);
    }

    @Transactional
    public void delete(UUID id) { repository.delete(findById(id)); }

    private Customer save(Customer customer) {
        try { return repository.save(customer); }
        catch (DataIntegrityViolationException ex) {
            throw new ApiException(HttpStatus.CONFLICT, "CUSTOMER_CONFLICT", "Nao foi possivel salvar o cliente");
        }
    }

    private Address fetchAddress(String cep) {
        try {
            AddressResponse response = viaCepClient.findByCep(cep.replace("-", ""));
            if (response == null || Boolean.TRUE.equals(response.erro()))
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_CEP", "CEP nao encontrado");
            return new Address(response.cep(), response.logradouro(), response.complemento(),
                    response.bairro(), response.localidade(), response.uf());
        } catch (ApiException ex) { throw ex; }
        catch (RuntimeException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "ADDRESS_PROVIDER_UNAVAILABLE",
                    "Servico de CEP indisponivel");
        }
    }
}
