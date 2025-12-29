package mu.server.service.mapper;

import mu.server.persistence.entity.Token;
import mu.server.persistence.entity.User;
import mu.server.persistence.enumeration.TokenType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface TokenMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "userModel")
    @Mapping(target = "token", source = "jwtToken")
    @Mapping(target = "expired", constant = "false")
    @Mapping(target = "revoked", constant = "false")
    @Mapping(target = "tokenType", qualifiedByName = "mapTokenType")
    Token mapToEntity(User userModel, String jwtToken, TokenType tokenType);

    @Named("mapTokenType")
    default TokenType mapTokenType(TokenType tokenType) {
        return Optional.ofNullable(tokenType)
                .orElse(TokenType.BEARER);
    }
}
