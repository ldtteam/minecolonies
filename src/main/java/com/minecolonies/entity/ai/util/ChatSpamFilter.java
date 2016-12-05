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
    protected static final int BASE_TIMEOUT = 30 * 20;

    /**
     * The maximum delay to wait
     * <p>
     * 20 Ticks per second.
     * 60 Seconds per Minute.
     * 10 Minutes.
     */
    protected static final int MAX_TIMEOUT = 20 * 60 * 10;

    /**
     * The worker we send chats from.
     */
    private final EntityCitizen worker;

    /**
     * Ticks to wait till we let a new chat through.
     */
    private int speechDelay = 0;

    /**
     * The current chat message to compare incoming messages to.
     */
    @NotNull
    private String speechDelayString = "";

    /**
     * The number of times the current message was already send.
     */
    private int speechRepeat = 0;

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
    public void requestWithoutSpam(@NotNull final String chat)
    {
        talkWithoutSpam("entity.miner.messageNeedBlockAndItem", chat);
    }

    /**
     * Send a chat message as often as you like.
     * It will be shown in certain delays.
     * Helpful for requesting items.
     *
     * @param key  the translation key
     * @param chat the chat message
     */
    public void talkWithoutSpam(final String key, final String... chat)
    {
        @NotNull final String curstring = key + Arrays.toString(chat);
        if (Objects.equals(speechDelayString, curstring))
        {
            if (speechDelay > worker.getOffsetTicks())
            {
                return;
            }

            // this check is to protect against overflows
            // (BASE_TIMEOUT << speechRepeat) is the same as BASE_TIMEOUT * pow(2, speachRepeat), but uses integers
            if ((BASE_TIMEOUT << speechRepeat) < MAX_TIMEOUT)
            {
                speechRepeat++;
            }
        }
        else
        {
            speechRepeat = 0;
        }

        worker.sendLocalizedChat(key, (Object[]) chat);
        speechDelayString = key + Arrays.toString(chat);

        // (BASE_TIMEOUT << speechRepeat) is the same as BASE_TIMEOUT * pow(2, speachRepeat), but uses integers
        speechDelay = Math.min(BASE_TIMEOUT << speechRepeat, MAX_TIMEOUT) + worker.getOffsetTicks();
    }
}
