package mu.server.persistence.projections;

import lombok.Builder;

@Builder
public record RetrieveUsers(String name, String username, String website) {
}
