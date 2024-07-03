package com.microsoftTeams.bot.converters;

import org.springframework.core.convert.converter.Converter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeToStringConverter implements Converter<ZonedDateTime, String> {
    private static final DateTimeFormatter ISO_ZONED_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public String convert(ZonedDateTime source) {
        return source.format(ISO_ZONED_DATE_TIME_FORMATTER);
    }
}


