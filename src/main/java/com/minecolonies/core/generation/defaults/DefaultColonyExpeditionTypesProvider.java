package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeBuilder;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.core.generation.defaults.DefaultColonyExpeditionLootProvider.*;

/**
 * Generator for expedition types.
 */
public class DefaultColonyExpeditionTypesProvider implements DataProvider
{
    /**
     * Expedition type constants.
     */
    public static final ResourceLocation OVERWORLD_EASY   = new ResourceLocation(Constants.MOD_ID, "overworld_easy");
    public static final ResourceLocation OVERWORLD_MEDIUM = new ResourceLocation(Constants.MOD_ID, "overworld_medium");
    public static final ResourceLocation OVERWORLD_HARD   = new ResourceLocation(Constants.MOD_ID, "overworld_hard");
    public static final ResourceLocation NETHER_EASY      = new ResourceLocation(Constants.MOD_ID, "nether_easy");
    public static final ResourceLocation NETHER_MEDIUM    = new ResourceLocation(Constants.MOD_ID, "nether_medium");
    public static final ResourceLocation NETHER_HARD      = new ResourceLocation(Constants.MOD_ID, "nether_hard");
    public static final ResourceLocation END_MEDIUM       = new ResourceLocation(Constants.MOD_ID, "end_medium");
    public static final ResourceLocation END_HARD         = new ResourceLocation(Constants.MOD_ID, "end_hard");

    /**
     * The pack output path generator.
     */
    private final PackOutput output;

    /**
     * Default constructor.
     */
    public DefaultColonyExpeditionTypesProvider(final PackOutput output)
    {
        this.output = output;
    }

    /**
     * Generate all expedition type instances.
     *
     * @return the expedition types collection.
     */
    private List<ColonyExpeditionTypeBuilder> generateTypes()
    {
        final List<ColonyExpeditionTypeBuilder> types = new ArrayList<>();

        types.add(new ColonyExpeditionTypeBuilder(OVERWORLD_EASY, Level.OVERWORLD, EXPEDITION_OVERWORLD_LOOT)
                    .setName("com.minecolonies.core.expedition_types.overworld.name")
                    .setToText("com.minecolonies.core.expedition_types.overworld.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.EASY)
                    .addEquipmentRequirement(ModEquipmentTypes.sword.get())
                    .addEquipmentRequirement(ModEquipmentTypes.pickaxe.get())
                    .addEquipmentRequirement(ModEquipmentTypes.shovel.get())
                    .addEquipmentRequirement(ModEquipmentTypes.helmet.get())
                    .addEquipmentRequirement(ModEquipmentTypes.chestplate.get())
                    .addEquipmentRequirement(ModEquipmentTypes.leggings.get())
                    .addEquipmentRequirement(ModEquipmentTypes.boots.get())
                    .addFoodRequirement(32)
                    .setGuards(1));

        types.add(new ColonyExpeditionTypeBuilder(OVERWORLD_MEDIUM, Level.OVERWORLD, EXPEDITION_OVERWORLD_LOOT)
                    .setName("com.minecolonies.core.expedition_types.overworld.name")
                    .setToText("com.minecolonies.core.expedition_types.overworld.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.MEDIUM)
                    .addEquipmentRequirement(ModEquipmentTypes.sword.get())
                    .addEquipmentRequirement(ModEquipmentTypes.pickaxe.get())
                    .addEquipmentRequirement(ModEquipmentTypes.shovel.get())
                    .addEquipmentRequirement(ModEquipmentTypes.helmet.get())
                    .addEquipmentRequirement(ModEquipmentTypes.chestplate.get())
                    .addEquipmentRequirement(ModEquipmentTypes.leggings.get())
                    .addEquipmentRequirement(ModEquipmentTypes.boots.get())
                    .addFoodRequirement(64)
                    .setGuards(2));

        types.add(new ColonyExpeditionTypeBuilder(OVERWORLD_HARD, Level.OVERWORLD, EXPEDITION_OVERWORLD_LOOT)
                    .setName("com.minecolonies.core.expedition_types.overworld.name")
                    .setToText("com.minecolonies.core.expedition_types.overworld.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.HARD)
                    .addEquipmentRequirement(ModEquipmentTypes.sword.get())
                    .addEquipmentRequirement(ModEquipmentTypes.pickaxe.get())
                    .addEquipmentRequirement(ModEquipmentTypes.shovel.get())
                    .addEquipmentRequirement(ModEquipmentTypes.helmet.get())
                    .addEquipmentRequirement(ModEquipmentTypes.chestplate.get())
                    .addEquipmentRequirement(ModEquipmentTypes.leggings.get())
                    .addEquipmentRequirement(ModEquipmentTypes.boots.get())
                    .addFoodRequirement(128)
                    .setGuards(4));

        types.add(new ColonyExpeditionTypeBuilder(NETHER_EASY, Level.NETHER, EXPEDITION_NETHER_LOOT)
                    .setName("com.minecolonies.core.expedition_types.nether.name")
                    .setToText("com.minecolonies.core.expedition_types.nether.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.EASY)
                    .addEquipmentRequirement(ModEquipmentTypes.sword.get())
                    .addEquipmentRequirement(ModEquipmentTypes.pickaxe.get())
                    .addEquipmentRequirement(ModEquipmentTypes.shovel.get())
                    .addEquipmentRequirement(ModEquipmentTypes.helmet.get())
                    .addEquipmentRequirement(ModEquipmentTypes.chestplate.get())
                    .addEquipmentRequirement(ModEquipmentTypes.leggings.get())
                    .addEquipmentRequirement(ModEquipmentTypes.boots.get())
                    .addFoodRequirement(32)
                    .setGuards(2));

        types.add(new ColonyExpeditionTypeBuilder(NETHER_MEDIUM, Level.NETHER, EXPEDITION_NETHER_LOOT)
                    .setName("com.minecolonies.core.expedition_types.nether.name")
                    .setToText("com.minecolonies.core.expedition_types.nether.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.MEDIUM)
                    .addEquipmentRequirement(ModEquipmentTypes.sword.get())
                    .addEquipmentRequirement(ModEquipmentTypes.pickaxe.get())
                    .addEquipmentRequirement(ModEquipmentTypes.shovel.get())
                    .addEquipmentRequirement(ModEquipmentTypes.helmet.get())
                    .addEquipmentRequirement(ModEquipmentTypes.chestplate.get())
                    .addEquipmentRequirement(ModEquipmentTypes.leggings.get())
                    .addEquipmentRequirement(ModEquipmentTypes.boots.get())
                    .addFoodRequirement(64)
                    .setGuards(4));

        types.add(new ColonyExpeditionTypeBuilder(NETHER_HARD, Level.NETHER, EXPEDITION_NETHER_LOOT)
                    .setName("com.minecolonies.core.expedition_types.nether.name")
                    .setToText("com.minecolonies.core.expedition_types.nether.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.HARD)
                    .addEquipmentRequirement(ModEquipmentTypes.sword.get())
                    .addEquipmentRequirement(ModEquipmentTypes.pickaxe.get())
                    .addEquipmentRequirement(ModEquipmentTypes.shovel.get())
                    .addEquipmentRequirement(ModEquipmentTypes.helmet.get())
                    .addEquipmentRequirement(ModEquipmentTypes.chestplate.get())
                    .addEquipmentRequirement(ModEquipmentTypes.leggings.get())
                    .addEquipmentRequirement(ModEquipmentTypes.boots.get())
                    .addFoodRequirement(128)
                    .setGuards(6));

        types.add(new ColonyExpeditionTypeBuilder(END_MEDIUM, Level.END, EXPEDITION_END_LOOT)
                    .setName("com.minecolonies.core.expedition_types.end.name")
                    .setToText("com.minecolonies.core.expedition_types.end.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.MEDIUM)
                    .addEquipmentRequirement(ModEquipmentTypes.sword.get())
                    .addEquipmentRequirement(ModEquipmentTypes.pickaxe.get())
                    .addEquipmentRequirement(ModEquipmentTypes.shovel.get())
                    .addEquipmentRequirement(ModEquipmentTypes.helmet.get())
                    .addEquipmentRequirement(ModEquipmentTypes.chestplate.get())
                    .addEquipmentRequirement(ModEquipmentTypes.leggings.get())
                    .addEquipmentRequirement(ModEquipmentTypes.boots.get())
                    .addFoodRequirement(64)
                    .setGuards(4));

        types.add(new ColonyExpeditionTypeBuilder(END_HARD, Level.END, EXPEDITION_END_LOOT)
                    .setName("com.minecolonies.core.expedition_types.end.name")
                    .setToText("com.minecolonies.core.expedition_types.end.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.HARD)
                    .addEquipmentRequirement(ModEquipmentTypes.sword.get())
                    .addEquipmentRequirement(ModEquipmentTypes.pickaxe.get())
                    .addEquipmentRequirement(ModEquipmentTypes.shovel.get())
                    .addEquipmentRequirement(ModEquipmentTypes.helmet.get())
                    .addEquipmentRequirement(ModEquipmentTypes.chestplate.get())
                    .addEquipmentRequirement(ModEquipmentTypes.leggings.get())
                    .addEquipmentRequirement(ModEquipmentTypes.boots.get())
                    .addFoodRequirement(128)
                    .setGuards(6));

        return types;
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(final @NotNull CachedOutput cachedOutput)
    {
        final PathProvider pathProvider = output.createPathProvider(Target.DATA_PACK, "colony/expedition_types");

        final List<ColonyExpeditionTypeBuilder> colonyExpeditionTypes = generateTypes();
        final CompletableFuture<?>[] futures = new CompletableFuture<?>[colonyExpeditionTypes.size()];
        for (int i = 0; i < colonyExpeditionTypes.size(); i++)
        {
            futures[i] =
              DataProvider.saveStable(cachedOutput, ColonyExpeditionTypeParser.toJson(colonyExpeditionTypes.get(i)), pathProvider.json(colonyExpeditionTypes.get(i).getId()));
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Expedition Types Provider";
    }
}
