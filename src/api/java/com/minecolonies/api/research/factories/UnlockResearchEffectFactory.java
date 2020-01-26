package com.minecolonies.api.research.factories;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.UnlockResearchEffect;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.ResearchConstants.TAG_ID;
import static com.minecolonies.api.research.ResearchConstants.TAG_UNLOCK;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing UnlockResearchEffects.
 */
public class UnlockResearchEffectFactory implements IResearchEffectFactory<UnlockResearchEffect>
{
    @NotNull
    @Override
    public TypeToken<UnlockResearchEffect> getFactoryOutputType()
    {
        return TypeConstants.UNLOCK_RESEARCH_EF;
    }

    @NotNull
    @Override
    public TypeToken<FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @NotNull
    @Override
    public UnlockResearchEffect getNewInstance(@NotNull final String id, final Object obj)
    {
        return new UnlockResearchEffect(id, (Boolean) obj);
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final UnlockResearchEffect effect)
    {
        final CompoundNBT compound = new CompoundNBT();
        @NotNull final CompoundNBT stackTag = new CompoundNBT();
        compound.putString(TAG_ID, effect.getId());
        compound.putBoolean(TAG_UNLOCK, effect.getEffect());
        return compound;
    }

    @NotNull
    @Override
    public UnlockResearchEffect deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final String id = nbt.getString(TAG_ID);
        final boolean effect = nbt.getBoolean(TAG_UNLOCK);
        return this.getNewInstance(id, effect);
    }
}
