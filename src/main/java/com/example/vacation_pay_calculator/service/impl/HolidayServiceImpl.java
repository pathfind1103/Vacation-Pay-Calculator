package com.example.vacation_pay_calculator.service.impl;

import com.example.vacation_pay_calculator.service.HolidayService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Список нерабочих праздничных дней в 2026 году
 * (источник: производственный календарь на consultant.ru)
 */

@Service
public final class HolidayServiceImpl implements HolidayService {

    private static final Set<LocalDate> HOLIDAYS = Collections.unmodifiableSet(
            Stream.of(
                    //Новогодние праздники
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 1, 2),
                    LocalDate.of(2026, 1, 3),
                    LocalDate.of(2026, 1, 4),
                    LocalDate.of(2026, 1, 5),
                    LocalDate.of(2026, 1, 6),
                    LocalDate.of(2026, 1, 7),
                    LocalDate.of(2026, 1, 8),
                    LocalDate.of(2026, 1, 9),

                    //День защитника Отечества
                    LocalDate.of(2026, 2, 23),

                    //Международный женский день
                    LocalDate.of(2026, 3, 9),

                    //День Весны и Труда
                    LocalDate.of(2026, 5, 1),

                    //День Победы
                    LocalDate.of(2026, 5, 11),

                    //День России
                    LocalDate.of(2026, 6, 12),

                    //День народного единства
                    LocalDate.of(2026, 11, 4),

                    //Канун Нового года
                    LocalDate.of(2026, 12, 31)
            ).collect(Collectors.toSet())
    );

    @Override
    public boolean isHoliday(LocalDate date) {
        return HOLIDAYS.contains(date);
    }
}
