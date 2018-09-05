package cn.insightachieve.tomoko.robots;

import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A base class for common UI operations.
 */
public abstract class BaseUIRobot {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BaseUIRobot.class);

    @Autowired
    protected WebDriver webDriver;
    @Autowired
    protected Actions actions;

    public void waitUtil(long timeOutInSeconds, BooleanSupplier s) {
        (new WebDriverWait(webDriver, timeOutInSeconds)).until((WebDriver d) -> {
            try {
                return s.getAsBoolean();
            } catch (NoSuchElementException e) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Element not found!", e);
                }
                return false;
            }
        });
    }

    public void waitIn(long timeOutInSeconds) {
        try {
            Thread.sleep(timeOutInSeconds * 1000);
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        }
    }

    public void waitLogin(long timeOutInSeconds) {
        waitUtil(timeOutInSeconds, () -> "https://wx2.qq.com/".equals(webDriver.getCurrentUrl()));
    }

    public void pageDown() {
        actions.sendKeys(Keys.PAGE_DOWN).build().perform();
    }

    public void pageUp() {
        actions.sendKeys(Keys.PAGE_UP).build().perform();
    }

    public void end() {
        actions.sendKeys(Keys.END).build().perform();
    }

    public void home() {
        actions.sendKeys(Keys.HOME).build().perform();
    }

    public WebElement tryFind(Supplier<WebElement> s) {
        try {
            return s.get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void input(WebElement we, String value) {
        we.sendKeys(Keys.chord(Keys.CONTROL, "a"), value);
    }
}
