package br.com.seubarriga.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hooks {
    @Before
    public void iniciarNavegador() {
        DriverFactory.startDriver();
    }

    @After
    public void fecharNavegador() {
        DriverFactory.stopDriver();
    }
}
