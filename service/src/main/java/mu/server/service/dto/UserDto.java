package mu.server.service.dto;

import lombok.Builder;

@Builder
public record UserDto(Long id, String name, String username, AddressDto addressDto, String phone, String website, CompanyDto companyDto) {

    public record AddressDto(String street, String city, String zipcode, GeoDto geoDto) {}
    public record GeoDto(String lat, String lng) {}
    public record CompanyDto(String name, String catchPhrase, String bs) {}

}
