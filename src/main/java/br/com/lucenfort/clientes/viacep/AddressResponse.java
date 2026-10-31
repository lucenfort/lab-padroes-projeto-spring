package br.com.lucenfort.clientes.viacep;

public record AddressResponse(String cep, String logradouro, String complemento, String bairro,
                              String localidade, String uf, Boolean erro) {}
