package io.github.jfbm.cookiecounter.tasks;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;

@Service
@Slf4j
public class GoogleTask extends CookieTask {

    public GoogleTask(WebDriver webDriver, Path chromeDataPath) {
        super(webDriver, chromeDataPath);
    }

    private static final String url = "https://www.google.de";

    private static final String acceptAllButtonId = "L2AGLb";
    private static final String cookieModal = "xe7COe";

    @Override
    protected void doWork(WebDriver driver) {
        log.info("Startig pageload of \"{}\"", url);
        driver.get(url);
        loadCookiesFromDB();

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d.findElement(By.id(acceptAllButtonId)));

        log.info("Accepting all cookies on \"{}\"", url);
        driver.findElement(By.id(acceptAllButtonId)).click();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !d.findElement(By.id(cookieModal)).isDisplayed());

        log.info("Google task finished work");
        loadCookiesFromDB();
    }
}
