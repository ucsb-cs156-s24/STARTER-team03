package edu.ucsb.cs156.example.web;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator.PressSequentiallyOptions;
import com.microsoft.playwright.Page;

import com.microsoft.playwright.Playwright;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import edu.ucsb.cs156.example.services.wiremock.WiremockServiceImpl;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("integration")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class ITUCSBDate {
    @LocalServerPort
    private int port;

    private Browser browser;
    private Page page;

    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void setupWireMock() {
        wireMockServer = new WireMockServer(options()
            .port(8090)
            .extensions(new ResponseTemplateTransformer(true)));

        wireMockServer.start();
    }
    
    public void setupAdmin() {
        WiremockServiceImpl.setupOauthMocks(wireMockServer, true);
        
        // Launch playwright browser headless
        // browser = Playwright.create().chromium().launch();

        // Launch playwright browser with visual
        browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

        BrowserContext context = browser.newContext();
        page = context.newPage();

        String url = String.format("http://localhost:%d/oauth2/authorization/my-oauth-provider", port);
        page.navigate(url);

        page.locator("#username").fill("admingaucho@ucsb.edu");
        page.locator("#password").fill("password");
        page.locator("#submit").click();

        assertThat(page.getByText("Log Out")).isVisible();
        assertThat(page.getByText("Welcome, admingaucho@ucsb.edu")).isVisible();

        url = String.format("http://localhost:%d/", port);
        page.navigate(url);
    }

    public void setupRegularUser() {
        WiremockServiceImpl.setupOauthMocks(wireMockServer, false);
        
        // Launch playwright browser headless
        // browser = Playwright.create().chromium().launch();

        // Launch playwright browser with visual
        browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

        BrowserContext context = browser.newContext();
        page = context.newPage();

        String url = String.format("http://localhost:%d/oauth2/authorization/my-oauth-provider", port);
        page.navigate(url);

        page.locator("#username").fill("cgaucho@ucsb.edu");
        page.locator("#password").fill("password");
        page.locator("#submit").click();

        assertThat(page.getByText("Log Out")).isVisible();
        assertThat(page.getByText("Welcome, cgaucho@ucsb.edu")).isVisible();

        url = String.format("http://localhost:%d/", port);
        page.navigate(url);
    }

    @AfterEach
    public void teardown() {
        browser.close();
    }

    @AfterAll
    public static void teardownWiremock() {
        wireMockServer.stop();
    }

    // @Test
    // public void admin_user_can_create_edit_delete_date() throws Exception {
    //     setupAdmin();

    //     page.getByText("UCSB Dates").click();

    //     page.getByText("Create UCSBDate").click();
    //     assertThat(page.getByText("Create New UCSBDate")).isVisible();
    //     page.getByTestId("UCSBDateForm-quarterYYYYQ").fill("20241");
    //     page.getByTestId("UCSBDateForm-localDateTime").pressSequentially("021920240000", new PressSequentiallyOptions().setDelay(200));
    //     page.getByTestId("UCSBDateForm-name").fill("Presidents' Day");
    //     page.getByTestId("UCSBDateForm-submit").click();

    //     assertThat(page.getByTestId("UCSBDateTable-cell-row-0-col-name")).hasText("Presidents' Day");

    //     page.getByTestId("UCSBDateTable-cell-row-0-col-Edit-button").click();
    //     assertThat(page.getByText("Edit UCSBDate")).isVisible();
    //     page.getByTestId("UCSBDateForm-name").fill("Presidents' Day 2024");
    //     page.getByTestId("UCSBDateForm-submit").click();

    //     assertThat(page.getByTestId("UCSBDateTable-cell-row-0-col-name")).hasText("Presidents' Day 2024");

    //     page.getByTestId("UCSBDateTable-cell-row-0-col-Delete-button").click();
        
    //     assertThat(page.getByTestId("UCSBDateTable-cell-row-0-col-name")).not().isVisible();
    // }

    @Test
    public void regular_user_cannot_create_date() throws Exception {
        setupRegularUser();

        page.getByText("UCSB Dates").click();

        assertThat(page.getByText("Create UCSBDate")).not().isVisible();
        assertThat(page.getByTestId("UCSBDateTable-cell-row-0-col-name")).not().isVisible();
    }
}
