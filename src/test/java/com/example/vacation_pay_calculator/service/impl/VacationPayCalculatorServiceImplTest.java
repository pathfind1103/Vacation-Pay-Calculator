package com.example.vacation_pay_calculator.service.impl;

import com.example.vacation_pay_calculator.dto.CalculateVacationPayRequest;
import com.example.vacation_pay_calculator.dto.CalculateVacationPayResponse;
import com.example.vacation_pay_calculator.service.HolidayService;
import com.example.vacation_pay_calculator.service.VacationPayCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VacationPayCalculatorServiceImplTest {

    @Mock
    private HolidayService holidayService;

    private VacationPayCalculatorService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new VacationPayCalculatorServiceImpl(holidayService);
    }

    @Test
    public void shouldCalculateCorrectlyForTypicalCase() {
        // Given: средняя зарплата 15 000 ₽, отпуск 14 дней
        CalculateVacationPayRequest request = new CalculateVacationPayRequest(15000.0, 14, null, null);

        // When: рассчитываем отпускные
        CalculateVacationPayResponse response = service.calculate(request);

        // Then: сумма должна быть 7167.24 (15000 / 29.3 × 14, scale 2, HALF_UP)
        assertThat(response).isNotNull();
        assertThat(response.getVacationPay()).isEqualByComparingTo(BigDecimal.valueOf(7167.24));
    }

    @Test
    public void shouldCalculateCorrectlyForOneVacationDayCase() {
        // Given: средняя зарплата 15 000 ₽, отпуск 1 день
        CalculateVacationPayRequest request = new CalculateVacationPayRequest(15000.0, 1, null, null);

        // When: рассчитываем отпускные
        CalculateVacationPayResponse response = service.calculate(request);

        // Then: сумма должна быть 511.95 (15000 / 29.3 × 1, scale 2, HALF_UP)
        assertThat(response).isNotNull();
        assertThat(response.getVacationPay()).isEqualByComparingTo(BigDecimal.valueOf(511.95));
    }

    @Test
    public void shouldThrowExceptionWhenSalaryIsNegative() {
        // Given: средняя зарплата -15 000 ₽, отпуск 14 дней
        CalculateVacationPayRequest request = new CalculateVacationPayRequest(-15000.0, 14, null, null);

        // When + Then: ожидаем получить исключение при расчете отпускных
        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The average salary should be non-negative");
    }

    @Test
    public void shouldThrowExceptionWhenVacationDaysIsZero() {
        // Given: средняя зарплата 15 000 ₽, отпуск 0 дней
        CalculateVacationPayRequest request = new CalculateVacationPayRequest(15000.0, 0, null, null);

        // When + Then: ожидаем получить исключение при расчете отпускных
        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The number of vacation days must be at least 1");
    }

    @Test
    public void shouldCalculateCorrectlyByDatesWhenNoHolidays() {
        // Given: 5 дней без праздников
        LocalDate start = LocalDate.of(2026, 5, 12); // вторник
        LocalDate end   = LocalDate.of(2026, 5, 16); // суббота (5 дней)
        // 12–16 мая 2026: нет праздников

        CalculateVacationPayRequest request = CalculateVacationPayRequest.builder()
                .averageSalary(15000.0)
                .vacationDays(0) // не используется
                .startDate(start)
                .endDate(end)
                .build();

        // Mock: все дни — не праздники
        when(holidayService.isHoliday(any(LocalDate.class))).thenReturn(false);

        // When
        CalculateVacationPayResponse response = service.calculate(request);

        // Then: 5 дней × (15000 / 29.3)
        BigDecimal daily = BigDecimal.valueOf(15000)
                .divide(BigDecimal.valueOf(29.3), 10, RoundingMode.HALF_UP);
        BigDecimal expected = daily.multiply(BigDecimal.valueOf(5))
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(response.getVacationPay())
                .isEqualByComparingTo(expected);
    }

    @Test
    public void shouldCalculateByDatesExcludingHolidayInPeriod() {
        //Given: 23–27 февраля 2026 (5 дней, 23 февраля — праздник)
        LocalDate start = LocalDate.of(2026, 2, 23);
        LocalDate end = LocalDate.of(2026, 2, 27);

        CalculateVacationPayRequest request = CalculateVacationPayRequest.builder()
                .averageSalary(40000.0)
                .startDate(start)
                .endDate(end)
                .build();

        //Mock: только 23 февраля — праздник, остальные дни — нет
        when(holidayService.isHoliday(eq(LocalDate.of(2026, 2, 23)))).thenReturn(true);

        //When
        CalculateVacationPayResponse response = service.calculate(request);

        //Then: 4 оплачиваемых дня и 1 праздник
        BigDecimal daily = BigDecimal.valueOf(40000)
                .divide(BigDecimal.valueOf(29.3), 10, RoundingMode.HALF_UP);
        BigDecimal expected = daily.
                multiply(BigDecimal.valueOf(4))
                .setScale(2, RoundingMode.HALF_UP);

        assertThat(response.getVacationPay()).isEqualByComparingTo(expected);
    }

    @Test
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {
        //Given: некорректный период
        LocalDate start = LocalDate.of(2026, 2, 27);
        LocalDate end = LocalDate.of(2026, 2, 23);

        CalculateVacationPayRequest request = CalculateVacationPayRequest.builder()
                .averageSalary(40000.0)
                .startDate(start)
                .endDate(end)
                .build();

        //When + Then
        assertThatThrownBy(() -> service.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The end date must be later than or equal to the start date");
    }
}
