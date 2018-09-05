package cn.insightachieve.tomoko.robots;

import cn.insightachieve.tomoko.models.Message;
import cn.insightachieve.tomoko.jobs.JobScheduler;
import cn.insightachieve.tomoko.models.DataCM;
import cn.insightachieve.tomoko.services.IMessageHandler;
import com.google.gson.Gson;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MessageCollector extends BaseUIRobot {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MessageCollector.class);

    private static final String XPATH_TAB_CONTACT = "/html/body/div/div/div/div[@class=\"tab\"]/div/a[@title=\"通讯录\"]";
    private static final String XPATH_ITEM_CONTACT =
            "//*[@id=\"navContact\"]/div/div[@ng-repeat=\"item in allContacts\"]//div[@class=\"contact_item \"]";

    @Autowired
    private JobScheduler jobScheduler;
    @Autowired
    private IMessageHandler messageLogger;

    private ScheduledExecutorService gmcSes = Executors.newSingleThreadScheduledExecutor();
    private Map<String, String> groupMap = new TreeMap<>();
    /**
     * The map to store the last collected message id for a specific group.
     */
    private Map<String, String> lastMsgIdMap = new TreeMap<>();
    /**
     * The map to store collected message id set for a specific group.
     */
    private Map<String, Set<String>> msgIdSetMap = new TreeMap<>();
    private AtomicInteger collectionCount = new AtomicInteger(1);
    private Gson gson = new Gson();

    @PostConstruct
    public void init() {
        LOGGER.info("MessageCollector start.");

        // Wait login success.
        try {
            jobScheduler.scheduleFJob(() -> waitLogin(3600)).get();
            LOGGER.info("WeChat login success.");
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("WeChat login failed!", e);
            return;
        }

        // Get group names and fill some meta-data.
        jobScheduler.scheduleJob(() -> {
            try {
                LOGGER.info("Group meta-data updating....");
                updateGroupMetaData();
                LOGGER.info("Group meta-data updated.");
            } catch (RuntimeException e) {
                LOGGER.error("Group meta-data update failed!", e);
            }

            for (String groupName : groupMap.keySet()) {
                this.lastMsgIdMap.put(groupName, "");
                this.msgIdSetMap.put(groupName, new HashSet<>());
            }
        });

        // Start a thread to do routine job for fetching messages from each groups.
        gmcSes.scheduleAtFixedRate(() -> jobScheduler.scheduleJob(() -> {
            int cnt = collectionCount.getAndIncrement();
            LOGGER.info("Collection[count={}] begin.", cnt);

            for (String groupName : groupMap.keySet()) {
                try {
                    LOGGER.info("Group[{}] collecting....", groupName);
                    //Get messages
                    fetchMessages(groupName);
                    waitIn(2);
                    LOGGER.info("Group[{}] collected.", groupName);
                } catch (RuntimeException e) {
                    LOGGER.error("Group[" + groupName + "] collect failed!", e);
                }
            }
            LOGGER.info("Collection[count={}] end.", cnt);
        }), 0, 10, TimeUnit.MINUTES);
        LOGGER.info("MessageCollector started.");
    }

    private void fetchMessages(String groupName) {
        // Open the chat area.
        WebElement searchInput = webDriver.findElement(By.xpath("//*[@id=\"search_bar\"]/input"));
        input(searchInput, groupName);
        waitIn(2);
        searchInput.sendKeys(Keys.ENTER);

        // Wait for chat area load completion.
        waitIn(5);
        waitUtil(5, () -> groupName.trim().equals(webDriver.findElement(
                By.xpath("//div[@id=\"chatArea\"]/div[@class=\"box_hd\"]/div/div/a[@data-username]")).getText().trim()));

        // Scroll to the end of the message area.
        List<WebElement> weList = webDriver.findElements(
                By.xpath("//div[@id=\"chatArea\"]//div[@ng-repeat=\"message in chatContent\"]"));
        WebElement scrollItem = null;
        if (weList.isEmpty()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("There is no message in group[{}].", groupName);
            }
            return;
        }

        scrollItem = weList.get(0);
        actions.moveToElement(scrollItem);
        end();
        waitIn(1);

        String lastMsgId = this.lastMsgIdMap.get(groupName);
        boolean completed = false;
        while (!completed) {
            List<WebElement> msgWeList = webDriver.findElements(
                    By.xpath("//div[@id=\"chatArea\"]/div/div/div/div[@ng-repeat=\"message in chatContent\"]"));
            //Reverse the list.
            Collections.reverse(msgWeList);

            boolean allRead = true, stopped = false;
            for (WebElement msgWe : msgWeList) {
                WebElement contentWe = msgWe.findElement(By.xpath("div/div/div/div[@class=\"content\"]"));

                WebElement dataCmWe = contentWe.findElement(By.xpath("div[@data-cm]"));
                String dcmJson = dataCmWe.getAttribute("data-cm");
                DataCM dataCM = gson.fromJson(dcmJson, DataCM.class);

                String msgId = dataCM.getMsgId();
                if (lastMsgId.equals(msgId)) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Last msgId: '{}' found.", msgId);
                    }
                    stopped = true;
                    break;
                } else if (this.msgIdSetMap.get(groupName).contains(msgId)) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Skip msgId: '{}'.", msgId);
                    }
                    continue;
                } else {
                    allRead = false;
                    Message message = new Message();
                    message.setGroup(groupName);
                    message.setContent(dataCmWe.getText());
                    message.setMsgId(msgId);
                    message.setSender(dataCM.getActualSender());
                    message.setSenderName(contentWe.findElement(By.xpath("h4[1]")).getText());

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("From group[{}], message: '{}'.", groupName, message);
                    }
                    messageLogger.handle(message);

                    this.lastMsgIdMap.put(groupName, msgId);
                    this.msgIdSetMap.get(groupName).add(msgId);
                }
            }

            if (stopped) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Finish fetching from group[{}], the last msgId found.", groupName);
                }
                break;
            } else if (allRead) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Finish fetching from group[{}], there is no more new message.", groupName);
                }
                break;
            }

            // Scroll up.
            actions.moveToElement(scrollItem);
            pageUp();
            waitIn(1);
        }
    }

    private void updateGroupMetaData() {
        waitUtil(5, () -> webDriver.findElement(By.xpath(XPATH_TAB_CONTACT)) != null);
        WebElement contact = webDriver.findElement(By.xpath(XPATH_TAB_CONTACT));
        contact.click();
        waitIn(5);

        List<WebElement> list = webDriver.findElements(By.xpath(XPATH_ITEM_CONTACT));
        if (!list.isEmpty()) {
            actions.click(list.get(0));
            end();
            waitIn(2);
        }

        list = webDriver.findElements(By.xpath(XPATH_ITEM_CONTACT));
        if (!list.isEmpty()) {
            actions.click(list.get(0));
            home();
            waitIn(2);
        }

        Map<String, String> map = new TreeMap<>();
        boolean completed = false;
        while (!completed) {
            List<WebElement> weList = webDriver.findElements(By.xpath(XPATH_ITEM_CONTACT));

            WebElement guard = null;
            for (WebElement we : weList) {
                String name = we.getText();
                if (map.containsKey(name)) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Skip group '{}[{}]'.", name, map.get(name));
                    }
                    continue;
                }

                String uid = null;
                String src = we.findElement(By.xpath("div[@class=\"avatar\"]/img")).getAttribute("src");
                String[] parts = src.split("\\?");
                if (parts.length == 2) {
                    String[] paras = parts[1].split("&");
                    for (String para : paras) {
                        String[] pair = para.split("=");
                        if (pair.length == 2 && "username".equals(pair[0])) {
                            uid = pair[1];
                            break;
                        }
                    }
                }

                if (uid == null) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("UID not found for '{}'.", name);
                    }
                    continue;
                } else if (!uid.startsWith("@@")) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Detect '{}'[{}] is not a group.", name, uid);
                    }
                    completed = true;
                    break;
                }

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Add group '{}'[{}].", name, uid);
                }
                map.put(name, uid);
                guard = we;
            }

            if (guard != null) {
                actions.click(guard);
                pageDown();
                waitIn(2);
            } else {
                LOGGER.warn("Scroll-down missing!");
            }
        }

        this.groupMap = map;
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Get {} groups: '{}'", this.groupMap.size(), this.groupMap);
        }
    }
}
