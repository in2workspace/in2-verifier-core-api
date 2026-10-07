package es.in2.vcverifier.config;

import es.in2.vcverifier.config.properties.BackendProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {BackendConfig.class, BackendConfigTest.TestConfig.class})
@ActiveProfiles("test")
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
class BackendConfigTest {

    @Autowired
    private BackendConfig backendConfig;

    @Test
    void testBackendConfig() {
        assertThat(backendConfig.getUrl())
                .as("Backend URL should match")
                .isEqualTo("https://raw.githubusercontent.com");

     assertThat(backendConfig.getPrivateKey())
        .as("Private key should remove 0x prefix")
        .isEqualTo("73e509a7681d4a395b1ced75681c4dc4020dbab02da868512276dd766733d5b5");

        assertThat(backendConfig.getTrustedIssuerListUri())
                .as("Trusted Issuer List URL should match")
                .isEqualTo("https://raw.githubusercontent.com");

        assertThat(backendConfig.getClientsRepositoryUri())
                .as("Clients Repository URI should match")
                .isEqualTo("https://raw.githubusercontent.com/in2workspace/in2-dome-gitops/refs/heads/main/trust-framework/tenant-red-marketplace/trusted_services_list.yaml");

        assertThat(backendConfig.getRevocationListUri())
                .as("Revocation List URI should match")
                .isEqualTo("https://raw.githubusercontent.com/in2workspace/in2-dome-gitops/refs/heads/main/trust-framework/tenant-red-marketplace/revoked_credential_list.yaml");
    }

    @Test
    void testPlainListEntityBypassUrlsEmptyByDefault() {
        assertThat(backendConfig.getPlainListEntityBypassUrls()).isEmpty();
        assertThat(backendConfig.isPlainListEntityBypassUrl("https://issuer.example.com/backoffice/v1/credentials/status/1")).isFalse();
    }

    @Test
    void testPlainListEntityBypassUrlsMatching() {
        BackendProperties properties = new BackendProperties(
                "https://raw.githubusercontent.com",
                null,
                List.of(),
                List.of(" https://Issuer.example.com ", "", "not a url", "https://other.example.com/backoffice/v1/credentials/status/1",
                        "http://local.example.com:8080/")
        );
        BackendConfig config = new BackendConfig(properties);

        // Entries are reduced to their base URL: paths are ignored, invalid entries dropped
        assertThat(config.getPlainListEntityBypassUrls()).containsExactly(
                "https://issuer.example.com",
                "https://other.example.com",
                "http://local.example.com:8080");
        // Any status list on a configured base URL, whatever its path
        assertThat(config.isPlainListEntityBypassUrl("https://issuer.example.com/backoffice/v1/credentials/status/1")).isTrue();
        assertThat(config.isPlainListEntityBypassUrl("https://issuer.example.com/backoffice/v1/credentials/status/2/")).isTrue();
        assertThat(config.isPlainListEntityBypassUrl("https://other.example.com/backoffice/v1/credentials/status/2")).isTrue();
        assertThat(config.isPlainListEntityBypassUrl("http://local.example.com:8080/status/1")).isTrue();
        // Different scheme, port, host or subdomain
        assertThat(config.isPlainListEntityBypassUrl("http://issuer.example.com/backoffice/v1/credentials/status/1")).isFalse();
        assertThat(config.isPlainListEntityBypassUrl("http://local.example.com/status/1")).isFalse();
        assertThat(config.isPlainListEntityBypassUrl("https://unknown.example.com/backoffice/v1/credentials/status/1")).isFalse();
        assertThat(config.isPlainListEntityBypassUrl("https://sub.issuer.example.com/backoffice/v1/credentials/status/1")).isFalse();
        assertThat(config.isPlainListEntityBypassUrl("https://issuer.example.com.evil.org/status/1")).isFalse();
        assertThat(config.isPlainListEntityBypassUrl("not a url")).isFalse();
        assertThat(config.isPlainListEntityBypassUrl(null)).isFalse();
    }

    @Configuration
    @EnableConfigurationProperties(BackendProperties.class)
    static class TestConfig {
    }
}
