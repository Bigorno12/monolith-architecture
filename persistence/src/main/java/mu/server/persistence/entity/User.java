package mu.server.persistence.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mu.server.persistence.embeded.Address;
import mu.server.persistence.embeded.Company;

@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "_user")
public class User {

    @Id
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "username", nullable = false)
    private String username;

    @Embedded
    @AttributeOverride(name = "street", column = @Column(name = "street"))
    @AttributeOverride(name = "city", column = @Column(name = "city"))
    @AttributeOverride(name = "zipcode", column = @Column(name = "zipcode"))
    @AttributeOverride(name = "geo.lat", column = @Column(name = "geo_lat"))
    @AttributeOverride(name = "geo.lng", column = @Column(name = "geo_lng"))
    private Address address;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "website", nullable = false)
    private String website;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "company_name"))
    @AttributeOverride(name = "catchPhrase", column = @Column(name = "company_catchPhrase"))
    @AttributeOverride(name = "bs", column = @Column(name = "company_bs"))
    private Company company;
}
