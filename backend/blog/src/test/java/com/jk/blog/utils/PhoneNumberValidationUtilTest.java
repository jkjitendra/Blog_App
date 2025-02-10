package com.jk.blog.utils;

import com.jk.blog.exception.InvalidPhoneNumberException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberValidationUtilTest {

    @Test
    void test_IsValidPhoneNumber_ShouldReturnTrue_WhenValidPhoneNumber() {
        assertTrue(PhoneNumberValidationUtil.isValidPhoneNumber("8888881212", "IN"), "Valid Indian phone number should return true");
        assertTrue(PhoneNumberValidationUtil.isValidPhoneNumber("+14155552671", "US"), "Valid US phone number should return true");
    }

    @Test
    void test_IsValidPhoneNumber_ShouldReturnFalse_WhenInvalidPhoneNumber() {
        assertFalse(PhoneNumberValidationUtil.isValidPhoneNumber("0000000000", "IN"), "Invalid phone number should return false");
        assertFalse(PhoneNumberValidationUtil.isValidPhoneNumber("123", "US"), "Too short phone number should return false");
    }

    @Test
    void test_IsValidPhoneNumber_ShouldReturnFalse_WhenInvalidRegion() {
        assertFalse(PhoneNumberValidationUtil.isValidPhoneNumber("8888881212", "XX"), "Invalid region code should return false");
    }

    @Test
    void test_GetPhoneNumber_ShouldReturnFormattedNumber_WhenValidInput() {
        String formattedNumber = PhoneNumberValidationUtil.getPhoneNumber("IN", "8888881212");

        assertNotNull(formattedNumber, "Formatted phone number should not be null");
        assertEquals("+918888881212", formattedNumber, "Formatted number should be in E164 format");
    }

    @Test
    void test_GetPhoneNumber_ShouldThrowException_WhenInvalidNumber() {
        InvalidPhoneNumberException exception = assertThrows(
                InvalidPhoneNumberException.class,
                () -> PhoneNumberValidationUtil.getPhoneNumber("IN", null)
        );

        assertEquals("Invalid Mobile Number Format for region: IN", exception.getMessage(), "Exception message should be correct");
    }
}