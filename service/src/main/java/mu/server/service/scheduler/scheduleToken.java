package mu.server.service.scheduler;

import lombok.RequiredArgsConstructor;
import mu.server.persistence.entity.Token;
import mu.server.persistence.repository.TokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class scheduleToken {

    private final TokenRepository tokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void tokenDeletion() {
        List<Token> tokens = tokenRepository.findTokensWhereExpiredAndRevokedIsTrue()
                .stream()
                .toList();

        if (!tokens.isEmpty()) {
            tokenRepository.deleteAll(tokens);
            tokenRepository.flush();
        }
    }
}
