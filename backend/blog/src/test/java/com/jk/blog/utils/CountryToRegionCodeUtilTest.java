package com.jk.blog.utils;

import com.jk.blog.exception.InvalidCountryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CountryToRegionCodeUtilTest {

    @Test
    void test_GetCountryISOCode_ShouldReturnISOCode_WhenValidCountryName() {
        assertEquals("US", CountryToRegionCodeUtil.getCountryISOCode("United States"));
        assertEquals("IN", CountryToRegionCodeUtil.getCountryISOCode("India"));
        assertEquals("GB", CountryToRegionCodeUtil.getCountryISOCode("United Kingdom"));
        assertEquals("CA", CountryToRegionCodeUtil.getCountryISOCode("Canada"));
    }

    @Test
    void test_GetCountryISOCode_ShouldThrowInvalidCountryException_WhenInvalidCountryName() {
        InvalidCountryException exception = assertThrows(
                InvalidCountryException.class,
                () -> CountryToRegionCodeUtil.getCountryISOCode("Atlantis")
        );

        assertEquals("Invalid Country Name: Atlantis", exception.getMessage());
    }

    @Test
    void test_GetCountryISOCode_ShouldThrowInvalidCountryException_WhenEmptyString() {
        InvalidCountryException exception = assertThrows(
                InvalidCountryException.class,
                () -> CountryToRegionCodeUtil.getCountryISOCode("")
        );

        assertEquals("Invalid Country Name: ", exception.getMessage());
    }

    @Test
    void test_GetCountryISOCode_ShouldThrowInvalidCountryException_WhenNullInput() {
        InvalidCountryException exception = assertThrows(
                InvalidCountryException.class,
                () -> CountryToRegionCodeUtil.getCountryISOCode(null)
        );

        assertEquals("Invalid Country Name: null", exception.getMessage());
    }

    @Test
    void test_IsValidCountryName_ShouldReturnTrue_WhenValidCountryName() {
        assertTrue(CountryToRegionCodeUtil.isValidCountryName("United States"));
        assertTrue(CountryToRegionCodeUtil.isValidCountryName("India"));
        assertTrue(CountryToRegionCodeUtil.isValidCountryName("Canada"));
    }

    @Test
    void test_IsValidCountryName_ShouldReturnFalse_WhenInvalidCountryName() {
        assertFalse(CountryToRegionCodeUtil.isValidCountryName("Atlantis"));
        assertFalse(CountryToRegionCodeUtil.isValidCountryName("Narnia"));
    }

    @Test
    void test_IsValidCountryName_ShouldReturnFalse_WhenEmptyString() {
        assertFalse(CountryToRegionCodeUtil.isValidCountryName(""));
    }

    @Test
    void test_IsValidCountryName_ShouldReturnFalse_WhenNullInput() {
        assertFalse(CountryToRegionCodeUtil.isValidCountryName(null));
    }
}
