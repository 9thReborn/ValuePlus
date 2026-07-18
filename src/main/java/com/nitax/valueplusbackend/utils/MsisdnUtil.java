package com.nitax.valueplusbackend.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MsisdnUtil {
    private static final String NIGERIAN_PHONE_REGEX = "^(?:\\+234|234|0)([7-9][0-1]\\d{8})$";
    private static final Pattern NIGERIAN_PHONE_PATTERN = Pattern.compile(NIGERIAN_PHONE_REGEX);

    private MsisdnUtil() {}

    public static String normalize(String rawMsisdn) {
        if (rawMsisdn == null) {
            return null;
        }

        String cleaned = rawMsisdn.trim().replaceAll("[\\s\\-()]", "");
        if (cleaned.isEmpty()) {
            return cleaned;
        }

        Matcher matcher = NIGERIAN_PHONE_PATTERN.matcher(cleaned);
        if (matcher.matches()) {
            if (cleaned.startsWith("+")) {
                return cleaned.substring(1);
            } else if (cleaned.startsWith("0")) {
                return "234" + cleaned.substring(1);
            }
            return cleaned; // already 234XXXXXXXXXX
        }

        log.warn("MSISDN '{}' did not match known Nigerian mobile format; storing cleaned as-is", rawMsisdn);
        return cleaned.startsWith("+") ? cleaned.substring(1) : cleaned;
    }

    public static boolean isValidNigerianMsisdn(String msisdn) {
        if (msisdn == null) {
            return false;
        }
        return NIGERIAN_PHONE_PATTERN.matcher(msisdn.trim()).matches();
    }
}
