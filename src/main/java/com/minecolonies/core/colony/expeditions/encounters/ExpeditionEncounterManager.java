package com.minecolonies.core.colony.expeditions.encounters;

import com.minecolonies.core.datalistener.ColonyExpeditionTypeListener;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager class for all the possible {@link ExpeditionEncounter} instances.
 */
public class ExpeditionEncounterManager
{
    /**
     * The singleton instance.
     */
    private static ExpeditionEncounterManager instance;

    /**
     * The map of all possible expedition types.
     */
    private final Map<ResourceLocation, ExpeditionEncounter> possibleTypes = new HashMap<>();

    /**
     * Internal constructor.
     */
    private ExpeditionEncounterManager()
    {
    }

    /**
     * Get the singleton instance for the expedition encounter manager.
     *
     * @return the singleton instance.
     */
    public static ExpeditionEncounterManager getInstance()
    {
        if (instance == null)
        {
            instance = new ExpeditionEncounterManager();
        }
        return instance;
    }

    /**
     * Reload all types, initiated by {@link ColonyExpeditionTypeListener}.
     *
     * @param newTypes the new map of types.
     */
    public void reloadEncounters(final Map<ResourceLocation, ExpeditionEncounter> newTypes)
    {
        this.possibleTypes.clear();
        this.possibleTypes.putAll(newTypes);
    }

    /**
     * Get an encounter from its id.
     *
     * @param encounter the encounter id.
     * @return the encounter instance or null.
     */
    public ExpeditionEncounter getEncounter(final ResourceLocation encounter)
    {
        return this.possibleTypes.get(encounter);
    }
}
