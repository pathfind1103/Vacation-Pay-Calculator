package com.example.vacation_pay_calculator.controller;

import com.example.vacation_pay_calculator.dto.CalculateVacationPayRequest;
import com.example.vacation_pay_calculator.dto.CalculateVacationPayResponse;
import com.example.vacation_pay_calculator.service.VacationPayCalculatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VacationPayCalculatorController.class)
public class VacationPayCalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VacationPayCalculatorService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn200AndCorrectVacationPayWhenValidQueryParams() throws Exception {
        //Given
        when(service.calculate(any(CalculateVacationPayRequest.class)))
                .thenReturn(new CalculateVacationPayResponse(new BigDecimal("7167.24")));

        //When + Then
        mockMvc.perform(get("/calculate")
                .param("averageSalary", "15000.0")
                .param("vacationDays", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vacationPay").value(7167.24));
    }

    @Test
    void shouldReturn400WhenSalaryIsNegative() throws Exception {
        //When + Then
        mockMvc.perform(get("/calculate")
                        .param("averageSalary", "-15000.0")
                        .param("vacationDays", "14"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenVacationDaysIsZero() throws Exception {
        //When + Then
        mockMvc.perform(get("/calculate")
                        .param("averageSalary", "15000.0")
                        .param("vacationDays", "0"))
                .andExpect(status().isBadRequest());
    }
}
