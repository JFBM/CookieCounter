package io.github.jfbm.cookiecounter.tasks;

import com.google.gson.JsonParser;
import com.sun.jna.platform.win32.Crypt32Util;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Slf4j
public abstract class CookieTask {
    protected WebDriver driver;
    private Path chromeUserData;

    public CookieTask(WebDriver driver, Path chromeUserData) {
        this.driver = driver;
        this.chromeUserData = chromeUserData;
    }

    public void doTask() {
        doWork(driver);
    }

    @SneakyThrows
    protected List<List<String>> loadCookiesFromDB() {
        String connectionString = "jdbc:sqlite:" + chromeUserData.resolve(Path.of("Default", "Network", "Cookies"));
        try (var conn = DriverManager.getConnection(connectionString)) {
            var query = "SELECT * FROM cookies";
            var result = conn.createStatement().executeQuery(query);

            var columnCount = result.getMetaData().getColumnCount();

            List<List<String>> results = new ArrayList<>();

            while (result.next()) {
                List<String> fields = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    fields.add(result.getString(i));
                }
                log.info(String.join(";", fields));
                results.add(fields);
            }
            return results;
        }
    }

    /**
     * https://stackoverflow.com/a/65953409
     *
     * @return The master key for cookie decryption
     */
    @SneakyThrows
    private byte[] loadMasterKey() {
        var localStatePath = chromeUserData.resolve("Local State");
        var localState = JsonParser.parseString(Files.readString(localStatePath)).getAsJsonObject();
        var encryptedMasterKeyWithPrefixB64 = localState.get("os_crypt").getAsJsonObject().get("encrypted_key").getAsString();

        byte[] encryptedMasterKeyWithPrefix = Base64.getDecoder().decode(encryptedMasterKeyWithPrefixB64);
        byte[] encryptedMasterKey = Arrays.copyOfRange(encryptedMasterKeyWithPrefix, 5, encryptedMasterKeyWithPrefix.length);

        byte[] masterKey = Crypt32Util.cryptUnprotectData(encryptedMasterKey);
        return masterKey;
    }

    protected abstract void doWork(WebDriver webDriver);
}

