package com.minecolonies.api.research;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.ResearchConstants.TAG_RESEARCH_EFFECTS;

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
    public void writeToNBT(final NBTTagCompound compound)
    {
        @NotNull final NBTTagList citizenTagList = effectMap.values().stream().map(effect -> StandardFactoryController.getInstance().serialize(effect)).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_RESEARCH_EFFECTS, citizenTagList);
    }

    /**
     * Read the research tree from NBT.
     * @param compound the compound to read it from.
     */
    public void readFromNBT(final NBTTagCompound compound)
    {
        effectMap.putAll(NBTUtils.streamCompound(compound.getTagList(TAG_RESEARCH_EFFECTS, Constants.NBT.TAG_COMPOUND))
                              .map(researchCompound -> (IResearchEffect) StandardFactoryController.getInstance().deserialize(researchCompound))
                              .collect(Collectors.toMap(IResearchEffect::getId, iEffect -> iEffect)));
    }
}
