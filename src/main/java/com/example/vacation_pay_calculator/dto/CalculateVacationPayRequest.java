package com.example.vacation_pay_calculator.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Builder
@Value
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CalculateVacationPayRequest {

    @Positive(message = "The average salary should be a positive number")
    double averageSalary;

    @Min(value = 1, message = "The number of vacation days must be at least 1")
    Integer vacationDays;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate;

    @AssertTrue(message = "The end date must be later than or equal to the start date")
    private boolean isDatesValid() {
        if (startDate == null && endDate == null) {
            return true;
        }

        if (startDate == null || endDate == null) {
            return false;
        }

        return !endDate.isBefore(startDate);
    }

}
