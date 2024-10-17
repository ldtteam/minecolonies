package com.minecolonies.core.colony.expeditions.colony.types;

import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionEquipmentRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionFoodRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionItemRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for defining colony expedition types.
 */
public class ColonyExpeditionTypeBuilder
{
    /**
     * The id of the expedition.
     */
    private final ResourceLocation id;

    /**
     * The target dimension this expedition would go to.
     */
    private final ResourceKey<Level> dimension;

    /**
     * The loot table to use for rewards generation.
     */
    private final ResourceLocation lootTable;

    /**
     * The list of requirements for this expedition type to be sent.
     */
    private final List<ColonyExpeditionRequirement> requirements = new ArrayList<>();

    /**
     * The name of the expedition, may be a translation string or a fixed text.
     */
    private String name;

    /**
     * The "to text" of the expedition, used as part of the interaction inquiry to give a real quick indication of what to expect from the expedition.
     */
    private String toText;

    /**
     * The difficulty of the expedition.
     */
    private ColonyExpeditionTypeDifficulty difficulty = ColonyExpeditionTypeDifficulty.EASY;

    /**
     * The minimum amount of guards needed for this expedition.
     */
    private int guards = 1;

    /**
     * Create a new expedition type builder.
     *
     * @param id        the id for this expedition type.
     * @param dimension the target dimension.
     * @param lootTable the loot table to generate rewards with.
     */
    public ColonyExpeditionTypeBuilder(final ResourceLocation id, final ResourceKey<Level> dimension, final ResourceLocation lootTable)
    {
        this.id = id;
        this.dimension = dimension;
        this.lootTable = lootTable;
        this.name = "Expedition in the " + StringUtils.capitalize(dimension.location().getPath());
        this.toText = "the " + StringUtils.capitalize(dimension.location().getPath());
    }

    /**
     * Adds an equipment requirement to this expedition type builder, with an amount of 1.
     *
     * @param equipmentType the equipment type.
     * @return the builder for chaining.
     */
    public ColonyExpeditionTypeBuilder addEquipmentRequirement(final EquipmentTypeEntry equipmentType)
    {
        return addEquipmentRequirement(equipmentType, 1);
    }

    /**
     * Adds an equipment requirement to this expedition type builder.
     *
     * @param equipmentType the equipment type.
     * @param amount        the amount needed.
     * @return the builder for chaining.
     */
    public ColonyExpeditionTypeBuilder addEquipmentRequirement(final EquipmentTypeEntry equipmentType, final int amount)
    {
        this.requirements.add(new ColonyExpeditionEquipmentRequirement(equipmentType, amount));
        return this;
    }

    /**
     * Adds a food requirement to this expedition type builder.
     *
     * @param amount the amount needed.
     * @return the builder for chaining.
     */
    public ColonyExpeditionTypeBuilder addFoodRequirement(final int amount)
    {
        this.requirements.add(new ColonyExpeditionFoodRequirement(amount));
        return this;
    }

    /**
     * Adds an item requirement to this expedition type builder, with an amount of 1.
     *
     * @param item the item.
     * @return the builder for chaining.
     */
    public ColonyExpeditionTypeBuilder addItemRequirement(final ItemLike item)
    {
        return addItemRequirement(item, 1);
    }

    /**
     * Adds an item requirement to this expedition type builder.
     *
     * @param item   the item.
     * @param amount the amount needed.
     * @return the builder for chaining.
     */
    public ColonyExpeditionTypeBuilder addItemRequirement(final ItemLike item, final int amount)
    {
        this.requirements.add(new ColonyExpeditionItemRequirement(item.asItem(), amount));
        return this;
    }

    /**
     * Get the id of the expedition.
     *
     * @return the resource id.
     */
    public ResourceLocation getId()
    {
        return id;
    }

    /**
     * The target dimension this expedition would go to.
     *
     * @return the level key.
     */
    public ResourceKey<Level> getDimension()
    {
        return dimension;
    }

    /**
     * Get the loot table to use for rewards generation.
     *
     * @return the resource id.
     */
    public ResourceLocation getLootTable()
    {
        return lootTable;
    }

    /**
     * Get the name of the expedition, may be a translation string or a fixed text.
     *
     * @return the text.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of the expedition, may be a translation string or a fixed text.
     * Defaults to "Expedition in the (capitalized dimension name)".
     *
     * @param name the new name.
     * @return the builder for chaining.
     */
    public ColonyExpeditionTypeBuilder setName(final String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Get the "to text" of the expedition, used as part of the interaction inquiry to give a real quick indication of what to expect from the expedition.
     *
     * @return the text.
     */
    public String getToText()
    {
        return toText;
    }

    /**
     * Set the "to text" of the expedition, used as part of the interaction inquiry to give a real quick indication of what to expect from the expedition.
     * Hence, this could be read as "go to ...".
     * <p>
     * Defaults to "the (capitalized dimension name)".
     *
     * @param toText the new "to text".
     * @return the builder for chaining.
     */
    public ColonyExpeditionTypeBuilder setToText(final String toText)
    {
        this.toText = toText;
        return this;
    }

    /**
     * Get the difficulty of the expedition.
     *
     * @return the difficulty.
     */
    public ColonyExpeditionTypeDifficulty getDifficulty()
    {
        return difficulty;
    }

    /**
     * Set the difficulty for this expedition.
     * Defaults to EASY.
     *
     * @param difficulty the new difficulty.
     * @return the builder for chaining.
     */
    public ColonyExpeditionTypeBuilder setDifficulty(final ColonyExpeditionTypeDifficulty difficulty)
    {
        this.difficulty = difficulty;
        return this;
    }

    /**
     * Get the list of requirements for this expedition type to be sent.
     *
     * @return the list of requirements.
     */
    public List<ColonyExpeditionRequirement> getRequirements()
    {
        return requirements;
    }

    /**
     * Get the minimum amount of guards needed for this expedition.
     *
     * @return the minimum guard count.
     */
    public int getGuards()
    {
        return guards;
    }

    /**
     * Set the amount of guards required for this expedition.
     * Defaults to 1.
     *
     * @param guards the new amount of guards.
     * @return the builder for chaining.
     */
    public ColonyExpeditionTypeBuilder setGuards(final int guards)
    {
        this.guards = guards;
        return this;
    }
}