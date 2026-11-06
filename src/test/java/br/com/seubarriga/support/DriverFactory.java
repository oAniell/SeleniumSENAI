package br.com.seubarriga.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.util.Locale;

public final class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static void startDriver() {
        String browser = System.getenv().getOrDefault("BROWSER", "chrome")
                .toLowerCase(Locale.ROOT);
        boolean headless = Boolean.parseBoolean(System.getenv().getOrDefault("HEADLESS", "true"));

        WebDriver driver;
        if ("chrome".equals(browser)) {
            driver = new ChromeDriver(chromeOptions(headless));
        } else if ("edge".equals(browser)) {
            driver = new EdgeDriver(edgeOptions(headless));
        } else {
            throw new IllegalArgumentException(
                    "Navegador não suportado: " + browser + ". Use 'chrome' ou 'edge'.");
        }
        DRIVER.set(driver);
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("O WebDriver ainda não foi inicializado.");
        }
        return driver;
    }

    public static void stopDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove();
            }
        }
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000");
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1440,1000");
        return options;
    }
}
