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
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@Epic("OrbitaMarket")
@Feature("Сценарии 7.1 (ТЗ)")
class Scenarios71Test extends BaseApiTest {

    private static final String ARCHIVE_ORDER_120 = """
            {
              "product_type": "ARCHIVE",
              "price": 120,
              "payload": {
                "aoi": "scenario-aoi",
                "capture_date": "2026-01-15",
                "sensor_type": "OPTICAL"
              }
            }
            """;

    private static final String ARCHIVE_ORDER_400 = """
            {
              "product_type": "ARCHIVE",
              "price": 400,
              "payload": {
                "aoi": "scenario-aoi-2",
                "capture_date": "2026-01-16",
                "sensor_type": "OPTICAL"
              }
            }
            """;

    private void createAccount(UUID userId) {
        given().header("X-User-Id", userId.toString()).post("/payments/accounts");
    }

    private void topUp(UUID userId, long amount) {
        given()
                .header("X-User-Id", userId.toString())
                .contentType(ContentType.JSON)
                .body("{\"amount\": " + amount + "}")
                .post("/payments/accounts/top-up")
                .then()
                .statusCode(200);
    }

    private String createOrder(UUID userId, String body) {
        return given()
                .header("X-User-Id", userId.toString())
                .contentType(ContentType.JSON)
                .body(body)
                .post("/orders/orders")
                .then()
                .statusCode(200)
                .extract()
                .path("order_id");
    }

    private void waitForOrderStatus(UUID userId, String orderId, String expectedStatus) {
        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        given()
                                .header("X-User-Id", userId.toString())
                                .get("/orders/orders/" + orderId)
                                .then()
                                .statusCode(200)
                                .body("status", equalTo(expectedStatus))
                );
    }

    @Test
    @DisplayName("7.1.1 Happy path: счёт → 1000 → заказ 120 → PAID, баланс 880")
    void happyPath_paidAndBalance880() {
        UUID userId = TestUsers.randomUserId();
        createAccount(userId);
        topUp(userId, 1000);

        String orderId = createOrder(userId, ARCHIVE_ORDER_120);
        waitForOrderStatus(userId, orderId, "PAID");

        given()
                .header("X-User-Id", userId.toString())
                .get("/payments/accounts/balance")
                .then()
                .statusCode(200)
                .body("balance", equalTo(880));
    }

    @Test
    @DisplayName("7.1.2 Недостаточно средств: 50 на счету, заказ 120 → PAYMENT_FAILED")
    void insufficientFunds_paymentFailed() {
        UUID userId = TestUsers.randomUserId();
        createAccount(userId);
        topUp(userId, 50);

        String orderId = createOrder(userId, ARCHIVE_ORDER_120);
        waitForOrderStatus(userId, orderId, "PAYMENT_FAILED");

        given()
                .header("X-User-Id", userId.toString())
                .get("/orders/orders/" + orderId)
                .then()
                .statusCode(200)
                .body("status", equalTo("PAYMENT_FAILED"))
                .body("failure_reason", equalTo("INSUFFICIENT_BALANCE"));

        given()
                .header("X-User-Id", userId.toString())
                .get("/payments/accounts/balance")
                .then()
                .statusCode(200)
                .body("balance", equalTo(50));
    }

    @Test
    @DisplayName("7.1.3 Два заказа по 400 при балансе 1000 → оба PAID, баланс 200")
    void twoOrdersConcurrent_bothPaidBalance200() {
        UUID userId = TestUsers.randomUserId();
        createAccount(userId);
        topUp(userId, 1000);

        String orderId1 = createOrder(userId, ARCHIVE_ORDER_400);
        String orderId2 = createOrder(userId, ARCHIVE_ORDER_400.replace("scenario-aoi-2", "scenario-aoi-3"));

        waitForOrderStatus(userId, orderId1, "PAID");
        waitForOrderStatus(userId, orderId2, "PAID");

        given()
                .header("X-User-Id", userId.toString())
                .get("/payments/accounts/balance")
                .then()
                .statusCode(200)
                .body("balance", equalTo(200));
    }

    @Test
    @DisplayName("7.1.4 Повторный POST /accounts → 409 ACCOUNT_ALREADY_EXISTS")
    void duplicateCreateAccount_conflict409() {
        UUID userId = TestUsers.randomUserId();

        given().header("X-User-Id", userId.toString()).post("/payments/accounts").then().statusCode(200);

        given()
                .header("X-User-Id", userId.toString())
                .post("/payments/accounts")
                .then()
                .statusCode(409)
                .body("error_code", equalTo("ACCOUNT_ALREADY_EXISTS"));
    }
}
