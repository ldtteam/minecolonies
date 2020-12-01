package com.minecolonies.coremod.research.registry;

import com.minecolonies.api.research.effects.IResearchEffect;
import com.minecolonies.api.research.registry.IResearchEffectRegistry;
import com.minecolonies.coremod.research.AdditionModifierResearchEffect;
import com.minecolonies.coremod.research.MultiplierModifierResearchEffect;
import com.minecolonies.coremod.research.UnlockAbilityResearchEffect;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;

import java.util.*;

public class ResearchEffectRegistry implements IResearchEffectRegistry
{
    private Map<String, IResearchEffect> researchEffects = new HashMap<>();
    private Set<String> researchResettable = new HashSet<>();
    private Set<String> unlockBuildingEffects = new HashSet<>();
    private Set<String> unlockAbilityEffects = new HashSet<>();

    /**
     *  Attempts to register a new Research Effect.
     * @param effect the {@link IResearchEffect} containing the behavioral description for an event.
     * @param isSetOnWorldLoad The unique identifier of the research effect.
     * @return Returns true if successful, and false if a researchEffect of the same name id already is registered.
     */
    public boolean register(IResearchEffect effect, Boolean isSetOnWorldLoad)
    {
        if(!researchEffects.containsKey(effect))
        {
            researchEffects.put(effect.getId(), effect);
            if(isSetOnWorldLoad)
            {
                researchResettable.add(effect.getId());
            }
        }
        else
        {
            return false;
        }

        if(effect instanceof UnlockBuildingResearchEffect && !unlockBuildingEffects.contains(effect.getId()))
        {
            // Internal building Ids are stored in lower case.
            // Recording the unlock events gives a faster way to check what is locked, especially if a lot of researcheffects exist.
            // If we support building lock events, this will need to be turned into a Map<String,Boolean>, though.
            unlockBuildingEffects.add(effect.getId().toLowerCase());
        }
        else if (effect instanceof UnlockAbilityResearchEffect && !unlockAbilityEffects.contains(effect.getId()))
        {
            unlockAbilityEffects.add(effect.getId());
        }
        return true;
    }

    public Collection<IResearchEffect> getAllEffects()
    {
        return researchEffects.values();
    }

    public IResearchEffect<?> getEffect(String id)
    {
        if(researchEffects.containsKey(id))
        {
            return researchEffects.get(id);
        }
        else
        {
            return null;
        }
    }

    public boolean hasEffect(String id)
    {
        return researchEffects.containsKey(id);
    }

    public Set<String> getUnlockBuildingEffects()
    {
        return unlockBuildingEffects;
    }

    public Set<String> getUnlockAbilityEffects()
    {
        return unlockAbilityEffects;
    }

    public boolean isBuildingUnlockable(String buildingHutDesc)
    {
        return unlockBuildingEffects.contains(buildingHutDesc);
    }

    /**
     *  Research JSONs are registered on every world load.
     *  To support changing to worlds with different datapacks,
     *  we have to remove the ones set during ResearchListener.
     *  We can't just clear the Maps, because someone may set research effects
     *  in code during init or other stages that are not recreated, or even out of order.
     */
    public void resetRegistry()
    {
        for(String id : researchResettable)
        {
            researchEffects.remove(id);
            unlockAbilityEffects.remove(id);
            unlockBuildingEffects.remove(id);
        }
    }
}
