package com.jk.blog.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DateTimeUtilTest {

    @Test
    void test_FormatInstantToIsoString_ShouldReturnIsoFormattedString_WhenValidInstant() {

        Instant instant = Instant.parse("2024-02-05T12:30:45.123Z");

        String result = DateTimeUtil.formatInstantToIsoString(instant);

        assertEquals("2024-02-05T12:30:45.123Z", result);
    }

    @Test
    void test_FormatInstantToIsoString_ShouldReturnNull_WhenInstantIsNull() {

        String result = DateTimeUtil.formatInstantToIsoString(null);
        assertNull(result);
    }
}