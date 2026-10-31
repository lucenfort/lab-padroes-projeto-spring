package br.com.lucenfort.clientes.service;

import br.com.lucenfort.clientes.domain.Customer;
import br.com.lucenfort.clientes.exception.ApiException;
import br.com.lucenfort.clientes.repository.CustomerRepository;
import br.com.lucenfort.clientes.viacep.AddressResponse;
import br.com.lucenfort.clientes.viacep.ViaCepClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock CustomerRepository repository;
    @Mock ViaCepClient viaCepClient;
    @InjectMocks CustomerService service;

    @Test void createsCustomerWithAddressFromCep() {
        when(repository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(viaCepClient.findByCep("01001000")).thenReturn(new AddressResponse("01001-000", "Praca da Se", "", "Se", "Sao Paulo", "SP", false));
        when(repository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Customer result = service.create("Ana", "ana@example.com", "01001-000");
        assertEquals("01001-000", result.getAddress().getCep());
        verify(viaCepClient).findByCep("01001000");
    }

    @Test void rejectsDuplicatedEmail() {
        when(repository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);
        ApiException error = assertThrows(ApiException.class, () -> service.create("Ana", "ana@example.com", "01001000"));
        assertEquals("EMAIL_ALREADY_EXISTS", error.getCode());
        verifyNoInteractions(viaCepClient);
    }

    @Test void rejectsUnknownCep() {
        when(repository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(viaCepClient.findByCep(anyString())).thenReturn(new AddressResponse(null, null, null, null, null, null, true));
        ApiException error = assertThrows(ApiException.class, () -> service.create("Ana", "ana@example.com", "01001000"));
        assertEquals("INVALID_CEP", error.getCode());
    }

    @Test void reportsNotFoundCustomer() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(java.util.Optional.empty());
        assertEquals("CUSTOMER_NOT_FOUND", assertThrows(ApiException.class, () -> service.findById(id)).getCode());
    }
}
