package mu.server.service.dto;

import lombok.Builder;

import java.util.Objects;

@Builder
public record UserCreation(String name, String username, AddressCreation address, String phone, String website,
                           GeoCreation geoCreation, CompanyCreation companyCreation) {

    public UserCreation {
        Objects.requireNonNull(name, "Name is null");
        Objects.requireNonNull(username, "Username is null");
        Objects.requireNonNull(address, "Address is null");
        Objects.requireNonNull(phone, "Phone is null");
        Objects.requireNonNull(website, "Website is null");
        Objects.requireNonNull(geoCreation, "Geo Creation is null");
        Objects.requireNonNull(companyCreation, "Company Creation is null");
     }

    @Builder
    public record AddressCreation(String street, String city, String zipcode) {
        public AddressCreation {
            Objects.requireNonNull(street, "Street is null");
            Objects.requireNonNull(city, "City is null");
            Objects.requireNonNull(zipcode, "Zipcode is null");

        }
    }

    @Builder
    public record GeoCreation(String lat, String lng) {
        public GeoCreation {
            Objects.requireNonNull(lat, "lat is null");
            Objects.requireNonNull(lng, "Lng is null");
        }
    }

    @Builder
    public record CompanyCreation(String name, String catchPhase, String bs) {
        public CompanyCreation {
            Objects.requireNonNull(name, "Company Name is null");
            Objects.requireNonNull(catchPhase, "Catch phase is null");
            Objects.requireNonNull(bs, "bs is null");
        }
    }
}
