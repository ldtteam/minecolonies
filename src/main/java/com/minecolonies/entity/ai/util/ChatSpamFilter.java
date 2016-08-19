package com.minecolonies.entity.ai.util;

import com.minecolonies.entity.EntityCitizen;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Send chat messages without worry about spam.
 */
public class ChatSpamFilter
{
    /**
     * The timeout in ticks to wait initially.
     * <p>
     * 20 Ticks per second.
     * 30 Seconds.
     */
    protected static final int BASE_TIMEOUT  = 30 * 20;
    /**
     * The maximum delay to wait
     * <p>
     * 20 Ticks per second.
     * 60 Seconds per Minute.
     * 10 Minutes.
     */
    protected static final int MAX_TIMEOUT   = 20 * 60 * 10;
    /**
     * The number to multiply timeout with every time
     * <p>
     * BASE_TIMEOUT * 2^times
     */
    private static final   int POWER_TIMEOUT = 2;
    /**
     * The worker we send chats from.
     */
    private final EntityCitizen worker;
    /**
     * Ticks to wait till we let a new chat through.
     */
    private int    speechDelay       = 0;
    /**
     * The current chat message to compare incoming messages to.
     */
    private String speechDelayString = "";
    /**
     * The number of times the current message was already send.
     */
    private int    speechRepeat      = 0;

    /**
     * Create a new ChatSpamFilter for the worker.
     *
     * @param worker the worker who will sends chats through this filter.
     */
    public ChatSpamFilter(final EntityCitizen worker)
    {
        this.worker = worker;
    }

    /**
     * Request an Item without spamming the chat.
     *
     * @param chat the Item Name
     */
    public void requestWithoutSpam(@NotNull String chat)
    {
        requestWithoutSpam(chat, 1);
    }

    /**
     * Request an Item without spamming the chat.
     *
     * @param chat  the Item Name
     * @param ticks the amount of ticks waited
     */
    public void requestWithoutSpam(@NotNull String chat, int ticks)
    {
        talkWithoutSpam("entity.miner.messageNeedBlockAndItem", ticks, chat);
    }

    /**
     * Send a chat message as often as you like.
     * It will be shown in certain delays.
     * Helpful for requesting items.
     *
     * @param key   the translation key
     * @param ticks the amount of ticks waited
     * @param chat  the chat message
     */
    public void talkWithoutSpam(String key, int ticks, String... chat)
    {
        String curstring = key + Arrays.toString(chat);
        if (Objects.equals(speechDelayString, curstring))
        {
            if (speechDelay > 0)
            {
                speechDelay -= ticks;
                return;
            }
            speechRepeat++;
        }
        else
        {
            speechDelay = 0;
            speechRepeat = 0;
        }
        worker.sendLocalizedChat(key, (Object[]) chat);
        speechDelayString = key + Arrays.toString(chat);
        speechDelay = Math.min((int) (BASE_TIMEOUT * Math.pow(POWER_TIMEOUT, speechRepeat)), MAX_TIMEOUT);
    }

    /**
     * Send a chat message as often as you like.
     * It will be shown in certain delays.
     * Helpful for requesting items.
     *
     * @param key  the translation key
     * @param chat the chat message
     */
    public void talkWithoutSpam(String key, String... chat)
    {
        talkWithoutSpam(key, 1, chat);
    }
}
