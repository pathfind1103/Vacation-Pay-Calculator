package com.example.vacation_pay_calculator.service.impl;

import com.example.vacation_pay_calculator.dto.CalculateVacationPayRequest;
import com.example.vacation_pay_calculator.dto.CalculateVacationPayResponse;
import com.example.vacation_pay_calculator.service.VacationPayCalculatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class VacationPayCalculatorServiceImpl implements VacationPayCalculatorService {
    private static final BigDecimal AVERAGE_DAYS_PER_MONTH = BigDecimal.valueOf(29.3);
    private static final int MONTHS_IN_YEAR = 12;

    @Override
    public CalculateVacationPayResponse calculate(CalculateVacationPayRequest request) {
        if (request.getAverageSalary() <= 0) {
            throw new IllegalArgumentException("The average salary should be non-negative");
        }
        if (request.getVacationDays() < 1) {
            throw new IllegalArgumentException("The number of vacation days must be at least 1");
        }

        BigDecimal salary = BigDecimal.valueOf(request.getAverageSalary());
        BigDecimal days = BigDecimal.valueOf(request.getVacationDays());

        BigDecimal vacationPay = salary
                .divide(AVERAGE_DAYS_PER_MONTH, 10, RoundingMode.HALF_UP)
                .multiply(days)
                .setScale(2, RoundingMode.HALF_UP);

        return new CalculateVacationPayResponse(vacationPay);
    }
}
