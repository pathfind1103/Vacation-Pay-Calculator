package com.example.vacation_pay_calculator.service;

import com.example.vacation_pay_calculator.dto.CalculateVacationPayRequest;
import com.example.vacation_pay_calculator.dto.CalculateVacationPayResponse;

public interface VacationPayCalculatorService {

    CalculateVacationPayResponse calculate(CalculateVacationPayRequest request);

}
