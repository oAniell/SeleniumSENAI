package br.com.seubarriga.steps;

import br.com.seubarriga.support.DriverFactory;
import br.com.seubarriga.support.TestUser;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class AppSteps {
    private static final String BASE_URL = System.getenv()
            .getOrDefault("SEUBARRIGA_URL", "https://seubarriga.wcaquino.me");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale PORTUGUESE = Locale.forLanguageTag("pt-BR");

    private final WebDriver driver = DriverFactory.getDriver();
    private final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    private String accountName;

    @Quando("cadastro um novo usuário para os testes")
    public void cadastrarNovoUsuarioParaOsTestes() {
        driver.get(BASE_URL + "/cadastro");
        firstElement(By.cssSelector("input[name='nome'], input#nome"))
                .sendKeys(TestUser.getName());
        firstElement(By.cssSelector("input[type='email'], input[name='email']"))
                .sendKeys(TestUser.getEmail());
        firstElement(By.cssSelector("input[type='password'], input[name='senha']"))
                .sendKeys(TestUser.getPassword());
        clickText("Cadastrar");
    }

    @Dado("que acessei a aplicação e fiz login")
    public void acessarAplicacaoEFazerLogin() {
        driver.get(BASE_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='email'], input[name='email']")))
                .sendKeys(TestUser.getEmail());
        driver.findElement(By.cssSelector("input[type='password']"))
                .sendKeys(TestUser.getPassword());
        clickText("Entrar");

        wait.until(currentDriver -> !currentDriver.getCurrentUrl().contains("/login"));
    }

    @Quando("navego para a criação de contas")
    public void navegarParaCriacaoDeContas() {
        openAccountSection("Adicionar");
    }

    @Quando("tento salvar uma conta sem nome")
    public void tentarSalvarContaSemNome() {
        clickText("Salvar");
    }

    @Entao("vejo a notificação {string}")
    public void verificarNotificacao(String expectedText) {
        String notification = wait.until(currentDriver -> {
            List<WebElement> alerts = currentDriver.findElements(
                    By.cssSelector("[role='alert'], .alert"));
            return alerts.stream()
                    .filter(WebElement::isDisplayed)
                    .map(WebElement::getText)
                    .filter(text -> !text.isBlank())
                    .filter(text -> text.contains(expectedText))
                    .findFirst()
                    .orElse(null);
        });
        Assertions.assertNotNull(notification,
                "A notificação esperada não foi exibida: " + expectedText);
    }

    @Quando("crio uma conta com nome único")
    public void criarContaComNomeUnico() {
        accountName = "Conta Selenium " + UUID.randomUUID().toString().substring(0, 8);
        saveAccount(accountName);
    }

    @Quando("tento criar novamente a mesma conta")
    public void tentarCriarContaDuplicada() {
        openAccountSection("Adicionar");
        saveAccount(accountName);
    }

    @Quando("crio uma movimentação para essa conta")
    public void criarMovimentacaoParaConta() {
        clickTextContaining("Movimentação");
        fillDate("data_transacao", "Data da Movimentação", LocalDate.now());
        fillDate("data_pagamento", "Data do Pagamento", LocalDate.now());
        fillInput("descricao", "Movimentação de teste automatizado");
        fillInput("interessado", "Teste Selenium");
        fillInput("valor", "25.50");
        selectOption("conta", "Conta", accountName);
        selectOption("tipo", "Tipo da Movimentação", "Despesa");
        selectOption("status", "Situação", "Pago");
        clickText("Salvar");
        verificarNotificacao("Movimentação adicionada com sucesso");
    }

    @Quando("busco o resumo mensal do mês e ano atuais")
    public void buscarResumoMensal() {
        clickTextContaining("Resumo Mensal");

        LocalDate today = LocalDate.now();
        String monthName = today.getMonth().getDisplayName(TextStyle.FULL, PORTUGUESE);
        selectOption("mes", "Mês", monthName);
        selectOption("ano", "Ano", Integer.toString(today.getYear()));
        clickText("Buscar");
    }

    @Entao("o resumo mensal é exibido")
    public void verificarResumoMensal() {
        wait.until(currentDriver -> currentDriver.getPageSource().contains("Resumo Mensal"));
        Assertions.assertTrue(driver.getPageSource().contains("Resumo Mensal"),
                "A página do resumo mensal não foi exibida.");
    }

    @Quando("edito o nome da conta criada")
    public void editarNomeDaContaCriada() {
        openAccountSection("Listar");
        WebElement row = wait.until(currentDriver -> currentDriver.findElements(By.xpath(
                        "//tr[.//*[normalize-space(text())='" + escapeXPath(accountName) + "']]"))
                .stream().findFirst().orElse(null));
        Assertions.assertNotNull(row, "A conta criada não apareceu na lista.");

        List<WebElement> editLinks = row.findElements(By.xpath(
                ".//*[self::a or self::button][@title='Editar' or @aria-label='Editar' "
                        + "or contains(normalize-space(.), 'Editar')]"));
        WebElement editLink = editLinks.isEmpty()
                ? row.findElement(By.xpath("./td[last()]//*[self::a or self::button]"))
                : editLinks.get(0);
        editLink.click();

        String updatedName = accountName + " Editada";
        WebElement nameInput = editableInput(
                By.cssSelector("form input[name='nome'], form input#nome, form input[type='text']"));
        nameInput.clear();
        nameInput.sendKeys(updatedName);
        clickText("Salvar");
        verificarNotificacao("Conta alterada com sucesso");
    }

    private void saveAccount(String name) {
        WebElement nameInput = editableInput(
                By.cssSelector("form input[name='nome'], form input#nome, form input[type='text']"));
        if (!name.isBlank()) {
            nameInput.clear();
            nameInput.sendKeys(name);
        }
        clickText("Salvar");
    }

    private void openAccountSection(String action) {
        clickText("Contas");
        clickText(action);
    }

    private void clickText(String text) {
        String escapedText = escapeXPath(text);
        By locator = By.xpath("//*[self::a or self::button]"
                + "[normalize-space(.)='" + escapedText + "']"
                + " | //input[(@type='submit' or @type='button') and @value='"
                + escapedText + "']");
        try {
            wait.until(currentDriver -> {
                try {
                    WebElement element = currentDriver.findElement(locator);
                    if (!element.isDisplayed() || !element.isEnabled()) {
                        return false;
                    }
                    element.click();
                    return true;
                } catch (StaleElementReferenceException exception) {
                    return false;
                }
            });
        } catch (TimeoutException exception) {
            String pageText = driver.findElement(By.tagName("body")).getText();
            throw new IllegalStateException("Não foi possível clicar em '" + text
                    + "' na página " + driver.getCurrentUrl()
                    + ". Conteúdo visível: " + pageText, exception);
        }
    }

    private void clickTextContaining(String text) {
        By locator = By.xpath("//*[self::a or self::button]"
                + "[contains(normalize-space(.), '" + escapeXPath(text) + "')]");
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    private void fillInput(String name, String value) {
        WebElement input = editableInput(By.cssSelector(
                "input[name='" + name + "'], input#" + name));
        input.clear();
        input.sendKeys(value);
    }

    private void fillDate(String name, String label, LocalDate date) {
        WebElement input = findByNameOrLabel(name, label, "input");
        input.clear();
        if ("date".equalsIgnoreCase(input.getAttribute("type"))) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];"
                            + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                            + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    input, date.toString());
        } else {
            input.sendKeys(DATE_FORMAT.format(date));
        }
    }

    private void selectOption(String name, String label, String optionText) {
        List<WebElement> selectElements = driver.findElements(
                By.cssSelector("select[name='" + name + "'], select#" + name));
        if (!selectElements.isEmpty()) {
            Select select = new Select(selectElements.get(0));
            try {
                select.selectByVisibleText(optionText);
            } catch (NoSuchElementException exception) {
                if (name.equals("mes")) {
                    selectMonth(select, optionText);
                } else {
                    throw exception;
                }
            }
            return;
        }

        List<WebElement> radioButtons = driver.findElements(
                By.cssSelector("input[type='radio'][name='" + name + "']"));
        if (!radioButtons.isEmpty()) {
            selectRadioOption(radioButtons, optionText);
            return;
        }

        WebElement selectElement = findByNameOrLabel(name, label, "select");
        new Select(selectElement).selectByVisibleText(optionText);
    }

    private void selectRadioOption(List<WebElement> radioButtons, String optionText) {
        for (WebElement radioButton : radioButtons) {
            String id = radioButton.getAttribute("id");
            List<WebElement> labels = id == null || id.isBlank()
                    ? List.of()
                    : driver.findElements(By.cssSelector("label[for='" + id + "']"));
            String labelText = labels.isEmpty() ? "" : labels.get(0).getText().trim();
            String idText = id == null ? "" : id.replaceFirst("^.*_", "");

            if (optionText.equalsIgnoreCase(labelText) || optionText.equalsIgnoreCase(idText)) {
                if (!labels.isEmpty()) {
                    labels.get(0).click();
                } else {
                    radioButton.click();
                }
                return;
            }
        }
        throw new NoSuchElementException("Não foi encontrada a opção de rádio '"
                + optionText + "'.");
    }

    private void selectMonth(Select select, String monthName) {
        int monthNumber = LocalDate.now().getMonthValue();
        for (WebElement option : select.getOptions()) {
            String value = option.getAttribute("value");
            if (Integer.toString(monthNumber).equals(value)
                    || String.format("%02d", monthNumber).equals(value)) {
                select.selectByValue(value);
                return;
            }
        }
        throw new NoSuchElementException("Não foi encontrada a opção do mês " + monthName + ".");
    }

    private WebElement findByNameOrLabel(String name, String label, String tagName) {
        List<WebElement> named = driver.findElements(
                By.cssSelector(tagName + "[name='" + name + "'], " + tagName + "#" + name));
        if (!named.isEmpty()) {
            return named.get(0);
        }

        By labelLocator = By.xpath("//label[normalize-space(.)='" + escapeXPath(label)
                + "']/following::" + tagName + "[1]");
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(labelLocator));
        } catch (TimeoutException exception) {
            List<String> availableFields = driver.findElements(By.cssSelector("input, select"))
                    .stream()
                    .map(element -> element.getTagName() + "[name="
                            + element.getAttribute("name") + ", id="
                            + element.getAttribute("id") + ", type="
                            + element.getAttribute("type") + "]")
                    .collect(Collectors.toList());
            throw new IllegalStateException("Campo '" + label + "' não encontrado. Campos: "
                    + availableFields, exception);
        }
    }

    private WebElement firstElement(By locator) {
        return wait.until(currentDriver -> {
            List<WebElement> elements = currentDriver.findElements(locator);
            return elements.isEmpty() ? null : elements.get(0);
        });
    }

    private WebElement editableInput(By locator) {
        return wait.until(currentDriver -> currentDriver.findElements(locator).stream()
                .filter(WebElement::isDisplayed)
                .filter(WebElement::isEnabled)
                .filter(element -> {
                    String type = element.getAttribute("type");
                    return !"hidden".equalsIgnoreCase(type)
                            && !"submit".equalsIgnoreCase(type)
                            && !"button".equalsIgnoreCase(type)
                            && !"checkbox".equalsIgnoreCase(type)
                            && !"radio".equalsIgnoreCase(type)
                            && !"true".equalsIgnoreCase(element.getAttribute("readonly"));
                })
                .findFirst()
                .orElse(null));
    }

    private String escapeXPath(String value) {
        if (!value.contains("'")) {
            return value;
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        String[] parts = value.split("'", -1);
        return "concat('" + String.join("', \"'\", '", parts) + "')";
    }
}
