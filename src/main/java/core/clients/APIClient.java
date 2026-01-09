package core.clients;

import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class APIClient {
    private final String baseUrl;
    private String token;

    public APIClient() {
        this.baseUrl = determineBaseUrl();
    }

    // Определение базового URL на основе файла конфигурации
    private String determineBaseUrl() {
        String environment = System.getProperty("env", "test");
        String configFileName = "application-" + environment + ".properties";

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found: " + configFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration file: " + configFileName, e);
        }

        return properties.getProperty("baseUrl");
    }

    // Настройка базовых параметров HTTP-запросов
    private RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .baseUri(baseUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .filter(addAuthTokenFilter());
    }

    // GET-запрос на эндпойнт /ping
    public Response ping() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.PING.getPath()) // Используем ENUM для эндпойнта /ping
                .then()
                .statusCode(201)
                .extract()
                .response();
    }

    // GET-запрос на эндпойнт /booking
    public Response getBooking() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    public Response getBookingById(int bookingId) {
        return getRequestSpec()
                .pathParam("id", bookingId)
                .when()
                .get(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .extract().
                response();
    }

    public void createToken(String username, String password) {
        // Тело запроса для получения токена
        String requestBody = String.format("{ \"username\": \"%s\", \"password\": \"%s\" }", username, password);

        Response response = getRequestSpec()
                .body(requestBody)
                .when()
                .post(ApiEndpoints.AUTH.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Извлекаем токен из ответа
        token = response.jsonPath().getString("token");
    }

    public String getToken() {
        return token;
    }

    // Фильтр для добавления токена в заголовок Authorization
    private Filter addAuthTokenFilter() {
        return (FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) -> {
            if (token != null) {
                requestSpec.header("Cookie", "token=" + token);
            }
            return ctx.next(requestSpec, responseSpec);
        };
    }

    // DELETE запрос на метод /booking
    public Response deleteBooking(int bookingId) {
        return getRequestSpec()
                .pathParam("id", bookingId) // Указываем path parameter для ID
                .when()
                .delete(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .response();
    }

    public Response createBooking(String newBooking) {
        return getRequestSpec()
                .body(newBooking)
                .log().all()
                .when()
                .post(ApiEndpoints.BOOKING.getPath())
                .then()
                .log().all()
                .extract()
                .response();
    }

    public Response updateBooking(int bookingid, String requestBody) {
        return getRequestSpec()
                .pathParam("id", bookingid)
                .body(requestBody)
                .log().all()
                .when()
                .put(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .log().all()
                .extract()
                .response();
    }

    public Response partialUpdateBooking(int bookingid, String requestBody) {
        return getRequestSpec()
                .pathParam("id", bookingid)
                .body(requestBody)
                .log().all()
                .when()
                .patch(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .log().all()
                .extract()
                .response();
    }

    public Response getBookingWithFilters(String firstname, String lastname) {
        RequestSpecification spec = getRequestSpec();

        if (firstname != null) {
            spec = spec.queryParam("firstname", firstname);
        }
        if (lastname != null) {
            spec = spec.queryParam("lastname", lastname);
        }

        return spec
                .when()
                .get(ApiEndpoints.BOOKING.getPath())
                .then()
                .extract()
                .response();
    }
}
