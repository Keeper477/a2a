package com.example.a2aaggregator.a2a;

public class UnsupportedA2aVersionException extends RuntimeException {
    public UnsupportedA2aVersionException(String version) { super(version); }
}
