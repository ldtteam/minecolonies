package com.minecolonies.core.colony.expeditions.encounters;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

/**
 * Encounters are mob encounters that can be found on expeditions.
 *
 * @param id               The id of the encounter.
 * @param entityType       The entity type of the encounter.
 * @param damage           The damage this encounter will deal.
 * @param reflectingDamage The damage this encounter will reflect upon itself after dealing damage.
 * @param health           The health this encounter has.
 * @param armor            The armor level this encounter has.
 * @param lootTable        The loot table killing this encounter will give.
 * @param xp               The experience killing this encounter will give.
 */
public record ExpeditionEncounter(ResourceLocation id, EntityType<?> entityType, float damage, float reflectingDamage, double health, int armor, ResourceLocation lootTable,
                                  double xp)
{
    /**
     * Get the name of the encounter.
     *
     * @return the display name.
     */
    public Component getName()
    {
        return entityType.getDescription();
    }
}
