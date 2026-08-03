#!/usr/bin/env kotlin

@file:DependsOn("com.microsoft.playwright:playwright:1.62.0")

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.AriaRole
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val tomorrow: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin")).plusDays(1L)
val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

Playwright.create().use { playwright ->
    val browser = playwright.chromium().launch()

    browser.newContext().use(::bookReturnTrip)
}

fun bookReturnTrip(context: BrowserContext) {
    bookTicket(
        context.newPage(),
        StationStart("Lommersum Grundschule, Weilerswist"),
        StationEnd("Weilerswist Bf, Weilerswist"),
        selectDeparture()
    )
}

fun selectDeparture() =
    if (tomorrow.dayOfWeek == DayOfWeek.FRIDAY) Departure("13:47") else Departure("14:47")

fun bookTicket(page: Page, stationStart: StationStart, stationEnd: StationEnd, departure: Departure) {
    openLoginPage(page)
        .cookieConsent()
        .login()
        .openBookingPage()
        .book(stationStart, stationEnd, departure)
        .selectConnection(departure)
        .selectAgeGroup()
        .bookNow()
}

class LoginPage(private val page: Page) {
    private val cookieConsent = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Alles Akzeptieren"))
    private val username = page.locator("input[name='user']")
    private val password = page.locator("input[name='pass']")
    private val login = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Anmelden"))
    private val book = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Weiter zur Buchung"))

    fun cookieConsent() = apply {
        cookieConsent.click()
    }

    fun login() = apply {
        username.fill(System.getenv("RVK_USERNAME"))
        password.fill(System.getenv("RVK_PASSWORD"))

        login.click()
    }

    fun openBookingPage(): BookingPage {
        page.waitForResponse("**/tx_taxibus.js*") {
            page.navigate("https://www.rvk.de/meinefahrt/einzelauftrag")
        }

        return BookingPage(page)
    }
}

fun openLoginPage(page: Page): LoginPage {
    page.navigate("https://www.rvk.de/login")

    return LoginPage(page)
}

class BookingPage(private val page: Page) {
    private val stationStart = page.getByLabel("Abfahrtshaltestelle")
    private val stationEnd = page.getByLabel("Zielhaltestelle")
    private val date = page.getByLabel("Datum")
    private val time = page.getByLabel("Uhrzeit")
    private val findConnections = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Verbindungen suchen"))
    private val `continue` = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("weiter"))

    fun book(start: StationStart, end: StationEnd, departure: Departure): ConnectionsPage {
        stationStart.fill(start.value)
        stationEnd.fill(end.value)

        date.fill(formatter.format(tomorrow))
        time.fill(departure.value)

        findConnections.click()

        return ConnectionsPage(page)
    }
}

class ConnectionsPage(private val page: Page) {
    private val `continue` = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("weiter"))

    fun selectConnection(departure: Departure): DetailsPage {
        page
            .getByRole(AriaRole.LINK)
            .filter(Locator.FilterOptions().setHasText("ab ${departure.value}"))
            .click()

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
value class StationStart(val value: String)

@JvmInline
value class StationEnd(val value: String)

@JvmInline
value class Departure(val value: String)
