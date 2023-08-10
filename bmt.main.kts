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
import kotlin.io.path.Path

Playwright.create().use { playwright ->
    val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false).setSlowMo(500.0))
    val page = browser.newPage()

    val taxibusPage = TaxibusPage(page)

    taxibusPage.navigate()
    taxibusPage.cookieConsent()
    taxibusPage.login()

    taxibusPage.book()

    val bookingPage = BookingPage(page)
    bookingPage.book("Weilerswist Bf, Weilerswist", "Lommersum Kirche, Weilerswist", "07:20")

    val detailsPage = DetailsPage(page)
    detailsPage.fillInDetails()

    val summaryPage = SummaryPage(page)
    //summaryPage.bookNow()

    page.screenshot(Page.ScreenshotOptions().setPath(Path("summary.png")))
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
    private val `continue` =
        page.getByRole(BUTTON, Page.GetByRoleOptions().setName("weiter"))

    fun book(start: String, end: String, departure: String) {
        stationStart.fill(start)
        stationEnd.fill(end)

        val tomorrow = ZonedDateTime.now(ZoneId.of("Europe/Berlin")).plusDays(1)
        date.fill(tomorrow.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))

        time.fill(departure)

        findConnections.click()

        // TODO make sure that the correct connection is selected

        `continue`.click()
    }
}

class DetailsPage(page: Page) {
    private val ageGroup =
        page.locator("select.age")
    private val `continue` =
        page.getByRole(BUTTON, Page.GetByRoleOptions().setName("weiter"))

    fun fillInDetails() {
        ageGroup.selectOption("Kind (6-14 Jahren)")

        `continue`.click()
    }
}

class SummaryPage(page: Page) {
    private val bookNow =
        page.getByRole(BUTTON, Page.GetByRoleOptions().setName("Jetzt buchen"))

    fun bookNow() {
        bookNow.click()
    }
}

