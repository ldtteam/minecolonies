package com.minecolonies.core.colony.expeditions.colony.types;

import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * JSON based class for defining colony expedition types.
 *
 * @param id           The id of the expedition.
 * @param name         The name of the expedition, may be a translation string or a fixed text.
 * @param toText       The "to text" of the expedition, used as part of the interaction inquiry to give a real quick indication of what to expect from the expedition.
 * @param difficulty   The difficulty of the expedition.
 * @param dimension    The target dimension this expedition would go to.
 * @param lootTable    The loot table to use for rewards generation.
 * @param requirements The list of requirements for this expedition type to be sent.
 * @param guards       The minimum amount of guards needed for this expedition.
 */
public record ColonyExpeditionType(ResourceLocation id, Component name, Component toText, ColonyExpeditionTypeDifficulty difficulty, ResourceKey<Level> dimension,
                                   ResourceLocation lootTable, List<ColonyExpeditionRequirement> requirements, int guards)
{}