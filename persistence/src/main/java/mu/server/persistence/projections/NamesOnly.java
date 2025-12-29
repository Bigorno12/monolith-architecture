package mu.server.persistence.projections;

public record NamesOnly(Long id, String firstname, String username) {
    public NamesOnly {
        if (firstname.isBlank()) {
            throw new IllegalArgumentException("Name is empty");
        }

        if (username.isBlank()) {
            throw new IllegalArgumentException("Username is empty");
        }
    }

    public NamesOnly(String firstname, String username) {
        this(1L, firstname, username);
    }
}
