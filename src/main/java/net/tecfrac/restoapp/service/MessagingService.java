package net.tecfrac.restoapp.service;

import com.twilio.Twilio;
import com.twilio.base.ResourceSet;
import com.twilio.rest.conversations.v1.service.Conversation;
import com.twilio.rest.conversations.v1.service.conversation.Participant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.tecfrac.restoapp.config.TwilioConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Setter
@Service
public class MessagingService {
    private static Set<String> badWords = new HashSet<>();
    private final TwilioConfig twilioConfig;
    public static void loadBadWords() {
        try {
            List<String> lines = Files.lines(Paths.get("src/main/resources/badwords.txt"))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();
            badWords.addAll(lines);
            for (String line : lines) {
                if (!line.isBlank()) {
                    badWords.add(line.trim().toLowerCase());
                }
            }
            System.out.println("Loaded " + badWords.size() + " bad words.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String censorMessage(String message) {
        if (message == null || message.isBlank()) return message;

        String[] words = message.split("\\b");
        for (int i = 0; i < words.length; i++) {
                String lowerWord = words[i].toLowerCase();
                if (badWords.contains(lowerWord)) {
                    char[] stars = new char[words[i].length()];
                    Arrays.fill(stars, '*');
                    words[i] = new String(stars);
            }
        }
        return String.join("", words);
    }
    @Scheduled(fixedRate = 43200000)
    public void deleteEmptyConversations() {
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
        var conversations = Conversation.reader(twilioConfig.getServiceSid()).read();


        for (Conversation convo : conversations) {
            ResourceSet<Participant> participants = Participant.reader(twilioConfig.getServiceSid(), convo.getSid()).read();

            if (!participants.iterator().hasNext()) {
                Conversation.deleter(twilioConfig.getServiceSid(), convo.getSid()).delete();
                System.out.println("Deleted empty participants conversation: " + convo.getSid());
            }
        }
    }


}
