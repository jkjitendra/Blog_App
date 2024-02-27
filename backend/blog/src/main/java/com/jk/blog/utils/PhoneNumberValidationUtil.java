package com.jk.blog.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;


public class PhoneNumberValidationUtil {

    public static boolean isValidPhoneNumber(String phoneNumberStr, String regionCode) throws IllegalArgumentException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber phoneNumber = phoneUtil.parse(phoneNumberStr, regionCode); // "null" for default region
            if (!phoneUtil.isValidNumber(phoneNumber)) {
                throw new IllegalArgumentException("Invalid Mobile Number or Region Code");
            }
            return true;
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Invalid Mobile Number Format or Region Code");
        }
    }

    public static String getPhoneNumber(String countryCode, String mobile) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String countryCallingCode = String.valueOf(phoneNumberUtil.getCountryCodeForRegion(countryCode));
        return  ('+' + countryCallingCode + mobile); // example +918888881212 => +, countryCode=91, mobile=8888881212
    }
}