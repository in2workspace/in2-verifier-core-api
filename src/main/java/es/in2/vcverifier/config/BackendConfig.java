package es.in2.vcverifier.config;

import es.in2.vcverifier.config.properties.BackendProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BackendConfig {

    private final BackendProperties properties;

    public String getUrl() {
        return properties.url();
    }

    public String getPrivateKey() {
        String privateKey = properties.identity().privateKey();
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }
        return privateKey;
    }

    public String getDidKey() {
        return properties.identity().didKey();
    }

    private BackendProperties.TrustFramework getSelectedTrustFramework() {
        return properties.getDOMETrustFrameworkByName();
    }

    public String getTrustedIssuerListUri() {
        return getSelectedTrustFramework().trustedIssuersListUrl();
    }

    public String getClientsRepositoryUri() {
        return getSelectedTrustFramework().trustedServicesListUrl();
    }

    public String getRevocationListUri() {
        return getSelectedTrustFramework().revokedCredentialListUrl();
    }

    // Legacy PlainListEntity status list URLs whose status check is bypassed.
    // TODO remove once the last credential of this type expires in DOME.
    public List<String> getPlainListEntityBypassUrls() {
        List<String> urls = properties.plainListEntityBypassUrls();
        if (urls == null) {
            return List.of();
        }
        return urls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(BackendConfig::normalizeUrl)
                .toList();
    }

    public boolean isPlainListEntityBypassUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return getPlainListEntityBypassUrls().contains(normalizeUrl(url));
    }

    private static String normalizeUrl(String url) {
        String normalized = url.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    // todo currently unused, will be used when Verifier can manage multiple trustframeworks
    public List<BackendProperties.TrustFramework> getAllTrustFrameworks() {
        return properties.trustFrameworks();
    }
}
