package io.github.jfbm.cookiecounter;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

@SpringBootApplication
@Slf4j
public class CookieCounterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookieCounterApplication.class, args);
    }

    @Bean
    public Path chromeDataPath() throws IOException {
        var path = Files.createTempDirectory("CookieCounter");
        log.info("Chrome-UserData: {}", path.toString());
        return path;
    }

    @Bean
    public Path chromeCookies(Path chromeDataPath) {
        return chromeDataPath.resolve(Path.of("Default", "Network", "Cookies"));
    }

    @Bean
    public Path runSaveDir() {
        var path = Path.of("cookie_dbs");

        if (Files.notExists(path)) {
            path.toFile().mkdirs();
        }

        return path;
    }

    @Bean
    public CommandLineRunner commandLineRunner(Path chromeCookies, Path runSaveDir, WebDriver driver) {
        return args -> {

            Scanner sc = new Scanner(System.in);
            System.out.println("Enter Run-Title:");
            String runTitle = sc.nextLine();

            System.out.println("Press Enter to end program");
            System.in.read();

            driver.close();
            Files.copy(chromeCookies, runSaveDir.resolve(runTitle));
        };
    }

    @Bean
    public WebDriver webDriver(Path chromeDataPath) {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--user-data-dir=" + chromeDataPath.toString());
        chromeOptions.addArguments("--start-maximized");

        return WebDriverManager
                .chromedriver()
                .cachePath("./.webdrivercache")
                .capabilities(chromeOptions)
                .create();
    }
}
