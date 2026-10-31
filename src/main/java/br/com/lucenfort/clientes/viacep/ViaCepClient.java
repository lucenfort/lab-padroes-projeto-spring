package br.com.lucenfort.clientes.viacep;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "viaCepClient", url = "${viacep.url:https://viacep.com.br/ws}")
public interface ViaCepClient {
    @GetMapping("/{cep}/json")
    AddressResponse findByCep(@PathVariable String cep);
}
