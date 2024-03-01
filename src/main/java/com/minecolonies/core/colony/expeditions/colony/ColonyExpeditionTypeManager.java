package com.minecolonies.core.colony.expeditions.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.IColonyExpeditionManager;
import com.minecolonies.core.datalistener.ColonyExpeditionTypeListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manager class for all the possible {@link ColonyExpeditionType} instances.
 */
public class ColonyExpeditionTypeManager
{
    /**
     * The singleton instance.
     */
    private static ColonyExpeditionTypeManager instance;

    /**
     * The map of all possible expedition types.
     */
    private final Map<ResourceLocation, ColonyExpeditionType> possibleTypes = new HashMap<>();

    /**
     * Randomizer instance.
     */
    private final Random random = new Random();

    /**
     * Internal constructor.
     */
    private ColonyExpeditionTypeManager()
    {
    }

    /**
     * Get the singleton instance for the colony expedition type manager.
     *
     * @return the singleton instance.
     */
    public static ColonyExpeditionTypeManager getInstance()
    {
        if (instance == null)
        {
            instance = new ColonyExpeditionTypeManager();
        }
        return instance;
    }

    /**
     * Reload all types, initiated by {@link ColonyExpeditionTypeListener}.
     *
     * @param newTypes the new map of types.
     */
    public void reloadTypes(final Map<ResourceLocation, ColonyExpeditionType> newTypes)
    {
        this.possibleTypes.clear();
        this.possibleTypes.putAll(newTypes);
    }

    /**
     * @param colony the colony reference to get the expedition manager for.
     * @return the expedition type.
     */
    public boolean canStartExpedition(final IColony colony)
    {
        return colony.hasTownHall() && colony.getBuildingManager().getTownHall().getBuildingLevel() > 0
                 && random.nextInt(100) < 50;
    }

    /**
     * Get the provided expedition type from its id.
     *
     * @param id the id.
     * @return the expedition type instance.
     */
    public ColonyExpeditionType getExpeditionType(final ResourceLocation id)
    {
        return this.possibleTypes.get(id);
    }

    /**
     * Obtain a random expedition type from the map of possible expedition types.
     * The target dimension must be reachable according to {@link IColonyExpeditionManager#canGoToDimension(ResourceKey)}.
     * This method can also return null, if there are no expedition types available at all.
     *
     * @param colony the colony reference to get the expedition manager for.
     * @return the expedition type.
     */
    @Nullable
    public ColonyExpeditionType getRandomExpeditionType(final IColony colony)
    {
        final IColonyExpeditionManager expeditionManager = colony.getExpeditionManager();
        final List<ColonyExpeditionType> expeditionTypes = new ArrayList<>(possibleTypes.values());

        ColonyExpeditionType chosenExpeditionType = null;
        while (!expeditionTypes.isEmpty() && chosenExpeditionType == null)
        {
            final ColonyExpeditionType colonyExpeditionType = expeditionTypes.get(random.nextInt(expeditionTypes.size()));
            if (!expeditionManager.canGoToDimension(colonyExpeditionType.getDimension()))
            {
                expeditionTypes.removeIf(type -> type.getDimension().equals(colonyExpeditionType.getDimension()));
                continue;
            }

            chosenExpeditionType = colonyExpeditionType;
        }

        return chosenExpeditionType;
    }
}