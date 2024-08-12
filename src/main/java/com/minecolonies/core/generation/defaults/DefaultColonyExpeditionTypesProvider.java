package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
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

import static com.minecolonies.core.generation.defaults.DefaultExpeditionLootProvider.EXPEDITION_OVERWORLD_LOOT;

/**
 * Generator for expedition types.
 */
public class DefaultColonyExpeditionTypesProvider implements DataProvider
{
    /**
     * Expedition type constants.
     */
    public static final ResourceLocation OVERWORLD_REGULAR = new ResourceLocation(Constants.MOD_ID, "overworld_regular");
    public static final ResourceLocation NETHER_REGULAR    = new ResourceLocation(Constants.MOD_ID, "nether_regular");
    public static final ResourceLocation END_REGULAR       = new ResourceLocation(Constants.MOD_ID, "end_regular");

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

        types.add(new ColonyExpeditionTypeBuilder(OVERWORLD_REGULAR, Level.OVERWORLD, EXPEDITION_OVERWORLD_LOOT)
                    .setName("com.minecolonies.core.expedition_types.overworld.name")
                    .setToText("com.minecolonies.core.expedition_types.overworld.to_text")
                    .addToolRequirement(ToolType.SWORD)
                    .addToolRequirement(ToolType.PICKAXE)
                    .addToolRequirement(ToolType.SHOVEL)
                    .addToolRequirement(ToolType.HELMET)
                    .addToolRequirement(ToolType.CHESTPLATE)
                    .addToolRequirement(ToolType.LEGGINGS)
                    .addToolRequirement(ToolType.BOOTS)
                    .addFoodRequirement(32));

        types.add(new ColonyExpeditionTypeBuilder(NETHER_REGULAR, Level.NETHER, EXPEDITION_OVERWORLD_LOOT)
                    .setName("com.minecolonies.core.expedition_types.nether.name")
                    .setToText("com.minecolonies.core.expedition_types.nether.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.MEDIUM)
                    .addToolRequirement(ToolType.SWORD)
                    .addToolRequirement(ToolType.PICKAXE)
                    .addToolRequirement(ToolType.SHOVEL)
                    .addToolRequirement(ToolType.HELMET)
                    .addToolRequirement(ToolType.CHESTPLATE)
                    .addToolRequirement(ToolType.LEGGINGS)
                    .addToolRequirement(ToolType.BOOTS)
                    .addFoodRequirement(32)
                    .setGuards(2));

        types.add(new ColonyExpeditionTypeBuilder(END_REGULAR, Level.END, EXPEDITION_OVERWORLD_LOOT)
                    .setName("com.minecolonies.core.expedition_types.end.name")
                    .setToText("com.minecolonies.core.expedition_types.end.to_text")
                    .setDifficulty(ColonyExpeditionTypeDifficulty.HARD)
                    .addToolRequirement(ToolType.SWORD)
                    .addToolRequirement(ToolType.PICKAXE)
                    .addToolRequirement(ToolType.SHOVEL)
                    .addToolRequirement(ToolType.HELMET)
                    .addToolRequirement(ToolType.CHESTPLATE)
                    .addToolRequirement(ToolType.LEGGINGS)
                    .addToolRequirement(ToolType.BOOTS)
                    .addFoodRequirement(32)
                    .setGuards(2));

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
