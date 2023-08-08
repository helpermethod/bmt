#!/usr/bin/env kotlin

@file:DependsOn("com.microsoft.playwright:playwright:1.35.1")

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import kotlin.io.path.Path

Playwright.create().use { playwright ->
    val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500.0))
    val page = browser.newPage()
    page.navigate("https://www.rvk.de/login")

    assertThat(page).hasTitle("Login")

    page
        .getByRole(AriaRole.BUTTON)
        .getByText("Alles akzeptieren")
        .click()

    page.screenshot(Page.ScreenshotOptions().setPath(Path("login.png")))
}
