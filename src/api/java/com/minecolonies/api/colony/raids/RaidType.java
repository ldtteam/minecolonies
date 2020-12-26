package com.minecolonies.api.colony.raids;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * List of combat raid types.
 */
public class RaidType extends ForgeRegistryEntry<RaidType>
{
    /**
     * The identifier for the group of raiders.
     */
    private final String raiderName;

    /**
     * Getter for the raider style's name.
     *
     * @return The raider type as a String.
     */
    public String getRaiderName()
    {
        return raiderName;
    }

    /**
     * Builds a new registry entry for a raider group.
     *
     * @param raiderName   Raider group identifier
     */
    public RaidType(final String raiderName)
    {
        super();
        this.raiderName = raiderName;
    }

    /**
     * The builder.
     */
    public static class Builder
    {
        private String                         raiderId;
        private ResourceLocation               registryName;

        public RaidType.Builder setId(final String name)
        {
            this.raiderId = name;
            return this;
        }

        public RaidType.Builder setRegistryName(final ResourceLocation registryName)
        {
            this.registryName = registryName;
            return this;
        }

        public RaidType createRaidType()
        {
            return new RaidType(raiderId).setRegistryName(registryName);
        }
    }
}
