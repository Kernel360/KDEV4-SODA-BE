package com.soda.member.domain.company;

import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;

@Getter
public enum StatisticsUnit {
    DAY("yyyy-MM-dd") {
        @Override
        public LocalDate getStartOfPeriod(LocalDate date) { return date; }
        @Override
        public LocalDate getNextPeriodStart(LocalDate date) { return date.plusDays(1); }
        @Override
        public String formatPeriod(LocalDate date) {
            return date.format(DateTimeFormatter.ofPattern(getDisplayFormat()));
        }
    },

    WEEK("yyyy-MM-Wn") {
        private static final WeekFields TARGET_WEEK_FIELDS = WeekFields.ISO;
        @Override
        public LocalDate getStartOfPeriod(LocalDate date) { return date.with(TARGET_WEEK_FIELDS.dayOfWeek(), 1); }
        @Override
        public LocalDate getNextPeriodStart(LocalDate date) { return getStartOfPeriod(date).plusWeeks(1); }
        @Override
        public String formatPeriod(LocalDate date) {
            YearMonth currentYearMonth = YearMonth.from(date);
            int weekOfMonth = date.get(TARGET_WEEK_FIELDS.weekOfMonth());
            return String.format("%s-W%d",
                    currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    weekOfMonth);
        }
    },

    MONTH("yyyy-MM") {
        @Override
        public LocalDate getStartOfPeriod(LocalDate date) { return date.withDayOfMonth(1); }
        @Override
        public LocalDate getNextPeriodStart(LocalDate date) { return getStartOfPeriod(date).plusMonths(1); }
        @Override
        public String formatPeriod(LocalDate date) {
            return YearMonth.from(date).format(DateTimeFormatter.ofPattern(getDisplayFormat()));
        }
    };

    private final String displayFormat;

    StatisticsUnit(String displayFormat) {
        this.displayFormat = displayFormat;
    }

    public abstract LocalDate getStartOfPeriod(LocalDate date);
    public abstract LocalDate getNextPeriodStart(LocalDate date);
    public abstract String formatPeriod(LocalDate date);

    public static StatisticsUnit fromString(String text) {
        if (text != null) {
            for (StatisticsUnit unit : StatisticsUnit.values()) {
                if (text.equalsIgnoreCase(unit.name())) {
                    return unit;
                }
            }
        }
        return MONTH;
    }
}