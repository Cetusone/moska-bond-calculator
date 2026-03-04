package org.example.moskacalculatorservice.api;

public interface DataService<T> {
    T getBondFullData(String isin);
}
