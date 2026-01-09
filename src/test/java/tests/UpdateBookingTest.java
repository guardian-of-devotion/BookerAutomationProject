package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.*;
import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(AllureJunit5.class)
@Epic("Обновление информации о бронировании")
@Feature("Проверка полного и частичного обновлений бронирования")
public class UpdateBookingTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking createdBooking;
    private NewBooking newBooking;
    private int bookingid;

    @Step("Инициализация API клиента и создание объекта Booking перед тестом")
    @BeforeEach
    public void setup() throws JsonProcessingException {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        // Создаем объект newBooking с необходимыми данными
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

        bookingid = createdBooking.getBookingid();
    }

    @Step("Отправка запроса на полное изменение бронирования")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void testUpdateBooking() throws JsonProcessingException {
        UpdatedBooking updatedBooking = new UpdatedBooking();
        updatedBooking.setFirstname("Ann");
        updatedBooking.setLastname("Queen");
        updatedBooking.setTotalprice(100);
        updatedBooking.setDepositpaid(true);
        updatedBooking.setBookingdates(new BookingDates("2025-02-01", "2025-02-10"));
        updatedBooking.setAdditionalneeds("Dinner");

        String requestBody = objectMapper.writeValueAsString(updatedBooking);

        Response response = apiClient.updateBooking(bookingid, requestBody);
        assertThat(response.getStatusCode()).isEqualTo(200);

        UpdatedBooking responseBooking = objectMapper.readValue(response.asString(), UpdatedBooking.class);

        assertThat(responseBooking.getFirstname()).isEqualTo("Ann");
        assertThat(responseBooking.getLastname()).isEqualTo("Queen");
        assertThat(responseBooking.getTotalprice()).isEqualTo(100);
        assertThat(responseBooking.isDepositpaid()).isEqualTo(true);
        assertThat(responseBooking.getBookingdates().getCheckin()).isEqualTo("2025-02-01");
        assertThat(responseBooking.getBookingdates().getCheckout()).isEqualTo("2025-02-10");
        assertThat(responseBooking.getAdditionalneeds()).isEqualTo("Dinner");
    }

    @Step("Отправка запроса на полное изменение бронирования")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    public void partialUpdateBooking() throws JsonProcessingException {
        PartiallyUpdatedBooking partiallyUpdatedBooking = new PartiallyUpdatedBooking();
        partiallyUpdatedBooking.setFirstname("Kevin");
        partiallyUpdatedBooking.setLastname("Walker");
        partiallyUpdatedBooking.setTotalprice(150);

        String requestBody = objectMapper.writeValueAsString(partiallyUpdatedBooking);

        Response response = apiClient.partialUpdateBooking(bookingid, requestBody);
        assertThat(response.getStatusCode()).isEqualTo(200);

        UpdatedBooking responseBooking = objectMapper.readValue(response.asString(), UpdatedBooking.class);

        assertThat(responseBooking.getFirstname()).isEqualTo("Kevin");
        assertThat(responseBooking.getLastname()).isEqualTo("Walker");
        assertThat(responseBooking.getTotalprice()).isEqualTo(150);
        assertEquals(newBooking.isDepositpaid(), createdBooking.getBooking().isDepositpaid());
        assertEquals(newBooking.getBookingdates().getCheckin(), createdBooking.getBooking().getBookingdates().getCheckin());
        assertEquals(newBooking.getBookingdates().getCheckout(), createdBooking.getBooking().getBookingdates().getCheckout());
        assertEquals(newBooking.getAdditionalneeds(), createdBooking.getBooking().getAdditionalneeds());
    }

    @Step("Удаление бронирования")
    @AfterEach
    public void tearDown() {
        apiClient.deleteBooking(createdBooking.getBookingid());
        assertThat(apiClient.getBookingById(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);
    }
}
