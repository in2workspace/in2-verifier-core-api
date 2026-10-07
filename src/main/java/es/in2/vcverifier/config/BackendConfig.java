package es.in2.vcverifier.config;

import es.in2.vcverifier.config.properties.BackendProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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

    // Issuer base URLs (scheme://host[:port]) whose legacy PlainListEntity status lists are bypassed.
    // Any path in a configured entry is ignored. Invalid entries are ignored.
    // Remove once the last credential of this type expires in DOME.
    public List<String> getPlainListEntityBypassUrls() {
        List<String> urls = properties.plainListEntityBypassUrls();
        if (urls == null) {
            return List.of();
        }
        return urls.stream()
                .map(BackendConfig::toBaseUrl)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    // True when the status list URL is on one of the configured base URLs, whatever its path.
    public boolean isPlainListEntityBypassUrl(String url) {
        String baseUrl = toBaseUrl(url);
        return baseUrl != null && getPlainListEntityBypassUrls().contains(baseUrl);
    }

    // "https://Issuer.example.org/backoffice/v1/credentials/status/1" -> "https://issuer.example.org"
    private static String toBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(url.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            String baseUrl = uri.getScheme().toLowerCase(Locale.ROOT) + "://" + uri.getHost().toLowerCase(Locale.ROOT);
            return uri.getPort() == -1 ? baseUrl : baseUrl + ":" + uri.getPort();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    // todo currently unused, will be used when Verifier can manage multiple trustframeworks
    public List<BackendProperties.TrustFramework> getAllTrustFrameworks() {
        return properties.trustFrameworks();
    }
}
