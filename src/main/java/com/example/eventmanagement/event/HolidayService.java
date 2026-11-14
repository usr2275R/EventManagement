package com.example.eventmanagement.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayService {

    private static final Logger log = LoggerFactory.getLogger(HolidayService.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;
    private final String defaultCountryCode;

    public HolidayService(@Value("${holiday.country-code:US}") String defaultCountryCode) {
        this.webClient =WebClient.builder().baseUrl("https://date.nager.at/api/v3").build();
        this.defaultCountryCode = defaultCountryCode;
    }

//    public List<Holiday> getPublicHolidays(int year, String countryCode) {
//        return List.of(new Holiday(LocalDate.of(2026, 12, 25), "Christmas", "Christmas Day", "IN"));
//    }

  public List<Holiday> getPublicHolidays(int year, String countryCode) {
        try {
            List<Holiday> holidays = webClient.get()
                    .uri("/PublicHolidays/{year}/{country}", year, countryCode)
                    .retrieve()
                    .bodyToFlux(Holiday.class)
                    .collectList()
                    .block(TIMEOUT);
            return holidays != null ? holidays : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch holidays for {} {}: ", countryCode, year, e);
            return Collections.emptyList();
        }
    }

    public Optional<Holiday> getHoliday(LocalDate date, String countryCode) {
        return getPublicHolidays(date.getYear(), countryCode).stream()
                .filter(h -> h.date().equals(date))
                .findFirst();
    }

    public Optional<Holiday> getHoliday(LocalDate date) {
        return getHoliday(date, defaultCountryCode);
    }

    public boolean isHoliday(LocalDate date, String countryCode) {
        return getHoliday(date, countryCode).isPresent();
    }


    public boolean isHoliday(LocalDate date) {
        return isHoliday(date, defaultCountryCode);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Holiday(LocalDate date, String localName, String name, String countryCode) {
    }

}
