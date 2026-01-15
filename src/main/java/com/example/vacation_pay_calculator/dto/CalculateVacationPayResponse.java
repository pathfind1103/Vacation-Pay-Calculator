package com.example.vacation_pay_calculator.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class CalculateVacationPayResponse {

    BigDecimal vacationPay;

}
