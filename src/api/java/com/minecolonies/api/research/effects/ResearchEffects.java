package com.minecolonies.api.research.effects;

import com.minecolonies.api.research.GlobalResearchTree;
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
public class ResearchEffects
{
    /**
     * The map of the research effects, from a string identifier to the effect.
     */
    private final Map<String, IResearchEffect> effectMap = new HashMap<>();

    /**
     * Get the research effect which is assigned to a particular string.
     * @param id the id of the effect.
     * @param type it's type.
     * @param <W> the Generic type.
     * @return one of the expected type or null.
     */
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

    /**
     * Apply the effect to the research effects class.
     * @param effect the effect to apply.
     */
    public void applyEffect(final IResearchEffect effect)
    {
        effectMap.put(effect.getId(), effect);
    }

    /**
     * Write the research tree to NBT.
     * @param compound the compound.
     */
    public void writeToNBT(final CompoundNBT compound)
    {
        @NotNull final ListNBT citizenTagList = effectMap.values().stream().map(effect -> {
            final CompoundNBT compoundNBT = new CompoundNBT();
            compound.putString(TAG_ID, effect.getResearchId());
            compound.putString(TAG_BRANCH, effect.getResearchBranch());
            return compoundNBT;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_RESEARCH_EFFECTS, citizenTagList);
    }

    /**
     * Read the research tree from NBT.
     * @param compound the compound to read it from.
     */
    public void readFromNBT(final CompoundNBT compound)
    {
        effectMap.putAll(NBTUtils.streamCompound(compound.getList(TAG_RESEARCH_EFFECTS, Constants.NBT.TAG_COMPOUND))
                              .map(researchCompound -> {
                                  final String researchId = compound.getString(TAG_ID);
                                  final String branch = compound.getString(TAG_BRANCH);
                                  return GlobalResearchTree.researchTree.getResearch(branch, researchId).getEffect();
                              })
                           .filter(Objects::nonNull)
                           .collect(Collectors.toMap(IResearchEffect::getId, iEffect -> iEffect)));
    }
}
