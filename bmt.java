///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 20
//DEPS com.microsoft.playwright:playwright:1.35.1

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.BrowserType;

public class bmt {
    public static void main(String... args) {
        try (var playwright = Playwright.create()) {
            var browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            var page = browser.newPage();
            page.navigate("https://www.rvk.de/login");
        }
    }
}
