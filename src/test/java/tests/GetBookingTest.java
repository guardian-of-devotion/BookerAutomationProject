package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import core.clients.APIClient;
import core.models.BookingId;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class GetBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBooking() throws Exception {
        // Выполняем запрос к эндпойнту /booking через метод APIClient
        Response response = apiClient.getBooking();

        // Проверяем, что статус код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализуем тело ответа в список объектов Booking
        String responseBody = response.getBody().asString();
        List<BookingId> bookings = objectMapper.readValue(responseBody, new TypeReference<List<BookingId>>() {});

        // Проверяем, что тело ответа содержит объекты Booking
        assertThat(bookings).isNotEmpty();

        // Проверяем, что каждый объект booking содержит валидное значение bookingid
        for (BookingId booking : bookings) {
            assertThat(booking.getBookingid()).isGreaterThan(0); // bookingid должен быть больше 0
        }
    }
}
