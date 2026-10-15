package com.demo.example.mscustomerdummy.service.impl;

import com.demo.example.mscustomerdummy.dto.CustomerDto;
import com.demo.example.mscustomerdummy.model.Customer;
import com.demo.example.mscustomerdummy.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

  private static final String CUSTOMER_ID = "customer-123";

  @Mock
  private CustomerRepository customerRepository;

  @InjectMocks
  private CustomerServiceImpl customerService;

  @Test
  void listCustomersEmitsAllCustomers() {
    Customer first = customer(CUSTOMER_ID, "Ana", "Perez", "ana@example.com", "123");
    Customer second = customer("customer-456", "Luis", "Diaz", "luis@example.com", "456");
    when(customerRepository.findAll()).thenReturn(Flux.just(first, second));

    StepVerifier.create(customerService.listCustomers())
        .expectNext(first, second)
        .verifyComplete();

    verify(customerRepository).findAll();
    verifyNoMoreInteractions(customerRepository);
  }

  @Test
  void getCustomerEmitsCustomerWhenFound() {
    Customer expected = customer(CUSTOMER_ID, "Ana", "Perez", "ana@example.com", "123");
    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Mono.just(expected));

    StepVerifier.create(customerService.getCustomer(CUSTOMER_ID))
        .expectNext(expected)
        .verifyComplete();

    verify(customerRepository).findById(CUSTOMER_ID);
    verifyNoMoreInteractions(customerRepository);
  }

  @Test
  void getCustomerReturnsNotFoundWhenMissing() {
    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Mono.empty());

    StepVerifier.create(customerService.getCustomer(CUSTOMER_ID))
        .expectErrorSatisfies(error -> {
          ResponseStatusException exception = assertInstanceOf(ResponseStatusException.class, error);
          assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        })
        .verify();

    verify(customerRepository).findById(CUSTOMER_ID);
    verifyNoMoreInteractions(customerRepository);
  }

  @Test
  void createCustomerMapsDtoAndEmitsSavedCustomer() {
    CustomerDto request = new CustomerDto("Ana", "Perez", "ana@example.com", "123");
    when(customerRepository.save(any(Customer.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

    StepVerifier.create(customerService.createCustomer(request))
        .assertNext(saved -> {
          assertEquals("Ana", saved.getNombre());
          assertEquals("Perez", saved.getApellido());
          assertEquals("ana@example.com", saved.getCorreo());
          assertEquals("123", saved.getDni());
        })
        .verifyComplete();

    ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
    verify(customerRepository).save(customerCaptor.capture());
    assertEquals("Ana", customerCaptor.getValue().getNombre());
    assertEquals("Perez", customerCaptor.getValue().getApellido());
    assertEquals("ana@example.com", customerCaptor.getValue().getCorreo());
    assertEquals("123", customerCaptor.getValue().getDni());
    verifyNoMoreInteractions(customerRepository);
  }

  @Test
  void updateCustomerChangesFieldsAndSavesWhenFound() {
    Customer existing = customer(CUSTOMER_ID, "Old", "Name", "old@example.com", "000");
    CustomerDto request = new CustomerDto("Ana", "Perez", "ana@example.com", "123");
    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Mono.just(existing));
    when(customerRepository.save(existing)).thenReturn(Mono.just(existing));

    StepVerifier.create(customerService.updateCustomer(CUSTOMER_ID, request))
        .assertNext(updated -> {
          assertEquals(CUSTOMER_ID, updated.getId());
          assertEquals("Ana", updated.getNombre());
          assertEquals("Perez", updated.getApellido());
          assertEquals("ana@example.com", updated.getCorreo());
          assertEquals("123", updated.getDni());
        })
        .verifyComplete();

    verify(customerRepository).findById(CUSTOMER_ID);
    verify(customerRepository).save(existing);
    verifyNoMoreInteractions(customerRepository);
  }

  @Test
  void updateCustomerReturnsNotFoundAndDoesNotSaveWhenMissing() {
    CustomerDto request = new CustomerDto("Ana", "Perez", "ana@example.com", "123");
    when(customerRepository.findById(CUSTOMER_ID)).thenReturn(Mono.empty());

    StepVerifier.create(customerService.updateCustomer(CUSTOMER_ID, request))
        .expectErrorSatisfies(error -> {
          ResponseStatusException exception = assertInstanceOf(ResponseStatusException.class, error);
          assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        })
        .verify();

    verify(customerRepository).findById(CUSTOMER_ID);
    verifyNoMoreInteractions(customerRepository);
  }

  @Test
  void deleteCustomerCompletesAndDelegatesToRepository() {
    when(customerRepository.deleteById(CUSTOMER_ID)).thenReturn(Mono.empty());

    StepVerifier.create(customerService.deleteCustomer(CUSTOMER_ID))
        .verifyComplete();

    verify(customerRepository).deleteById(CUSTOMER_ID);
    verifyNoMoreInteractions(customerRepository);
  }

  private static Customer customer(
      String id,
      String nombre,
      String apellido,
      String correo,
      String dni) {
    return Customer.builder()
        .id(id)
        .nombre(nombre)
        .apellido(apellido)
        .correo(correo)
        .dni(dni)
        .build();
  }
}
