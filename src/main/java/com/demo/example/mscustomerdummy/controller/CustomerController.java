package com.demo.example.mscustomerdummy.controller;

import com.demo.example.mscustomerdummy.dto.CustomerDto;
import com.demo.example.mscustomerdummy.model.Customer;
import com.demo.example.mscustomerdummy.service.CustomerService;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
@Tag(name = "Customer", description = "Operaciones sobre Customers")
public class CustomerController {

  private final CustomerService customerService;

  @Operation(summary = "Lista todos los Customer", description = "Muestra todos los Customer del sistema")
  @GetMapping("/customers")
  public Flux<Customer> viewCustomers(@RequestHeader Map<String, String> headers) {
    return customerService.listCustomers();
  }

  @Operation(summary = "Busca un Customer por ID", description = "Busca un Customer del sistema por ID")
  @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
  @GetMapping("/customers/{id}")
  public Mono<Customer> viewCustomer(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    return customerService.getCustomer(id)
        .doOnError(throwable -> System.out.println(throwable));
  }

  @Operation(summary = "Elimina un Customer por ID", description = "Elimina un Customer del sistema")
  @DeleteMapping("/customers/{id}")
  public Mono<Void> deleteCustomer(@RequestHeader Map<String, String> headers, @PathVariable String id) {
    return customerService.deleteCustomer(id);
  }

  @Operation(summary = "Crear nuevo Customer", description = "Crea un Customer dentro del sistema")
  @PostMapping("/customers")
  public Mono<Customer> createCustomer(@RequestHeader Map<String, String> headers, @Valid @RequestBody CustomerDto customer) {
    return customerService.createCustomer(customer);
  }

  @Operation(summary = "Actualizar un Customer", description = "Actualiza un Customer existente en el sistema")
  @PutMapping("/customers/{id}")
  public Mono<Customer> updateCustomer(
    @RequestHeader Map<String, String> headers,
    @PathVariable String id,
    @Valid @RequestBody CustomerDto customer) {
      return customerService.updateCustomer(id, customer);
  }

}
