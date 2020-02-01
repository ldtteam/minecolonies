package com.minecolonies.coremod.research;

import com.minecolonies.api.research.effects.IResearchEffects;
import com.minecolonies.api.research.interfaces.IGlobalResearchTree;
import com.minecolonies.api.research.interfaces.IResearchEffect;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.*;

/**
 * The map of unlocked research effects of a given colony.
 */
public class ResearchEffects implements IResearchEffects
{
    /**
     * The map of the research effects, from a string identifier to the effect.
     */
    private final Map<String, IResearchEffect> effectMap = new HashMap<>();

    @Override
    public <W extends IResearchEffect> W getEffect(final String id, @NotNull final Class<W> type)
    {
        final IResearchEffect effect = effectMap.get(id);
        if (type.isInstance(effect))
        {
            return (W) effect;
        }

        Log.getLogger().warn("Unable to retrieve Effect with id: " + effect + " from the ResearchEffectsMap!");
        return null;
    }

    @Override
    public void applyEffect(final IResearchEffect effect)
    {
        effectMap.put(effect.getId(), effect);
    }

    @Override
    public void writeToNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT citizenTagList = effectMap.values().stream().map(effect -> {
            final CompoundNBT compoundNBT = new CompoundNBT();
            compoundNBT.putString(TAG_ID, effect.getResearchId());
            compoundNBT.putString(TAG_BRANCH, effect.getResearchBranch());
            return compoundNBT;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_RESEARCH_EFFECTS, citizenTagList);
    }

    @Override
    public void readFromNBT(final CompoundNBT compound)
    {
        effectMap.putAll(NBTUtils.streamCompound(compound.getList(TAG_RESEARCH_EFFECTS, Constants.NBT.TAG_COMPOUND))
                              .map(researchCompound -> {
                                  final String researchId = researchCompound.getString(TAG_ID);
                                  final String branch = researchCompound.getString(TAG_BRANCH);
                                  return IGlobalResearchTree.getInstance().getResearch(branch, researchId).getEffect();
                              })
                           .filter(Objects::nonNull)
                           .collect(Collectors.toMap(IResearchEffect::getId, iEffect -> iEffect)));
    }
}
