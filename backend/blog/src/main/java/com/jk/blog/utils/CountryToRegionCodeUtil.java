package com.jk.blog.utils;

import java.util.*;

public class CountryToRegionCodeUtil {
    public static String getCountryISOCode(String countryName) {

        if (isValidCountryName(countryName)) {
            Optional<String> countryCode = Arrays.stream(Locale.getISOCountries())
                    .map(isoCode -> new Locale.Builder().setRegion(isoCode).build())
                    .filter(locale -> locale.getDisplayCountry(Locale.ENGLISH).equalsIgnoreCase(countryName))
                    .map(Locale::getCountry)
                    .findFirst();

            if (countryCode.isPresent()) {
                return countryCode.get();
            }
        }
        throw new IllegalArgumentException("Invalid Country Name or Country Name Not Found.");

    }

    public static boolean isValidCountryName(String countryName) {
        return Arrays.stream(Locale.getAvailableLocales())
                .map(locale -> locale.getDisplayCountry(Locale.ENGLISH))
                .anyMatch(displayCountry -> displayCountry.equalsIgnoreCase(countryName));
    }
}
