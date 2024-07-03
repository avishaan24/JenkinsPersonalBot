package com.microsoftTeams.bot.config;

import com.microsoftTeams.bot.converters.StringToZonedDateTimeConverter;
import com.microsoftTeams.bot.converters.ZonedDateTimeToStringConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@SuppressWarnings("unused")
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    private final String database;

    public MongoConfig(Environment environment){
//        this.database = environment.getProperty("spring.data.mongodb.database");
        this.database = "Jenkins";
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected String getDatabaseName() {
        return database;
    }

    @Bean
    @SuppressWarnings("NullableProblems")
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new ZonedDateTimeToStringConverter(),
                new StringToZonedDateTimeConverter()
        ));
    }
}
