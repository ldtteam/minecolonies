package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenChatHandler;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * The citizen chat handler which handles all possible notifications (blocking or not).
 */
public class CitizenChatHandler implements ICitizenChatHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final EntityCitizen citizen;

    /**
     * Delay for a message to be relevant again. (For remind me later).
     */
    private static final int MAX_IGNORE_TICKS = TICKS_SECOND * 60 * 20;

    /**
     * Messages to be reminded about later.
     */
    private final Map<ITextComponent, Tuple<Integer, Boolean>> remindMeLater = new HashMap<>();

    /**
     * All kind of blocking messages.
     */
    private final Set<ITextComponent> blocking = new HashSet<>();

    /**
     * All kinds of general pending messages.
     */
    private final Set<ITextComponent> pending = new HashSet<>();

    /**
     * Constructor for the experience handler.
     *
     * @param citizen the citizen owning the handler.
     */
    public CitizenChatHandler(final EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    @Override
    public void solve(final ITextComponent component)
    {
        blocking.remove(component);
        pending.remove(component);
        remindMeLater.remove(component);
    }

    @Override
    public void remindMeLater(final ITextComponent component, final int worldTick)
    {
        if (blocking.contains(component))
        {
            blocking.remove(component);
            remindMeLater.put(component, new Tuple<>(worldTick, true));
        }

        if (pending.contains(component))
        {
            pending.remove(component);
            remindMeLater.put(component, new Tuple<>(worldTick, false));
        }
    }

    //todo do we need a predicate with "does this still apply?" ?

    //todo for different chat types we'll create different handlers which we then add to the citizenData.
    //todo: ChitChat, Request (Blocking, Pending), Action Required

    //todo we want one type for just general chitchat (happiness (food, housing, guards, jobs, etc), night, rain, mourning, etc, job related states).

    //todo we need to be able to differentiate between blocking and pending on the client side too!

    //todo, first, in the serverCitizenInteractionResponseHandler we need always the options of "remindMeLater" and "okay"

    @Override
    public void sendLocalizedChat(final String keyIn, final boolean isBlocking, final int worldTick, final Object... args)
    {
        final String key = keyIn.toLowerCase(Locale.US);
        final TranslationTextComponent message;
        if (args.length == 0)
        {
            message = new TranslationTextComponent(key);
        }
        else
        {
            message = new TranslationTextComponent(key, args);
        }

        if (remindMeLater.containsKey(message))
        {
            final Tuple<Integer, Boolean> tuple = remindMeLater.get(message);
            if (tuple.getA() + MAX_IGNORE_TICKS > worldTick)
            {
                if (tuple.getB())
                {
                    blocking.add(message);
                }
                else
                {
                    pending.add(message);
                }
                remindMeLater.remove(message);
            }
        }
    }

    /**
     * Notify about death of citizen.
     * @param damageSource the damage source.
     */
    @Override
    public void notifyDeath(final DamageSource damageSource)
    {
        if (citizen.getCitizenColonyHandler().getColony() != null && citizen.getCitizenData() != null)
        {
            final IJob job = citizen.getCitizenJobHandler().getColonyJob();
            if (job != null)
            {
                final ITextComponent component = new TranslationTextComponent(
                  "block.blockhuttownhall.messageworkerdead",
                  new TranslationTextComponent(job.getName()),
                  citizen.getCitizenData().getName(),
                  (int) citizen.posX,
                  (int) citizen.posY,
                  (int) citizen.posZ, damageSource.damageType);
                LanguageHandler.sendPlayersMessage(citizen.getCitizenColonyHandler().getColony().getImportantMessageEntityPlayers(), component.getUnformattedComponentText());
            }
            else
            {
                LanguageHandler.sendPlayersMessage(
                  citizen.getCitizenColonyHandler().getColony().getImportantMessageEntityPlayers(),
                  "block.blockhuttownhall.messagecolonistdead",
                  citizen.getCitizenData().getName(), (int) citizen.posX, (int) citizen.posY, (int) citizen.posZ, damageSource.damageType);
            }
        }
    }
}
