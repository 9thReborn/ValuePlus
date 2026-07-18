package com.nitax.valueplusbackend.domain;

public enum PublisherStatus {
    REJECTED{
        @Override
        public int statusCode() {
            return -1;
        }
    },
    UNVERIFIED{
        @Override
        public int statusCode() {
            return 1;
        }
    },
    AWAIT_APPROVAL{
        @Override
        public int statusCode() {
            return 2;
        }
    },
    APPROVED{
        @Override
        public int statusCode() {
            return 3;
        }
    },
    ACTIVE{
        @Override
        public int statusCode() {
            return 4;
        }
    },
    INACTIVE{
        @Override
        public int statusCode() {
            return 5;
        }
    },
    SUSPENDED{
        @Override
        public int statusCode() {
            return 6;
        }
    };

    public abstract int statusCode();
}
