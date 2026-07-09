package org.mainshop.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mainshop.config.BaseApiTest;
import org.mainshop.config.TestUsers;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Epic("OrbitaMarket")
@Feature("Payments API (ТЗ 2.3)")
class PaymentsApiTest extends BaseApiTest {

    @Test
    @DisplayName("POST /payments/accounts — создать счёт")
    @Description("Успешное создание счёта для нового user_id через API Gateway")
    void createAccount_success() {
        UUID userId = TestUsers.randomUserId();

        given()
                .header("X-User-Id", userId.toString())
        .when()
                .post("/payments/accounts")
        .then()
                .statusCode(200)
                .body("user_id", equalTo(userId.toString()))
                .body("balance", equalTo(0))
                .body("currency", equalTo("geocredits"));
    }

    @Test
    @DisplayName("POST /payments/accounts/top-up — пополнение")
    @Description("Пополнение счёта на 1000 геокредитов")
    void topUp_success() {
        UUID userId = TestUsers.randomUserId();

        given().header("X-User-Id", userId.toString()).post("/payments/accounts");
        given()
                .header("X-User-Id", userId.toString())
                .contentType(ContentType.JSON)
                .body("{\"amount\": 1000}")
        .when()
                .post("/payments/accounts/top-up")
        .then()
                .statusCode(200)
                .body("balance", equalTo(1000))
                .body("currency", equalTo("geocredits"));
    }

    @Test
    @DisplayName("GET /payments/accounts/balance — баланс")
    @Description("Получение текущего баланса пользователя")
    void getBalance_success() {
        UUID userId = TestUsers.randomUserId();

        given().header("X-User-Id", userId.toString()).post("/payments/accounts");
        given()
                .header("X-User-Id", userId.toString())
                .contentType(ContentType.JSON)
                .body("{\"amount\": 500}")
                .post("/payments/accounts/top-up");

        given()
                .header("X-User-Id", userId.toString())
        .when()
                .get("/payments/accounts/balance")
        .then()
                .statusCode(200)
                .body("user_id", equalTo(userId.toString()))
                .body("balance", equalTo(500))
                .body("currency", equalTo("geocredits"));
    }
}
