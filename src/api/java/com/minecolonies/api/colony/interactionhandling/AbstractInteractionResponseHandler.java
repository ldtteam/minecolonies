package com.minecolonies.api.colony.interactionhandling;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.NbtTagConstants;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The abstract interaction response handler to be extended by the other ones.
 */
public abstract class AbstractInteractionResponseHandler implements IInteractionResponseHandler
{
    /**
     * The text the citizen is saying.
     */
    private IInteractionIdentifier inquiry;

    /**
     * The map of response options of the player, to new inquires of the interacting entity.
     */
    private Map<IInteractionIdentifier, IInteractionIdentifier> responses = new HashMap<>();

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
     * @param inquiry the inquiry.
     * @param primary if primary inquiry.
     * @param priority the priority.
     * @param responseTuples optional response options.
     */
    @SafeVarargs
    public AbstractInteractionResponseHandler(@NotNull final IInteractionIdentifier inquiry,
      final boolean primary,
      final IChatPriority priority,
      final Tuple<IInteractionIdentifier, IInteractionIdentifier>...responseTuples)
    {
        this.inquiry = inquiry;
        this.primary = primary;
        this.priority = priority;
        for (final Tuple<IInteractionIdentifier, IInteractionIdentifier> element : responseTuples)
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
    public IInteractionIdentifier getInquiry()
    {
        return inquiry;
    }

    @Nullable
    @Override
    public IInteractionIdentifier getResponseResult(final IInteractionIdentifier response)
    {
        return responses.getOrDefault(response, null);
    }

    @Override
    public List<IInteractionIdentifier> getPossibleResponses()
    {
        return ImmutableList.copyOf(responses.keySet());
    }

    /**
     * Serialize the response handler to NBT.
     * @return the serialized data.
     */
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.putString(TAG_INQUIRY, ITextComponent.Serializer.toJson(this.inquiry.getDisplayName()));
        final ListNBT list = new ListNBT();
        for (final Map.Entry<IInteractionIdentifier, IInteractionIdentifier> element : responses.entrySet())
        {
            final CompoundNBT elementTag = new CompoundNBT();
            elementTag.putString(TAG_RESPONSE, ITextComponent.Serializer.toJson(element.getKey().getDisplayName()));
            elementTag.putString(TAG_NEXT_INQUIRY, ITextComponent.Serializer.toJson(element.getValue() == null ? null : element.getValue().getDisplayName()));

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
    public void deserializeNBT(@NotNull final CompoundNBT compoundNBT)
    {
        this.inquiry = new TextInteractionId(ITextComponent.Serializer.fromJson(compoundNBT.getString(TAG_INQUIRY)));
        final ListNBT list = compoundNBT.getList(TAG_RESPONSES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            final CompoundNBT nbt = list.getCompound(i);
            this.responses.put(new TextInteractionId(ITextComponent.Serializer.fromJson(nbt.getString(TAG_RESPONSE)))
              , new TextInteractionId(ITextComponent.Serializer.fromJson(nbt.getString(TAG_NEXT_INQUIRY))));
        }
        this.primary = compoundNBT.getBoolean(TAG_PRIMARY);
        this.priority = ChatPriority.values()[compoundNBT.getInt(TAG_PRIORITY)];
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
    public boolean isVisible(final World world)
    {
        return true;
    }

    @Override
    public boolean isValid(final ICitizenData colony)
    {
        return true;
    }
}
