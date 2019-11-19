package com.minecolonies.api.colony.interactionhandling;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.util.Tuple;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
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
    private static final String TAG_DELAY   = "delay";
    private static final String TAG_PARENT  = "parent";
    private static final String TAG_PARENTS = "parents";

    /**
     * At which world tick this should be displayed again.
     */
    private int displayAtWorldTick     = 0;

    /**
     * Validator to test for this.
     */
    private Predicate<ICitizenData> validator;

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
      final Predicate<ICitizenData> validator,
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
    public boolean isValid(final ICitizenData colony)
    {
        return (validator == null && !this.parents.isEmpty()) || validator.test(colony);
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
     */
    public void removeParent(final ITextComponent oldParent)
    {
        this.parents.remove(oldParent);
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

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compoundNBT = super.serializeNBT();
        compoundNBT.putInt(TAG_DELAY, displayAtWorldTick);
        final ListNBT list = new ListNBT();
        for (final ITextComponent element : parents)
        {
            final CompoundNBT elementTag = new CompoundNBT();
            elementTag.putString(TAG_PARENT, ITextComponent.Serializer.toJson(element));
            list.add(elementTag);
        }
        compoundNBT.put(TAG_PARENTS, list);
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(@NotNull final CompoundNBT compoundNBT)
    {
        super.deserializeNBT(compoundNBT);
        this.displayAtWorldTick = compoundNBT.getInt(TAG_DELAY);
        this.parents.clear();
        final ListNBT list = compoundNBT.getList(TAG_PARENTS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            this.parents.add(ITextComponent.Serializer.fromJson(compoundNBT.getString(TAG_PARENT)));
        }
        this.validator = InteractionValidatorPredicates.map.get(getInquiry());
    }
}
