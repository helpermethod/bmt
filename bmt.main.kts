#!/usr/bin/env kotlin

@file:DependsOn("com.microsoft.playwright:playwright:1.35.1")

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.AriaRole.BUTTON
import com.microsoft.playwright.options.AriaRole.LINK
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

Playwright.create().use { playwright ->
    val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500.0))
    val page = browser.newPage()

    val taxibusPage = TaxibusPage(page)

    taxibusPage.navigate()
    taxibusPage.cookieConsent()
    taxibusPage.login()

    taxibusPage.book()

    val bookingPage = BookingPage(page)
    bookingPage.book()
}

class TaxibusPage(private val page: Page) {
    private val cookieConsent =
        page.getByRole(BUTTON, Page.GetByRoleOptions().setName("Alles Akzeptieren"))
    private val username =
        page.locator("input[name='user']")
    private val password =
        page.locator("input[name='pass']")
    private val login =
        page.getByRole(BUTTON, Page.GetByRoleOptions().setName("Anmelden"))
    private val book =
        page.getByRole(LINK, Page.GetByRoleOptions().setName("Weiter zur Buchung"))

    fun navigate() {
        page.navigate("https://www.rvk.de/taxibus-und-ast")
    }

    fun cookieConsent() {
        cookieConsent.click()
    }

    fun login() {
        username.fill(System.getenv("RVK_USERNAME"))
        password.fill(System.getenv("RVK_PASSWORD"))

        login.click()
    }

    fun book() {
        book.click()
    }
}

class BookingPage(page: Page) {
    private val stationStart =
        page.getByLabel("Abfahrtshaltestelle")
    private val stationEnd =
        page.getByLabel("Zielhaltestelle")
    private val date =
        page.getByLabel("Datum")
    private val time =
        page.getByLabel("Uhrzeit")
    private val findConnections =
        page.getByRole(LINK, Page.GetByRoleOptions().setName("Verbindungen suchen"))

    fun book() {
        stationStart.fill("Weilerswist Bf, Weilerswist")
        stationEnd.fill("Lommersum Kirche, Weilerswist")

        val tomorrow = ZonedDateTime.now(ZoneId.of("Europe/Berlin")).plusDays(1)
        date.fill(tomorrow.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))

        time.fill("07:20")

        findConnections.click()
    }
}
