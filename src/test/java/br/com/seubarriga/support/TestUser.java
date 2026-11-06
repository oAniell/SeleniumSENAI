package br.com.seubarriga.support;

import java.util.UUID;

public final class TestUser {
    private static final String ID = UUID.randomUUID().toString().replace("-", "");
    private static final String NAME = "Usuario Selenium " + ID.substring(0, 8);
    private static final String EMAIL = "selenium." + ID + "@example.com";
    private static final String PASSWORD = "Teste@" + ID.substring(0, 12);

    private TestUser() {
    }

    public static String getName() {
        return NAME;
    }

    public static String getEmail() {
        return EMAIL;
    }

    public static String getPassword() {
        return PASSWORD;
    }
}
