package com.minecolonies.api.research.factories;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.research.ModifierResearchEffect;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.ResearchConstants.TAG_ID;
import static com.minecolonies.api.research.ResearchConstants.TAG_MODIFIER;

/**
 * Factory implementation taking care of creating new instances, serializing and deserializing RecipeStorages.
 */
public class ModifierResearchEffectFactory implements IResearchEffectFactory<ModifierResearchEffect>
{
    @NotNull
    @Override
    public TypeToken<ModifierResearchEffect> getFactoryOutputType()
    {
        return TypeConstants.MOD_RESEARCH_EF;
    }

    @NotNull
    @Override
    public TypeToken<FactoryVoidInput> getFactoryInputType()
    {
        return TypeConstants.FACTORYVOIDINPUT;
    }

    @NotNull
    @Override
    public ModifierResearchEffect getNewInstance(@NotNull final String id, final Object obj)
    {
        return new ModifierResearchEffect(id, (Double) obj);
    }

    @NotNull
    @Override
    public CompoundNBT serialize(@NotNull final IFactoryController controller, @NotNull final ModifierResearchEffect effect)
    {
        final CompoundNBT compound = new CompoundNBT();
        @NotNull final CompoundNBT stackTag = new CompoundNBT();
        compound.putString(TAG_ID, effect.getId());
        compound.putDouble(TAG_MODIFIER, effect.getEffect());
        return compound;
    }

    @NotNull
    @Override
    public ModifierResearchEffect deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundNBT nbt)
    {
        final String id = nbt.getString(TAG_ID);
        final double effect = nbt.getDouble(TAG_MODIFIER);
        return this.getNewInstance(id, effect);
    }
}
