package com.example.vacation_pay_calculator.service;

import java.time.LocalDate;

public interface HolidayService {
    boolean isHoliday(LocalDate date);
}
