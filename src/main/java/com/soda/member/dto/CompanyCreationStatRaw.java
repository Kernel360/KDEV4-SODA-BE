package com.soda.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCreationStatRaw {
    private int year;
    private int month;
    private int weekOfYear;
    private int dayOfMonth;
    private long count;
}
