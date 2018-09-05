package cn.insightachieve.tomoko;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Application {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private final WebDriver webDriver = new ChromeDriver();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public WebDriver webDriver() {
        return webDriver;
    }

    @Bean
    public Actions actions() {
        return new Actions(webDriver);
    }

    @PostConstruct
    public void start() {
        webDriver.get("https://wx.qq.com");
    }

}
