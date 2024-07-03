package com.microsoftTeams.bot.converters;

import org.springframework.core.convert.converter.Converter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class StringToZonedDateTimeConverter implements Converter<String, ZonedDateTime> {
    private static final DateTimeFormatter ISO_ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    @SuppressWarnings("NullableProblems")
    public ZonedDateTime convert(String source) {
        return ZonedDateTime.parse(source, ISO_ZONED_DATE_TIME_FORMATTER);
    }
}


