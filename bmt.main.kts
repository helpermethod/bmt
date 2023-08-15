#!/usr/bin/env kotlin

@file:DependsOn("com.microsoft.playwright:playwright:1.35.1")

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.AriaRole
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path

Playwright.create().use { playwright ->
    val browser = playwright.chromium().launch()

    browser.newContext().use(::bookOutBoundTrip)
    browser.newContext().use(::bookReturnTrip)
}

fun bookOutBoundTrip(context: BrowserContext) {
    val page = context.newPage()

    book(
        page,
        StationStart("Weilerswist Bf, Weilerswist"),
        StationEnd("Lommersum Kirche, Weilerswist"),
        Departure("07:20")
    )

    page.screenshot(Page.ScreenshotOptions().apply {
        path = Path("outbound.png")
        fullPage = true
    })
}

fun bookReturnTrip(context: BrowserContext) {
    val page = context.newPage()

    book(
        page,
        StationStart("Lommersum Kirche, Weilerswist"),
        StationEnd("Weilerswist Bf, Weilerswist"),
        Departure("14:47")
    )

    page.screenshot(Page.ScreenshotOptions().apply {
        path = Path("return.png")
        fullPage = true
    })
}

fun book(page: Page, stationStart: StationStart, stationEnd: StationEnd, departure: Departure) {
    TaxibusPage(page)
        .navigate()
        .cookieConsent()
        .login()
        .startBooking()
        .book(stationStart, stationEnd, departure)
        .selectAgeGroup()
        //.bookNow()
}

class TaxibusPage(private val page: Page) {
    private val cookieConsent = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Alles Akzeptieren"))
    private val username = page.locator("input[name='user']")
    private val password = page.locator("input[name='pass']")
    private val login = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Anmelden"))
    private val book = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Weiter zur Buchung"))

    fun navigate() = apply {
        page.navigate("https://www.rvk.de/taxibus-und-ast")
    }

    fun cookieConsent() = apply {
        cookieConsent.click()
    }

    fun login() = apply {
        username.fill(System.getenv("RVK_USERNAME"))
        password.fill(System.getenv("RVK_PASSWORD"))

        login.click()
    }

    fun startBooking(): BookingPage {
        book.click()

        return BookingPage(page)
    }
}

class BookingPage(private val page: Page) {
    private val stationStart = page.getByLabel("Abfahrtshaltestelle")
    private val stationEnd = page.getByLabel("Zielhaltestelle")
    private val date = page.getByLabel("Datum")
    private val time = page.getByLabel("Uhrzeit")
    private val findConnections = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Verbindungen suchen"))
    private val `continue` = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("weiter"))

    fun book(start: StationStart, end: StationEnd, departure: Departure): DetailsPage {
        stationStart.fill(start.stationStart)
        stationEnd.fill(end.stationEnd)

        val tomorrow = ZonedDateTime.now(ZoneId.of("Europe/Berlin")).plusDays(1)
        date.fill(tomorrow.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))

        time.fill(departure.departure)

        findConnections.click()

        `continue`.click()

        return DetailsPage(page)
    }
}

class DetailsPage(private val page: Page) {
    private val ageGroup = page.locator("select.age")
    private val `continue` = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("weiter"))

    fun selectAgeGroup(): SummaryPage {
        ageGroup.selectOption("Kind (6-14 Jahren)")

        `continue`.click()

        return SummaryPage(page)
    }
}

class SummaryPage(page: Page) {
    private val bookNow = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Jetzt buchen"))

    fun bookNow() {
        bookNow.click()
    }
}

@JvmInline
value class StationStart(val stationStart: String)

@JvmInline
value class StationEnd(val stationEnd: String)

@JvmInline
value class Departure(val departure: String)
