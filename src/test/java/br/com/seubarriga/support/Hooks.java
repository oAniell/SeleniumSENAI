package br.com.seubarriga.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class Hooks {

    /**
     * Cadastra, uma única vez antes de toda a suíte, o usuário usado nos logins.
     * Assim cada cenário é independente e pode ser executado isoladamente.
     */
    @BeforeAll
    public static void cadastrarUsuarioDeTeste() {
        DriverFactory.startDriver();
        try {
            WebDriver driver = DriverFactory.getDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            driver.get(Config.BASE_URL + "/cadastro");
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("input[name='nome'], input#nome")))
                    .sendKeys(TestUser.getName());
            driver.findElement(By.cssSelector("input[type='email'], input[name='email']"))
                    .sendKeys(TestUser.getEmail());
            driver.findElement(By.cssSelector("input[type='password'], input[name='senha']"))
                    .sendKeys(TestUser.getPassword());
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//input[(@type='submit' or @type='button') and @value='Cadastrar']"
                            + " | //*[self::a or self::button][normalize-space(.)='Cadastrar']")))
                    .click();

            // Lança TimeoutException se a confirmação não aparecer, abortando a suíte.
            wait.until(currentDriver -> {
                List<WebElement> alerts = currentDriver.findElements(By.cssSelector(".alert"));
                return alerts.stream().anyMatch(alert ->
                        alert.getText().contains("Usuário inserido com sucesso"));
            });
        } finally {
            DriverFactory.stopDriver();
        }
    }

    @Before
    public void iniciarNavegador() {
        DriverFactory.startDriver();
    }

    @After
    public void fecharNavegador() {
        DriverFactory.stopDriver();
    }
}
