package ru.job4j.grabber.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {
    private static DateTimeParser parser;

    @BeforeAll
    static void initParser() {
        parser = new HabrCareerDateTimeParser();
    }

    @Test
    void testDateTime() {
        String dateTime = "2024-02-28T14:05:13+03:00";
        LocalDateTime expect = LocalDateTime.of(2024, Month.FEBRUARY, 28, 14, 05, 13);
        assertThat(parser.parse(dateTime)).isEqualTo(expect);
    }
}