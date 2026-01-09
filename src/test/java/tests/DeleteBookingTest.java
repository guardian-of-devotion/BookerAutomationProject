package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.BookingId;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    // Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        assertThat(apiClient.getToken()).isNotNull();
    }

    @Test
    public void testDeleteBooking() throws Exception {

        // Получаем список ID бронирований
        Response response = apiClient.getBooking();
        String responseBody = response.getBody().asString();
        List<BookingId> bookings = objectMapper.readValue(responseBody, new TypeReference<List<BookingId>>() {});

        // Создаем bookingId с id = 17
        int bookingId = bookings.get(17).getBookingid();

        // Удаляем выбранное бронирование и проверяем статус
        Response deleteResponse = apiClient.deleteBooking(bookingId);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(201);

        // Проверяем, что нет этого бронирования
        Response getResponse = apiClient.getBookingById(bookingId);
        assertThat(getResponse.getStatusCode()).isEqualTo(404);

    }
}
