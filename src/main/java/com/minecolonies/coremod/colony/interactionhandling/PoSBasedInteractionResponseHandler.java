package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.structurize.api.util.BlockPosUtil;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorPredicates;
import com.minecolonies.api.colony.interactionhandling.ServerCitizenInteractionResponseHandler;
import com.minecolonies.api.util.Tuple;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * The position based interaction response handler.
 */
public class PoSBasedInteractionResponseHandler extends ServerCitizenInteractionResponseHandler
{
    private static final String POS_TAG = "pos";

    private static final Tuple[] tuples  = {
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.okay"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.ignore"), null),
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.remindmelater"), null)
    };

    /**
     * The position this is related to.
     */
    private BlockPos pos = null;

    /**
     * Specific validator for this one.
     */
    private Predicate<Tuple<ICitizenData, BlockPos>> validator;

    /**
     * The server interaction response handler.
     * @param inquiry the client inquiry.
     * @param priority the interaction priority.
     * @param pos the pos this is related to.
     */
    public PoSBasedInteractionResponseHandler(
      final ITextComponent inquiry,
      final ChatPriority priority,
      final ITextComponent validator,
      final BlockPos pos)
    {
        super(inquiry, true, priority, InteractionValidatorPredicates.map.getOrDefault(validator, null), validator, tuples);
        this.pos = pos;
    }

    /**
     * The server interaction response handler.
     * @param inquiry the client inquiry.
     * @param priority the interaction priority.
     * @param pos the pos this is related to.
     */
    public PoSBasedInteractionResponseHandler(
      final ITextComponent inquiry,
      final ChatPriority priority,
      final BlockPos pos)
    {
        super(inquiry, true, priority, InteractionValidatorPredicates.map.getOrDefault(inquiry, null), inquiry, tuples);
        this.pos = pos;
    }

    /**
     * The inquiry of the citizen from NBT.
     * @param compoundNBT the compound to deserialize it from.
     */
    public PoSBasedInteractionResponseHandler(@NotNull final CompoundNBT compoundNBT)
    {
        super(compoundNBT);
    }

    /**
     * Way to load the response handler for a citizen.
     * @param data the citizen owning this handler.
     */
    public PoSBasedInteractionResponseHandler(final ICitizenData data)
    {
        super(data);
    }

    @Override
    public List<IInteractionResponseHandler> genChildInteractions()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isValid(final ICitizenData citizen)
    {
        return (validator == null && !this.parents.isEmpty()) || ( validator != null && validator.test(new Tuple<>(citizen, pos)) );
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = super.serializeNBT();
        BlockPosUtil.writeToNBT(tag, POS_TAG, pos);
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final CompoundNBT compoundNBT)
    {
        super.deserializeNBT(compoundNBT);
        this.pos = BlockPosUtil.readFromNBT(compoundNBT, POS_TAG);
    }

    @Override
    protected void loadValidator()
    {
        this.validator = InteractionValidatorPredicates.posMap.get(getInquiry());
    }
}
