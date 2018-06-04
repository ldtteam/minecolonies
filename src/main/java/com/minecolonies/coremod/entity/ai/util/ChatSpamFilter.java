package com.minecolonies.coremod.entity.ai.util;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.EntityCitizen;
import net.minecraft.util.text.TextComponentBase;
import org.jetbrains.annotations.NotNull;

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
    private final CitizenData citizenData;

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
     * @param citizenData the worker who will sends chats through this filter.
     */
    public ChatSpamFilter(final CitizenData citizenData)
    {
        this.citizenData = citizenData;
    }

    /**
     * Request an Item without spamming the chat.
     *
     * @param chat the Item Name
     */
    public void requestTextComponentWithoutSpam(@NotNull final TextComponentBase chat)
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
    public void talkWithoutSpam(final String key, final Object... chat)
    {
        if (!citizenData.getCitizenEntity().isPresent())
        {
            return;
        }

        final EntityCitizen worker = citizenData.getCitizenEntity().get();

        @NotNull final String curstring = key + getStringOfChat(chat);
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

        worker.getCitizenChatHandler().sendLocalizedChat(key, chat);
        speechDelayString = key + getStringOfChat(chat);

        // (BASE_TIMEOUT << speechRepeat) is the same as BASE_TIMEOUT * pow(2, speachRepeat), but uses integers
        speechDelay = Math.min(BASE_TIMEOUT << speechRepeat, MAX_TIMEOUT) + worker.getOffsetTicks();
    }

    /**
     * Get a describing string of an Object array.
     *
     * @param chat the object array.
     * @return the describing string.
     */
    public String getStringOfChat(final Object... chat)
    {
        if (chat.length == 0)
        {
            return "";
        }
        final StringBuilder tempString = new StringBuilder();
        for (final Object object : chat)
        {
            if (object instanceof TextComponentBase)
            {
                tempString.append(((TextComponentBase) object).getUnformattedText());
            }
            else
            {
                tempString.append(object);
            }
        }
        return tempString.toString();
    }

    /**
     * Request an Item without spamming the chat.
     *
     * @param chat the Item Name
     */
    public void requestTextStringWithoutSpam(@NotNull final String chat)
    {
        talkWithoutSpam("entity.miner.messageNeedBlockAndItem", chat);
    }
}
