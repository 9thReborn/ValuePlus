package com.nitax.valueplusbackend.dto;

import java.util.concurrent.TimeUnit;

public enum ChurnType {
        THIRTY_MINUTES(30, TimeUnit.MINUTES),
        ONE_HOUR(60, TimeUnit.MINUTES),
        THREE_HOURS(180, TimeUnit.MINUTES),
        SIX_HOURS(360, TimeUnit.MINUTES),
        TWELVE_HOURS(720, TimeUnit.MINUTES),
        DAILY(1, TimeUnit.DAYS),
        TWO_DAYS(2, TimeUnit.DAYS),
        THREE_DAYS(3, TimeUnit.DAYS),
        WEEKLY(7, TimeUnit.DAYS);

        private final int duration;
        private final TimeUnit unit;

        ChurnType(int duration, TimeUnit unit) {
            this.duration = duration;
            this.unit = unit;
        }

        public int getDurationInMinutes() {
            return unit == TimeUnit.MINUTES ? duration : duration * 24 * 60;
        }
    }

