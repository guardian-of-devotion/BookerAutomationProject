package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GetBookingByIdTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBookingById() throws Exception {
        // Отправка GET-запроса на получение бронирования по ID
        Response response = apiClient.getBookingById(14);

        // Проверка статус-кода
        assertThat(response.getStatusCode()).isEqualTo(200);

        // Десериализация тела ответа
        String responseBody = response.getBody().asString();
        Booking booking = objectMapper.readValue(responseBody, Booking.class);

        // Проверки
        assertThat(booking.getFirstname()).isEqualTo("Mark");
        assertThat(booking.getLastname()).isEqualTo("Jones");
        assertThat(booking.getTotalprice()).isEqualTo(931);
        assertThat(booking.isDepositpaid()).isEqualTo(false);
        assertThat(booking.getBookingdates().getCheckin()).isEqualTo("2015-11-12");
        assertThat(booking.getBookingdates().getCheckout()).isEqualTo("2018-01-08");
        assertThat(booking.getAdditionalneeds()).isEqualTo("Breakfast");
    }
}
