import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.model.DiscussMessage;
import com.scienjus.smartqq.model.GroupMessage;
import com.scienjus.smartqq.model.Message;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KoebutaMessageCallback implements MessageCallback {
    private Map<String, String> autoReplies;
    private HashMap<String, String> autoRepliesFullMatch;

    public KoebutaMessageCallback() {
        reloadAutoReplies();

        autoRepliesFullMatch = new HashMap<>();
        autoRepliesFullMatch.put("ping", "pong");
    }

    public Map<String, String> reloadAutoReplies() {
        autoReplies = new HashMap<>();
        Path autoRepliesFile = FileSystems.getDefault().getPath(System.getProperty("replies", "replies.json"));
        if (autoRepliesFile.toFile().exists()) {
            try {
                autoReplies = JSON.parseObject(String.join("", Files.readAllLines(autoRepliesFile)),
                        new TypeReference<Map<String, String>>() {
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return autoReplies;
    }


    private List<String> shouldAutoReply(String messageContent) {
        if (autoRepliesFullMatch.containsKey(messageContent)) {
            return Collections.singletonList(autoRepliesFullMatch.get(messageContent));
        }
        return autoReplies.keySet().stream().filter(messageContent::contains).map(autoReplies::get).collect(Collectors.toList());
    }

    public void onMessage(Message message) {
        if (message.getContent().equals("GET_ID")) {
            KoebutaApplication.getInstance().getClient().sendMessageToFriend(message.getUserId(), String.valueOf(message.getUserId()));
        }
        for (String reply : shouldAutoReply(message.getContent())) {
            KoebutaApplication.getInstance().getClient().sendMessageToFriend(message.getUserId(), reply);
        }
    }

    public void onGroupMessage(GroupMessage groupMessage) {
        if (groupMessage.getContent().equals("GET_ID")) {
            KoebutaApplication.getInstance().getClient().sendMessageToGroup(groupMessage.getGroupId(), String.valueOf(groupMessage.getGroupId()));
        }
        for (String reply : shouldAutoReply(groupMessage.getContent())) {
            KoebutaApplication.getInstance().getClient().sendMessageToGroup(groupMessage.getGroupId(), reply);
        }
    }

    public void onDiscussMessage(DiscussMessage discussMessage) {
        if (discussMessage.getContent().equals("GET_ID")) {
            KoebutaApplication.getInstance().getClient().sendMessageToDiscuss(discussMessage.getDiscussId(), String.valueOf(discussMessage.getDiscussId()));
        }
        for (String reply : shouldAutoReply(discussMessage.getContent())) {
            KoebutaApplication.getInstance().getClient().sendMessageToDiscuss(discussMessage.getDiscussId(), reply);
        }
    }
}
