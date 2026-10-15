package com.demo.example.mscustomerdummy.controller;

import com.demo.example.mscustomerdummy.dto.CustomerDto;
import com.demo.example.mscustomerdummy.model.Customer;
import com.demo.example.mscustomerdummy.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

  private static final String CUSTOMER_ID = "customer-123";
  private static final String CUSTOMER_JSON =
      """
      {
        "nombre": "Ana",
        "apellido": "Perez",
        "correo": "ana@example.com",
        "dni": "123"
      }
      """;

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CustomerService customerService;

  @Test
  void getCustomersReturnsList() throws Exception {
    when(customerService.listCustomers()).thenReturn(Flux.just(customer()));

    MvcResult result = mockMvc.perform(get("/api/customers"))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(CUSTOMER_ID))
        .andExpect(jsonPath("$[0].nombre").value("Ana"));

    verify(customerService).listCustomers();
  }

  @Test
  void getCustomerReturnsCustomer() throws Exception {
    when(customerService.getCustomer(CUSTOMER_ID)).thenReturn(Mono.just(customer()));

    MvcResult result = mockMvc.perform(get("/api/customers/{id}", CUSTOMER_ID))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CUSTOMER_ID))
        .andExpect(jsonPath("$.dni").value("123"));

    verify(customerService).getCustomer(CUSTOMER_ID);
  }

  @Test
  void getCustomerReturnsNotFoundWhenServiceDoesNotFindIt() throws Exception {
    when(customerService.getCustomer(CUSTOMER_ID))
        .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer no encontrado")));

    MvcResult result = mockMvc.perform(get("/api/customers/{id}", CUSTOMER_ID))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isNotFound());

    verify(customerService).getCustomer(CUSTOMER_ID);
  }

  @Test
  void postCustomerDelegatesAndReturnsCreatedCustomer() throws Exception {
    when(customerService.createCustomer(new CustomerDto("Ana", "Perez", "ana@example.com", "123")))
        .thenReturn(Mono.just(customer()));

    MvcResult result = mockMvc.perform(post("/api/customers")
            .contentType("application/json")
            .content(CUSTOMER_JSON))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CUSTOMER_ID))
        .andExpect(jsonPath("$.nombre").value("Ana"));

    verify(customerService).createCustomer(new CustomerDto("Ana", "Perez", "ana@example.com", "123"));
  }

  @Test
  void postEmptyCustomerReturnsBadRequestWithoutCallingService() throws Exception {
    mockMvc.perform(post("/api/customers")
            .contentType("application/json")
            .content("{}"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(customerService);
  }

  @Test
  void putCustomerDelegatesAndReturnsUpdatedCustomer() throws Exception {
    when(customerService.updateCustomer(
        CUSTOMER_ID,
        new CustomerDto("Ana", "Perez", "ana@example.com", "123")))
        .thenReturn(Mono.just(customer()));

    MvcResult result = mockMvc.perform(put("/api/customers/{id}", CUSTOMER_ID)
            .contentType("application/json")
            .content(CUSTOMER_JSON))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CUSTOMER_ID))
        .andExpect(jsonPath("$.dni").value("123"));

    verify(customerService).updateCustomer(
        CUSTOMER_ID,
        new CustomerDto("Ana", "Perez", "ana@example.com", "123"));
  }

  @Test
  void putEmptyCustomerReturnsBadRequestWithoutCallingService() throws Exception {
    mockMvc.perform(put("/api/customers/{id}", CUSTOMER_ID)
            .contentType("application/json")
            .content("{}"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(customerService);
  }

  @Test
  void deleteCustomerCompletesSuccessfully() throws Exception {
    when(customerService.deleteCustomer(CUSTOMER_ID)).thenReturn(Mono.empty());

    MvcResult result = mockMvc.perform(delete("/api/customers/{id}", CUSTOMER_ID))
        .andExpect(request().asyncStarted())
        .andReturn();

    mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isOk());

    verify(customerService).deleteCustomer(CUSTOMER_ID);
  }

  private static Customer customer() {
    return Customer.builder()
        .id(CUSTOMER_ID)
        .nombre("Ana")
        .apellido("Perez")
        .correo("ana@example.com")
        .dni("123")
        .build();
  }
}
