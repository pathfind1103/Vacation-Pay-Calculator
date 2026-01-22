package com.example.vacation_pay_calculator.service.impl;

import com.example.vacation_pay_calculator.dto.CalculateVacationPayRequest;
import com.example.vacation_pay_calculator.dto.CalculateVacationPayResponse;
import com.example.vacation_pay_calculator.service.HolidayService;
import com.example.vacation_pay_calculator.service.VacationPayCalculatorService;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class VacationPayCalculatorServiceImpl implements VacationPayCalculatorService {
    private static final BigDecimal AVERAGE_DAYS_PER_MONTH = BigDecimal.valueOf(29.3);
    private static final int MONTHS_IN_YEAR = 12;

    private final HolidayService holidayService;

    public VacationPayCalculatorServiceImpl(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @Override
    public CalculateVacationPayResponse calculate(CalculateVacationPayRequest request) {
        if (request.getAverageSalary() <= 0) {
            throw new IllegalArgumentException("The average salary should be non-negative");
        }

        BigDecimal salary = BigDecimal.valueOf(request.getAverageSalary());
        BigDecimal dailyRate = salary.divide(BigDecimal.valueOf(29.3), 10, RoundingMode.HALF_UP);

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();

        long paidDays;

        if (start != null && end != null) {
            if (end.isBefore(start)) {
                throw new IllegalArgumentException("The end date must be later than or equal to the start date");
            }

            paidDays = calculatePaidDays(start, end);
        } else {
            int vacationDays = request.getVacationDays();

            if (vacationDays < 1) {
                throw new IllegalArgumentException("The number of vacation days must be at least 1");
            }

            paidDays = vacationDays;
        }

        BigDecimal vacationPay = dailyRate
                .multiply(BigDecimal.valueOf(paidDays))
                .setScale(2, RoundingMode.HALF_UP);

        return new CalculateVacationPayResponse(vacationPay);
    }

    private long calculatePaidDays(LocalDate start, LocalDate end) {
        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;

        long holidaysCount = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (holidayService.isHoliday(current)) {
                holidaysCount++;
            }

            current = current.plusDays(1);
        }

        return totalDays - holidaysCount;
    }
}
