package mu.server.persistence.projections;

public record NamesOnly(Long id, String name, String username) {
    public NamesOnly {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name is empty");
        }

        if (username.isBlank()) {
            throw new IllegalArgumentException("Username is empty");
        }
    }

    public NamesOnly(String name, String username) {
        this(1L, name, username);
    }
}
