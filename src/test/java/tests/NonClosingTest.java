package tests;

import com.browserstack.config.BrowserStackConfig;
import com.microsoft.playwright.*;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.jupiter.api.*;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NonClosingTest {
    public static String userName, accessKey;
    public static Map<String, Object> browserStackYamlMap;
    public static final String USER_DIR = "user.dir";

    static Playwright playwright;
    static Browser browser;

//    BrowserContext context;
    public static Page page;

    public NonClosingTest() {
        File file = new File(getUserDir() + "/browserstack.yml");
        browserStackYamlMap = convertYamlFileToMap(file, new HashMap<>());
    }

    @BeforeEach
    void launchBrowser(TestInfo testInfo) {
        String displayName = testInfo.getDisplayName();
        String methodName = "";
        if(testInfo.getTestMethod().isPresent()){
            methodName = testInfo.getTestMethod().get().getName();
        } else {
            methodName = displayName;
        }

        System.out.println("BeforeEach for test: " + displayName);
        System.out.println("Method name: " + methodName);
        if(browser == null)
        {
            playwright = Playwright.create();
            BrowserType browserType = playwright.chromium();

            boolean runOnBStack = Boolean.parseBoolean(System.getProperty("run-on-bstack", "false"));
            System.out.println("runOnBStack: " + runOnBStack);

            if(!runOnBStack){
                System.out.println("Launching Browser on Local Machine... Thread: " +
                        Thread.currentThread().getName() + " => " + displayName);

                browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
            } else {
                System.out.println("Launching Browser on BrowserStack... Thread: " +
                        Thread.currentThread().getName() + " => " + displayName);

                String caps = null;
                userName = System.getenv("BROWSERSTACK_USERNAME") !=
                        null ? System.getenv("BROWSERSTACK_USERNAME") :
                        (String) browserStackYamlMap.get("userName");

                accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY") !=
                        null ? System.getenv("BROWSERSTACK_ACCESS_KEY") :
                        (String) browserStackYamlMap.get("accessKey");

                HashMap<String, String> capabilitiesObject = new HashMap<>();
                capabilitiesObject.put("browserstack.user", userName);
                capabilitiesObject.put("browserstack.key", accessKey);
                capabilitiesObject.put("browserstack.source",
                        "java-playwright-browserstack:sample-sdk:v1.0");

                capabilitiesObject.put("projectName", "BrowserStack Samples NonClosing");
                capabilitiesObject.put("buildName", "browserstack build nonclosing");

                capabilitiesObject.put("browser", "chrome");

                JSONObject jsonCaps = new JSONObject(capabilitiesObject);
                try {
                    caps = URLEncoder.encode(jsonCaps.toString(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                String wsEndpoint = "wss://cdp.browserstack.com/playwright?caps=" + caps;
                browser = browserType.connect(wsEndpoint,
                        new BrowserType.ConnectOptions().setTimeout(60000));


            }

            if(page == null) {
                page = browser.newPage();
            }

        }
    }

    @Test
    void bstackSampleTest1() {
        try {
            page.navigate("https://www.bstackdemo.com/");

            String product_name = page.locator("//*[@id='1']/p").textContent();
            page.locator("//*[@id='1']/div[4]").click();
            page.locator(".float\\-cart__content");
            String product_in_cart =
                    page.locator("//*[@id='__next']/div/div/div[2]/div[2]/div[2]/div/div[3]/p[1]").nth(1)
                    .textContent();

            assertEquals(product_in_cart, product_name);

            markTestStatus("passed", "Title matched", page);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    @Test
    void bstackOtherDeviceTest() {
        try {
            page.navigate("https://www.bstackdemo.com/");

            String product_name = page.locator("//*[@id='5']/p").textContent();
            page.locator("//*[@id='5']/div[4]").click();
            page.locator(".float\\-cart__content");
            String product_in_cart =
                    page.locator("//*[@id='__next']/div/div/div[2]/div[2]/div[2]/div/div[3]/p[1]")
                    .textContent();

            assertEquals(product_in_cart, product_name);

            markTestStatus("passed", "Title matched", page);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());

        }
    }

    @Test
    void bstackMainPageTest() {
        try {
            page.navigate("https://www.browserstack.com/");

            String product_desc = page.locator("#product-text-section > h1").textContent();
            assertEquals("Comprehensive Test Stack", product_desc);

            markTestStatus("passed", "Heading matched", page);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

//    @AfterEach
    @AfterAll
    public static void closeContext() {
//        page.close();
//        browser.close();
//        playwright.close();

        if(page != null && !page.isClosed()){
            System.out.println("Closing page... Thread: " + Thread.currentThread().getName());
            page.close();
        }

        if(browser != null){
            System.out.println("Closing browser... Thread: " + Thread.currentThread().getName());
            browser.close();
        }

        if(playwright != null){
            System.out.println("Closing playwright... Thread: " + Thread.currentThread().getName());
            playwright.close();
        }
    }

    private String getUserDir() {
        return System.getProperty(USER_DIR);
    }

    private Map<String, Object> convertYamlFileToMap(File yamlFile, Map<String, Object> map) {
        try {
            InputStream inputStream = Files.newInputStream(yamlFile.toPath());
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(inputStream);
            map.putAll(config);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Malformed browserstack.yml file - %s.", e));
        }
        return map;
    }

    public static void markTestStatus(String status, String reason, Page page) {
        Object result;
        result = page.evaluate("_ => {}", "browserstack_executor: { \"action\": \"setSessionStatus\", \"arguments\": { \"status\": \"" + status + "\", \"reason\": \"" + reason + "\"}}");
    }
}
