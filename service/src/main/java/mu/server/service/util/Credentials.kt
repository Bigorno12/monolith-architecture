package mu.server.service.util

import org.keycloak.representations.idm.CredentialRepresentation

object Credentials {
    fun createCredentialRepresentation(password: String): CredentialRepresentation =
        CredentialRepresentation()
            .also { credentialRepresentation ->
                credentialRepresentation.isTemporary = false
                credentialRepresentation.type = CredentialRepresentation.PASSWORD
                credentialRepresentation.value = password
            }
}
