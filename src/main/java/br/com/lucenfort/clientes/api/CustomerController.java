package br.com.lucenfort.clientes.api;

import br.com.lucenfort.clientes.domain.Customer;
import br.com.lucenfort.clientes.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@Validated
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }
    @GetMapping public List<Customer> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public Customer findById(@PathVariable UUID id) { return service.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@Valid @RequestBody CustomerRequest request) {
        return service.create(request.name(), request.email(), request.cep());
    }
    @PutMapping("/{id}")
    public Customer update(@PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        return service.update(id, request.name(), request.email(), request.cep());
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }

    public record CustomerRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP deve conter 8 digitos") String cep) {}
}
