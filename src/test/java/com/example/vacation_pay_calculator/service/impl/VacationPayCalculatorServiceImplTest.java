package com.example.vacation_pay_calculator.service.impl;

import com.example.vacation_pay_calculator.dto.CalculateVacationPayRequest;
import com.example.vacation_pay_calculator.dto.CalculateVacationPayResponse;
import com.example.vacation_pay_calculator.service.VacationPayCalculatorService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class VacationPayCalculatorServiceImplTest {

    private final VacationPayCalculatorService service = new VacationPayCalculatorServiceImpl();

    @Test
    public void shouldCalculateCorrectlyForTypicalCase() {
        // Given: средняя зарплата 15 000 ₽, отпуск 14 дней
        CalculateVacationPayRequest request = new CalculateVacationPayRequest(15000.0, 14);

        // When: рассчитываем отпускные
        CalculateVacationPayResponse response = service.calculate(request);

        // Then: сумма должна быть 7167.24 (15000 / 29.3 × 14, scale 2, HALF_UP)
        assertThat(response).isNotNull();
        assertThat(response.getVacationPay()).isEqualByComparingTo(BigDecimal.valueOf(7167.24));
    }

    @Test
    public void shouldCalculateCorrectlyForOneVacationDayCase() {
        // Given: средняя зарплата 15 000 ₽, отпуск 1 день
        CalculateVacationPayRequest request = new CalculateVacationPayRequest(15000.0, 1);

        // When: рассчитываем отпускные
        CalculateVacationPayResponse response = service.calculate(request);

        // Then: сумма должна быть 511.95 (15000 / 29.3 × 1, scale 2, HALF_UP)
        assertThat(response).isNotNull();
        assertThat(response.getVacationPay()).isEqualByComparingTo(BigDecimal.valueOf(511.95));
    }

    @Test
    public void shouldThrowExceptionWhenSalaryIsNegative() {
        // Given: средняя зарплата -15 000 ₽, отпуск 14 дней
        CalculateVacationPayRequest request = new CalculateVacationPayRequest(-15000.0, 14);

        // When + Then: ожидаем получить исключение при расчете отпускных
        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The average salary should be non-negative");
    }

    @Test
    public void shouldThrowExceptionWhenVacationDaysIsZero() {
        // Given: средняя зарплата 15 000 ₽, отпуск 0 дней
        CalculateVacationPayRequest request = new CalculateVacationPayRequest(15000.0, 0);

        // When + Then: ожидаем получить исключение при расчете отпускных
        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The number of vacation days must be at least 1");
    }
}
