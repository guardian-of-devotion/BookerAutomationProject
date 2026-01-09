package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.*;
import io.qameta.allure.*;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(AllureJunit5.class)
@Epic("Процесс получения информации о бронировании")
@Feature("Поиск информации о бронировании с фильтрами: имя, фамилия")
public class GetBookingWithFiltersTest {
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private CreatedBooking firstCreatedBooking;
    private CreatedBooking secondCreatedBooking;
    private CreatedBooking thirdCreatedBooking;
    private NewBooking firstNewBooking;
    private NewBooking secondNewBooking;
    private NewBooking thirdNewBooking;

    @Step("Инициализация API клиента и создание трех объектов Booking перед тестом")
    @BeforeEach
    public void setup() throws JsonProcessingException {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
        apiClient.createToken("admin", "password123");

        // Создание первого бронирования
        firstNewBooking = new NewBooking();
        firstNewBooking.setFirstname("Alfredo");
        firstNewBooking.setLastname("Gomez");
        firstNewBooking.setTotalprice(1000);
        firstNewBooking.setDepositpaid(true);
        firstNewBooking.setBookingdates(new BookingDates("2026-01-01", "2026-01-05"));
        firstNewBooking.setAdditionalneeds("Breakfast");

        String requestBodyOfFirstBooking = objectMapper.writeValueAsString(firstNewBooking);
        Response firstResponse = apiClient.createBooking(requestBodyOfFirstBooking);

        String firstResponseBody = firstResponse.asString();
        firstCreatedBooking = objectMapper.readValue(firstResponseBody, CreatedBooking.class);

        // Создание второго бронирования
        secondNewBooking = new NewBooking();
        secondNewBooking.setFirstname("Ann");
        secondNewBooking.setLastname("Queen");
        secondNewBooking.setTotalprice(120);
        secondNewBooking.setDepositpaid(false);
        secondNewBooking.setBookingdates(new BookingDates("2025-05-11", "2025-05-21"));
        secondNewBooking.setAdditionalneeds("Dinner");

        String requestBodyOfSecondBooking = objectMapper.writeValueAsString(secondNewBooking);
        Response secondResponse = apiClient.createBooking(requestBodyOfSecondBooking);

        String secondResponseBody = secondResponse.asString();
        secondCreatedBooking = objectMapper.readValue(secondResponseBody, CreatedBooking.class);

        // Создание третьего бронирования
        thirdNewBooking = new NewBooking();
        thirdNewBooking.setFirstname("Adolf");
        thirdNewBooking.setLastname("Doe");
        thirdNewBooking.setTotalprice(200);
        thirdNewBooking.setDepositpaid(true);
        thirdNewBooking.setBookingdates(new BookingDates("2025-06-12", "2025-06-19"));
        thirdNewBooking.setAdditionalneeds("Breakfast");

        String requestBodyOfThirdBooking = objectMapper.writeValueAsString(thirdNewBooking);
        Response thirdResponse = apiClient.createBooking(requestBodyOfThirdBooking);

        String thirdResponseBody = thirdResponse.asString();
        thirdCreatedBooking = objectMapper.readValue(thirdResponseBody, CreatedBooking.class);
    }

    @Severity(SeverityLevel.CRITICAL)
    @Step("Отправка запроса на получение информации о бронированиях с использованием фильтров: имя, фамилия")
    @ParameterizedTest(name = "firstname={0}, lastname={1}")
    @ArgumentsSource(tests.providers.BookingFilterProvider.class)
    public void testGetBookingWithFilters(String firstname, String lastname)
            throws JsonProcessingException {
        Response response = apiClient.getBookingWithFilters(firstname, lastname);
        assertThat(response.getStatusCode()).isEqualTo(200);

        String responseBody = response.getBody().asString();
        List<BookingId> bookingIds = objectMapper.readValue(responseBody, new TypeReference<List<BookingId>>() {
        });

        assertThat(bookingIds).isNotEmpty();

        boolean hasMatchingBooking = false;

        for (BookingId bookingId: bookingIds) {
            Response bookingResponse = apiClient.getBookingById(bookingId.getBookingid());
            assertThat(bookingResponse.getStatusCode()).isEqualTo(200);

            Booking booking = objectMapper.readValue(bookingResponse.asString(), Booking.class);

            boolean matches = true;

            if (firstname != null) {
                matches = matches && firstname.equals(booking.getFirstname());
            }
            if (lastname != null) {
                matches = matches && lastname.equals(booking.getLastname());
            }

            if (matches) {
                hasMatchingBooking = true;
                break;
            }
        }

        assertThat(hasMatchingBooking).isTrue();
    }

    @Step("Удаление трех бронирований")
    @AfterEach
    public void tearDown() {
        apiClient.deleteBooking(firstCreatedBooking.getBookingid());
        apiClient.deleteBooking(secondCreatedBooking.getBookingid());
        apiClient.deleteBooking(thirdCreatedBooking.getBookingid());

        assertThat(apiClient.getBookingById(firstCreatedBooking.getBookingid()).getStatusCode()).isEqualTo(404);
        assertThat(apiClient.getBookingById(secondCreatedBooking.getBookingid()).getStatusCode()).isEqualTo(404);
        assertThat(apiClient.getBookingById(thirdCreatedBooking.getBookingid()).getStatusCode()).isEqualTo(404);

    }
}
