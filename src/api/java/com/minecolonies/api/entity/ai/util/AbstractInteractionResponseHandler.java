package com.minecolonies.api.entity.ai.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
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
    private ITextComponent inquiry;

    /**
     * The map of response options of the player, to new inquires of the interacting entity.
     */
    private Map<ITextComponent, ITextComponent> responses = new HashMap<>();

    /**
     * If the interaction is a primary (true) or secondary (false) interaction.
     */
    private boolean primary;

    /**
     * The inquiry of the citizen.
     * @param inquiry the text the citizen is supposed to say.
     * @param responseTuples optional response options.
     */
    @SafeVarargs
    public AbstractInteractionResponseHandler(@NotNull final ITextComponent inquiry, final boolean primary, final Tuple<ITextComponent, ITextComponent>...responseTuples)
    {
        this.inquiry = inquiry;
        this.primary = true;
        for (final Tuple<ITextComponent, ITextComponent> element : responseTuples)
        {
            this.responses.put(element.getA(), element.getB());
        }
    }

    /**
     * The inquiry of the citizen from NBT.
     * @param compoundNBT the compound to deserialize it from.
     */
    public AbstractInteractionResponseHandler(@NotNull final CompoundNBT compoundNBT)
    {
        this.deserializeNBT(compoundNBT);
    }

    @Override
    public ITextComponent getInquiry()
    {
        return inquiry;
    }

    @Nullable
    @Override
    public ITextComponent getResponseResult(final ITextComponent response)
    {
        return responses.getOrDefault(response, null);
    }

    @Override
    public List<ITextComponent> getPossibleResponses()
    {
        return new ArrayList<>(responses.keySet());
    }

    /**
     * Serialize the response handler to NBT.
     * @return the serialized data.
     */
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = new CompoundNBT();
        tag.putString(TAG_INQUIRY, ITextComponent.Serializer.toJson(this.inquiry));
        final ListNBT list = new ListNBT();
        for (final Map.Entry<ITextComponent, ITextComponent> element : responses.entrySet())
        {
            final CompoundNBT elementTag = new CompoundNBT();
            elementTag.putString(TAG_RESPONSE, ITextComponent.Serializer.toJson(element.getKey()));
            elementTag.putString(TAG_NEXT_INQUIRY, ITextComponent.Serializer.toJson(element.getValue()));

            list.add(elementTag);
        }
        tag.put(TAG_RESPONSES, list);
        tag.putBoolean(TAG_PRIMARY, isPrimary());
        return tag;
    }

    /**
     * Deserialize the response handler from NBT.
     */
    public void deserializeNBT(@NotNull final CompoundNBT compoundNBT)
    {
        this.inquiry = ITextComponent.Serializer.fromJson(compoundNBT.getString(TAG_INQUIRY));
        final ListNBT list = compoundNBT.getList(TAG_RESPONSE, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            this.responses.put(ITextComponent.Serializer.fromJson(compoundNBT.getString(TAG_RESPONSE)), ITextComponent.Serializer.fromJson(compoundNBT.getString(TAG_NEXT_INQUIRY)));
        }
        this.primary = compoundNBT.getBoolean(TAG_PRIMARY);
    }

    @Override
    public boolean isPrimary()
    {
        return primary;
    }
}
