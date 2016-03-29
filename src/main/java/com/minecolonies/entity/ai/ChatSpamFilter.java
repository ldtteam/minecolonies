package com.minecolonies.entity.ai;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.Utils;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Objects;

/**
 * Send chat messages without worry about spam.
 */
public class ChatSpamFilter
{

    /**
     * Custom logger for the class.
     */
    private static final Logger LOGGER = Utils.generateLoggerForClass(ChatSpamFilter.class);
    private final EntityCitizen worker;
    private int speechDelay = 0;
    private String speechDelayString = "";
    private int speechRepeat = 1;

    public ChatSpamFilter(final EntityCitizen worker){
        this.worker = worker;
    }

    /**
     * Request an Item without spamming the chat.
     * @param chat the Item Name
     */
    public void requestWithoutSpam(String chat)
    {
        talkWithoutSpam("entity.miner.messageNeedBlockAndItem", chat);
    }

    /**
     * Send a chat message as often as you like.
     * It will be shown in certain delays.
     * Helpful for requesting items.
     * @param key the translation key
     * @param chat the chat message
     */
    public void talkWithoutSpam(String key, String... chat)
    {
        String curstring = key + Arrays.toString(chat);
        if (Objects.equals(speechDelayString, curstring))
        {
            if (speechDelay > 0)
            {
                speechDelay--;
                return;
            }
            speechRepeat++;
        }
        else
        {
            speechDelay = 0;
            speechRepeat = 1;
        }
        worker.sendLocalizedChat(key, (Object[]) chat);
        speechDelayString = key + Arrays.toString(chat);

        speechDelay = (int) Math.pow(30, speechRepeat);
    }
}
