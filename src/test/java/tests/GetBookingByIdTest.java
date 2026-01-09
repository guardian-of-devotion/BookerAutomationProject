package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(AllureJunit5.class)
@Epic("Процесс получения информации о бронировании")
@Feature("Получение информации о бронировании")
public class GetBookingByIdTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;

    @Step("Инициализация API клиента и создание объекта Booking перед тестом")
    @BeforeEach
    public void setup() throws JsonProcessingException {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        newBooking = new NewBooking();
        newBooking.setFirstname("John");
        newBooking.setLastname("Doe");
        newBooking.setTotalprice(150);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new BookingDates("2024-01-01", "2024-01-05"));
        newBooking.setAdditionalneeds("Breakfast");

        String requestBody = objectMapper.writeValueAsString(newBooking);
        Response response = apiClient.createBooking(requestBody);

        String responseBody = response.asString();
        createdBooking = objectMapper.readValue(responseBody, CreatedBooking.class);
    }

    @Step("Отправка запроса на получение информации о бронировании")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void testGetBookingById() throws Exception {
        // Отправка GET-запроса на получение бронирования по ID
        Response response = apiClient.getBookingById(createdBooking.getBookingid());

        // Проверка статус-кода
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализация тела ответа
        String responseBody = response.getBody().asString();
        Booking booking = objectMapper.readValue(responseBody, Booking.class);

        // Проверки
        assertThat(booking.getFirstname()).isEqualTo("John");
        assertThat(booking.getLastname()).isEqualTo("Doe");
        assertThat(booking.getTotalprice()).isEqualTo(150);
        assertThat(booking.isDepositpaid()).isEqualTo(true);
        assertThat(booking.getBookingdates().getCheckin()).isEqualTo("2024-01-01");
        assertThat(booking.getBookingdates().getCheckout()).isEqualTo("2024-01-05");
        assertThat(booking.getAdditionalneeds()).isEqualTo("Breakfast");
    }

    @Step("Удаление бронирования")
    @AfterEach
    public void tearDown() {
        apiClient.deleteBooking(createdBooking.getBookingid());
        assertThat(apiClient.getBookingById(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
    }
}
