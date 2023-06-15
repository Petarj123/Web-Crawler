package com.mtc.crawler.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class UserAgentGenerator {
    private static final String[] AGENT_PREFIXES = {
            "Mozilla/5.0 (Windows NT ",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X ",
            "Mozilla/5.0 (X11; Linux x86_64) ",
            "Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Device/XYZ123) "
    };

    private static final String[] AGENT_SUFFIXES = {
            " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36",
            " AppleWebKit/537.36 (KHTML, like Gecko) Firefox/93.0",
            " AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36 Edg/94.0.992.50",
            " AppleWebKit/537.36 (KHTML, like Gecko) Mobile Safari/537.36"
    };
    private final Random random = new Random();
    public String generateRandomUserAgent() {
        List<String> userAgents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String prefix = AGENT_PREFIXES[random.nextInt(AGENT_PREFIXES.length)];
            String suffix = AGENT_SUFFIXES[random.nextInt(AGENT_SUFFIXES.length)];
            String userAgent = prefix + getRandomVersion() + suffix;
            userAgents.add(userAgent);
        }
        int randomIndex = random.nextInt(userAgents.size());
        return userAgents.get(randomIndex);
    }
    private String getRandomVersion() {
        int majorVersion = random.nextInt(20) + 1;
        int minorVersion = random.nextInt(10);
        return majorVersion + "." + minorVersion;
    }
}
