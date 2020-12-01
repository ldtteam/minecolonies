package com.minecolonies.api.research.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.research.effects.IResearchEffect;

import java.util.Collection;
import java.util.Set;

/**
 *   An interface providing information about Minecolonies Research Effects,
 *   and the ability to register new effects.  Also some helper classes to reduce later sorting.
 *   This ResearchEffectRegistry should include both research effects regardless of status:
 *   Use {@link com.minecolonies.api.research.effects.IResearchEffectManager}
 *   to set and apply effects for completed research.
 *   Use {MinecoloniesAPIProxy.getInstance().getGlobalResearchTree()}
 *   to add research to the University Research System.
 */

public interface IResearchEffectRegistry
{
    static IResearchEffectRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getResearchEffectRegistry();
    }

    boolean register(IResearchEffect effect, Boolean isSetOnWorldLoad);

    Collection<IResearchEffect> getAllEffects();

    Collection<IResearchEffect> getEffect(String id);

    // Deriving underlying types of effects is relatively complex in code.
    // These caches should simplify code access, at little memory cost.
    Set<String> getUnlockBuildingEffects();

    Set<String> getUnlockAbilityEffects();

    boolean isBuildingUnlockable(String buildingHutDesc);

    void resetRegistry();
}
