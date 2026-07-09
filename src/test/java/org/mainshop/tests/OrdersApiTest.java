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
import static org.hamcrest.Matchers.*;

@Epic("OrbitaMarket")
@Feature("Orders API (ТЗ 3.5)")
class OrdersApiTest extends BaseApiTest {

    private static final String ARCHIVE_ORDER_BODY = """
            {
              "product_type": "ARCHIVE",
              "price": 120,
              "payload": {
                "aoi": "test-aoi",
                "capture_date": "2026-01-01",
                "sensor_type": "OPTICAL"
              }
            }
            """;

    private void prepareUserWithBalance(UUID userId, long amount) {
        given().header("X-User-Id", userId.toString()).post("/payments/accounts");
        given()
                .header("X-User-Id", userId.toString())
                .contentType(ContentType.JSON)
                .body("{\"amount\": " + amount + "}")
                .post("/payments/accounts/top-up");
    }

    @Test
    @DisplayName("POST /orders/orders — создать заказ")
    @Description("Создание ARCHIVE-заказа, статус PAYMENT_PENDING")
    void createOrder_success() {
        UUID userId = TestUsers.randomUserId();
        prepareUserWithBalance(userId, 1000);

        given()
                .header("X-User-Id", userId.toString())
                .contentType(ContentType.JSON)
                .body(ARCHIVE_ORDER_BODY)
        .when()
                .post("/orders/orders")
        .then()
                .statusCode(200)
                .body("order_id", notNullValue())
                .body("status", equalTo("PAYMENT_PENDING"))
                .body("product_type", equalTo("ARCHIVE"))
                .body("price", equalTo(120))
                .body("created_at", notNullValue());
    }

    @Test
    @DisplayName("GET /orders/orders — список заказов")
    @Description("Список заказов текущего пользователя")
    void listOrders_success() {
        UUID userId = TestUsers.randomUserId();
        prepareUserWithBalance(userId, 1000);

        given()
                .header("X-User-Id", userId.toString())
                .contentType(ContentType.JSON)
                .body(ARCHIVE_ORDER_BODY)
                .post("/orders/orders");

        given()
                .header("X-User-Id", userId.toString())
        .when()
                .get("/orders/orders")
        .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("[0].product_type", equalTo("ARCHIVE"));
    }

    @Test
    @DisplayName("GET /orders/orders/{order_id} — заказ по id")
    @Description("Получение одного заказа по order_id")
    void getOrderById_success() {
        UUID userId = TestUsers.randomUserId();
        prepareUserWithBalance(userId, 1000);

        String orderId = given()
                .header("X-User-Id", userId.toString())
                .contentType(ContentType.JSON)
                .body(ARCHIVE_ORDER_BODY)
                .post("/orders/orders")
                .then()
                .statusCode(200)
                .extract()
                .path("order_id");

        given()
                .header("X-User-Id", userId.toString())
        .when()
                .get("/orders/orders/" + orderId)
        .then()
                .statusCode(200)
                .body("order_id", equalTo(orderId))
                .body("price", equalTo(120))
                .body("status", anyOf(equalTo("PAYMENT_PENDING"), equalTo("PAID"), equalTo("PAYMENT_FAILED")));
    }
}
