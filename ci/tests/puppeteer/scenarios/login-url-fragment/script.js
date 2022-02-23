const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    let service = "https://apereo.github.io#hello"
    await page.goto(`https://localhost:8443/cas/login?service=${service}`);
    await cas.loginWith(page, "casuser", "Mellon");
    let url = await page.url()
    await page.waitForTimeout(2000)
    await cas.assertTicketParameter(page);
    assert((url.match(/#/g) || []).length === 1)
    let result = new URL(page.url());
    assert(result.hash === "hello-world");
    await browser.close();
})();
