package br.com.lucenfort.clientes.api;

import br.com.lucenfort.clientes.repository.CustomerRepository;
import br.com.lucenfort.clientes.viacep.AddressResponse;
import br.com.lucenfort.clientes.viacep.ViaCepClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired CustomerRepository customerRepository;
    @MockBean ViaCepClient viaCepClient;

    @AfterEach
    void cleanDatabase() {
        customerRepository.deleteAll();
    }

    @Test
    void createsAndReadsCustomerThroughHttp() throws Exception {
        when(viaCepClient.findByCep("01001000")).thenReturn(validAddress());

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana Silva","email":"ana@example.com","cep":"01001-000"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ana Silva"))
                .andExpect(jsonPath("$.address.uf").value("SP"));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana@example.com"));
    }

    @Test
    void returnsValidationErrorWithoutCallingProvider() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"invalid","cep":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void returnsNotFoundForUnknownCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void returnsBadGatewayWhenProviderFails() throws Exception {
        when(viaCepClient.findByCep(anyString())).thenThrow(new IllegalStateException("provider unavailable"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana Silva","email":"ana@example.com","cep":"01001-000"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("ADDRESS_PROVIDER_UNAVAILABLE"));
    }

    private AddressResponse validAddress() {
        return new AddressResponse("01001-000", "Praca da Se", "", "Se", "Sao Paulo", "SP", false);
    }
}
