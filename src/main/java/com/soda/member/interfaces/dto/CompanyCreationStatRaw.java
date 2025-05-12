package com.soda.member.interfaces.dto;

import java.time.LocalDate;

public record CompanyCreationStatRaw(Integer year, Integer month, Integer week, Integer day, Long count) {
    public LocalDate getDate() {
        return LocalDate.of(year, month, day);
    }
}
