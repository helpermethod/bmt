#!/usr/bin/env kotlin

@file:DependsOn("com.microsoft.playwright:playwright:1.35.1")

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole.BUTTON
import kotlin.io.path.Path

Playwright.create().use { playwright ->
    val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500.0))
    val page = browser.newPage()

    page.navigate("https://www.rvk.de/login")

    assertThat(page).hasTitle("Login")

    page
        .getByRole(BUTTON, Page.GetByRoleOptions().setName("Anmelden"))
        .click()

    page
        .locator("input[name='user']")
        .fill(System.getenv("RVK_USERNAME"))

    page
        .locator("input[name='pass']")
        .fill(System.getenv("RVK_PASSWORD"))

    page
        .getByRole(BUTTON, )
        .click()

    page.screenshot(Page.ScreenshotOptions().setPath(Path("fail.png")))
}
