package com.example.vacation_pay_calculator.dto;

import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

@Value
public class CalculateVacationPayRequest {

    @Positive(message = "The average salary should be a positive number")
    double averageSalary;

    @Min(value = 1, message = "The number of vacation days must be at least 1")
    int vacationDays;

}
