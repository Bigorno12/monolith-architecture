package mu.server.service.dto;

import lombok.Builder;

@Builder
public record UserCreation(String name, String username, AddressCreation address, String phone, String website,
                           GeoCreation geoCreation, CompanyCreation companyCreation) {

    @Builder
    public record AddressCreation(String street, String city, String zipcode) {

    }

    @Builder
    public record GeoCreation(String lat, String lng) {

    }

    @Builder
    public record CompanyCreation(String name, String catchPhase, String bs) {

    }
}
