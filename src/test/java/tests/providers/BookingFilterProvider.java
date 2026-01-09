package tests.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.List;
import java.util.stream.Stream;

public class BookingFilterProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return List.of(
                Arguments.of("John", null),
                Arguments.of(null, "Doe"),
                Arguments.of("Adolf", "Doe")
        ).stream();
    }
}
