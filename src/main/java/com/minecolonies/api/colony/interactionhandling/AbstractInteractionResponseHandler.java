package com.minecolonies.api.colony.interactionhandling;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.ldtteam.structurize.api.util.Tuple;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The abstract interaction response handler to be extended by the other ones.
 */
public abstract class AbstractInteractionResponseHandler implements IInteractionResponseHandler
{
    /**
     * The text the citizen is saying.
     */
    private Component inquiry;

    /**
     * The map of response options of the player, to new inquires of the interacting entity.
     */
    private Map<Component, Component> responses = new LinkedHashMap<>();

    /**
     * If the interaction is a primary (true) or secondary (false) interaction.
     */
    private boolean primary;

    /**
     * The interaction priority.
     */
    private IChatPriority priority;

    /**
     * The inquiry of the citizen.
     *
     * @param inquiry        the inquiry.
     * @param primary        if primary inquiry.
     * @param priority       the priority.
     * @param responseTuples optional response options.
     */
    @SafeVarargs
    public AbstractInteractionResponseHandler(
      @NotNull final Component inquiry,
      final boolean primary,
      final IChatPriority priority,
      final Tuple<Component, Component>... responseTuples)
    {
        this.inquiry = inquiry;
        this.primary = primary;
        this.priority = priority;
        for (final Tuple<Component, Component> element : responseTuples)
        {
            this.responses.put(element.getA(), element.getB());
        }
    }

    /**
     * Way to load the response handler.
     */
    public AbstractInteractionResponseHandler()
    {
        // Do nothing, await loading from NBT.
    }

    @Override
    public Component getInquiry()
    {
        return inquiry;
    }

    @Nullable
    @Override
    public Component getResponseResult(final Component response)
    {
        return responses.getOrDefault(response, null);
    }

    @Override
    public List<Component> getPossibleResponses()
    {
        return ImmutableList.copyOf(responses.keySet());
    }

    /**
     * Serialize the response handler to NBT.
     *
     * @return the serialized data.
     */
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag tag = new CompoundTag();
        tag.store(TAG_INQUIRY, ComponentSerialization.CODEC, this.inquiry);
        final ListTag list = new ListTag();
        for (final Map.Entry<Component, Component> element : responses.entrySet())
        {
            final CompoundTag elementTag = new CompoundTag();
            elementTag.store(TAG_RESPONSE, ComponentSerialization.CODEC, element.getKey());
            elementTag.store(TAG_NEXT_INQUIRY, ComponentSerialization.CODEC, element.getValue());

            list.add(elementTag);
        }
        tag.put(TAG_RESPONSES, list);
        tag.putBoolean(TAG_PRIMARY, isPrimary());
        tag.putInt(TAG_PRIORITY, priority.getPriority());
        tag.putString(NbtTagConstants.TAG_HANDLER_TYPE, getType());
        return tag;
    }
    
    /**
     * Deserialize the response handler from NBT.
     */
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag compoundNBT)
    {
        this.inquiry = compoundNBT.read(TAG_INQUIRY, ComponentSerialization.CODEC).orElse(null);
        final ListTag list = compoundNBT.getListOrEmpty(TAG_RESPONSES);
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundTag nbt = list.getCompoundOrEmpty(i);
            final Component response = nbt.read(TAG_RESPONSE, ComponentSerialization.CODEC).orElse(null);
            final Component nextInquiry = nbt.read(TAG_NEXT_INQUIRY, ComponentSerialization.CODEC).orElse(null);
            this.responses.put(response, nextInquiry);
        }
        this.primary = compoundNBT.getBooleanOr(TAG_PRIMARY, false);
        this.priority = ChatPriority.values()[compoundNBT.getIntOr(TAG_PRIORITY, 0)];
    }

    @Override
    public boolean isPrimary()
    {
        return primary;
    }

    @Override
    public IChatPriority getPriority()
    {
        return this.priority;
    }

    @Override
    public boolean isVisible(final Level world)
    {
        return true;
    }

    @Override
    public boolean isValid(final ICitizenData colony)
    {
        return true;
    }
}
