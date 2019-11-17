package com.minecolonies.coremod.colony.interactionhandling;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.interactionhandling.AbstractInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.util.Tuple;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * The server side interaction response handler.
 */
public abstract class ServerCitizenInteractionResponseHandler extends AbstractInteractionResponseHandler
{
    /**
     * At which world tick this should be displayed again.
     */
    private int displayAtWorldTick = 0;

    /**
     * Validator
     */
    private Predicate<IColony> validator;

    /**
     * All registered parents of this response handler.
     */
    private Set<ITextComponent> parents = new HashSet<>();

    /**
     * The server interaction response handler.
     * @param inquiry the client inquiry.
     * @param primary if primary interaction.
     * @param priority the interaction priority.
     * @param validator validation predicate to check if this interaction is still valid.
     * @param responseTuples the tuples mapping player responses to further interactions.
     */
    @SafeVarargs
    public ServerCitizenInteractionResponseHandler(
      final ITextComponent inquiry,
      final boolean primary,
      final ChatPriority priority,
      final Predicate<IColony> validator,
      final Tuple<ITextComponent, ITextComponent>...responseTuples)
    {
        super(inquiry, primary, priority, responseTuples);
        this.validator = validator;
    }

    /**
     * The inquiry of the citizen from NBT.
     * @param compoundNBT the compound to deserialize it from.
     */
    public ServerCitizenInteractionResponseHandler(@NotNull final CompoundNBT compoundNBT)
    {
        super(compoundNBT);
    }

    @Override
    public boolean isVisible(final World world)
    {
        return displayAtWorldTick == 0 || displayAtWorldTick > world.getGameTime();
    }

    @Override
    public boolean isValid(final IColony colony)
    {
        return validator == null || validator.test(colony);
    }

    /**
     * Add a parent to the list.
     * @param parent the parent to add.
     */
    public void addParent(final ITextComponent parent)
    {
        this.parents.add(parent);
    }

    /**
     * Remove an old parent and return true if no parent is left.
     * @param oldParent the parent to remove.
     * @return true if now orphan.
     */
    public boolean removeParent(final ITextComponent oldParent)
    {
        this.parents.remove(oldParent);
        return this.parents.isEmpty();
    }

    @Override
    public void onResponseTriggered(final ITextComponent response, final World world)
    {
        if (response instanceof TranslationTextComponent)
        {
            if (((TranslationTextComponent) response).getKey().equals("com.minecolonies.coremod.gui.chat.remindmelater"))
            {
                displayAtWorldTick = (int) (world.getGameTime() + TICKS_SECOND * 60 * 10);
            }
            else if (((TranslationTextComponent) response).getKey().equals("com.minecolonies.coremod.gui.chat.ignore"))
            {
                displayAtWorldTick = Integer.MAX_VALUE;
            }
        }
    }

    /**
     * Build the child interaction list.
     * @return all interactions depending on this.
     */
    public abstract List<IInteractionResponseHandler> genChildInteractions();
}
