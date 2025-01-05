package com.jk.blog.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;


public class PhoneNumberValidationUtil {

    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    /**
     * Validates a given phone number against a specific country region.
     * @param phoneNumberStr The phone number in string format.
     * @param regionCode The country region code (e.g., "US", "IN").
     * @return True if the phone number is valid, otherwise false.
     */
    public static boolean isValidPhoneNumber(String phoneNumberStr, String regionCode) throws IllegalArgumentException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(phoneNumberStr, regionCode); // "null" for default region
            return phoneUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }

    /**
     * Returns a formatted phone number with the correct international dialing code.
     * Example: If input is "8888881212" with "IN", output is "+918888881212".
     * @param countryCode The ISO country code (e.g., "IN", "US").
     * @param mobile The raw mobile number.
     * @return The correctly formatted phone number as a String.
     */
    public static String getPhoneNumber(String countryCode, String mobile) {
        try {
            // example +918888881212 => +, countryCode=91, mobile=8888881212
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(mobile, countryCode);
            return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);  // +918888881212 format
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Invalid Mobile Number Format for region: " + countryCode);
        }
    }
}