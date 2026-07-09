package org.mainshop.config;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseApiTest {

    protected static final String BASE_URL = System.getProperty(
            "orbita.base.url",
            "http://localhost:8080"
    );

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.filters(
                new AllureRestAssured(),
                new RequestLoggingFilter(),
                new ResponseLoggingFilter()
        );
    }
}
