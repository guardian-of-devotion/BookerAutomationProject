package core.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BookingId {
    private int bookingid;

    // Конструктор
    @JsonCreator
    public BookingId(@JsonProperty("bookingid") int bookingid) {
        this.bookingid = bookingid;
    }

    // Геттер и сеттер

    public int getBookingid() {
        return bookingid;
    }

    public void setBookingid(int bookingid) {
        this.bookingid = bookingid;
    }
}
