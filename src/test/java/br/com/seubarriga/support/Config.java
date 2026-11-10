package br.com.seubarriga.support;

public final class Config {
    public static final String BASE_URL = System.getenv()
            .getOrDefault("SEUBARRIGA_URL", "https://seubarriga.wcaquino.me");

    private Config() {
    }
}
