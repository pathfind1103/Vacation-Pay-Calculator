package com.example.vacation_pay_calculator.controller;

import com.example.vacation_pay_calculator.dto.CalculateVacationPayRequest;
import com.example.vacation_pay_calculator.dto.CalculateVacationPayResponse;
import com.example.vacation_pay_calculator.service.VacationPayCalculatorService;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;

@RestController
public class VacationPayCalculatorController {

    private final VacationPayCalculatorService vacationPayCalculatorService;

    public VacationPayCalculatorController(VacationPayCalculatorService vacationPayCalculatorService) {
        this.vacationPayCalculatorService = vacationPayCalculatorService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<CalculateVacationPayResponse> calculate (
            @Valid @ModelAttribute CalculateVacationPayRequest request) {

        CalculateVacationPayResponse response = vacationPayCalculatorService.calculate(request);
        return ResponseEntity.ok(response);
    }
}
