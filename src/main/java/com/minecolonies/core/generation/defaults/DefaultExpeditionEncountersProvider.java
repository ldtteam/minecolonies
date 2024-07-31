package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounter;
import com.minecolonies.core.colony.expeditions.encounters.ExpeditionEncounterParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generator for expedition encounters.
 */
public class DefaultExpeditionEncountersProvider implements DataProvider
{
    /**
     * Expedition encounter constants.
     */
    public static final ResourceLocation ZOMBIE            = new ResourceLocation(Constants.MOD_ID, "zombie");
    public static final ResourceLocation SKELETON          = new ResourceLocation(Constants.MOD_ID, "skeleton");
    public static final ResourceLocation CREEPER           = new ResourceLocation(Constants.MOD_ID, "creeper");
    public static final ResourceLocation SPIDER            = new ResourceLocation(Constants.MOD_ID, "spider");
    public static final ResourceLocation CAVE_SPIDER       = new ResourceLocation(Constants.MOD_ID, "cave_spider");
    public static final ResourceLocation ENDERMAN          = new ResourceLocation(Constants.MOD_ID, "enderman");
    public static final ResourceLocation SHULKER           = new ResourceLocation(Constants.MOD_ID, "shulker");
    public static final ResourceLocation PIGLIN            = new ResourceLocation(Constants.MOD_ID, "piglin");
    public static final ResourceLocation PIGLIN_BRUTE      = new ResourceLocation(Constants.MOD_ID, "piglin_brute");
    public static final ResourceLocation HOGLIN            = new ResourceLocation(Constants.MOD_ID, "hoglin");
    public static final ResourceLocation DROWNED           = new ResourceLocation(Constants.MOD_ID, "drowned");
    public static final ResourceLocation DROWNED_TRIDENT   = new ResourceLocation(Constants.MOD_ID, "drowned_trident");
    public static final ResourceLocation BLAZE             = new ResourceLocation(Constants.MOD_ID, "blaze");
    public static final ResourceLocation MAGMA_CUBE_SMALL  = new ResourceLocation(Constants.MOD_ID, "magma_cube_small");
    public static final ResourceLocation MAGMA_CUBE_MEDIUM = new ResourceLocation(Constants.MOD_ID, "magma_cube_medium");
    public static final ResourceLocation MAGMA_CUBE_LARGE  = new ResourceLocation(Constants.MOD_ID, "magma_cube_large");
    public static final ResourceLocation WITHER_SKELETON   = new ResourceLocation(Constants.MOD_ID, "wither_skeleton");
    public static final ResourceLocation ZOMBIFIED_PIGLIN  = new ResourceLocation(Constants.MOD_ID, "zombified_piglin");
    public static final ResourceLocation VINDICATOR        = new ResourceLocation(Constants.MOD_ID, "vindicator");
    public static final ResourceLocation EVOKER            = new ResourceLocation(Constants.MOD_ID, "evoker");
    public static final ResourceLocation VEX               = new ResourceLocation(Constants.MOD_ID, "vex");
    public static final ResourceLocation GUARDIAN          = new ResourceLocation(Constants.MOD_ID, "guardian");
    public static final ResourceLocation ELDER_GUARDIAN    = new ResourceLocation(Constants.MOD_ID, "elder_guardian");
    public static final ResourceLocation PILLAGER          = new ResourceLocation(Constants.MOD_ID, "pillager");
    public static final ResourceLocation PILLAGER_CAPTAIN  = new ResourceLocation(Constants.MOD_ID, "pillager_captain");

    /**
     * Boss encounter constants.
     */
    public static final ResourceLocation WARDEN = new ResourceLocation(Constants.MOD_ID, "warden");

    /**
     * The pack output path generator.
     */
    private final PackOutput output;

    /**
     * Default constructor.
     */
    public DefaultExpeditionEncountersProvider(final PackOutput output)
    {
        this.output = output;
    }

    /**
     * Generate all expedition encounter instances.
     *
     * @return the expedition encounters collection.
     */
    private List<ExpeditionEncounter> generateTypes()
    {
        final List<ExpeditionEncounter> types = new ArrayList<>();

        // Standard mobs
        types.add(new ExpeditionEncounter(ZOMBIE, EntityType.ZOMBIE, 2.5f, 0, 20, 0, EntityType.ZOMBIE.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(SKELETON, EntityType.SKELETON, 3f, 0, 20, 0, EntityType.SKELETON.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(CREEPER, EntityType.CREEPER, 22f, 20, 20, 0, EntityType.CREEPER.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(SPIDER, EntityType.SPIDER, 2f, 0, 16, 0, EntityType.SPIDER.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(CAVE_SPIDER, EntityType.CAVE_SPIDER, 4f, 0, 12, 0, EntityType.CAVE_SPIDER.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(ENDERMAN, EntityType.ENDERMAN, 7f, 0, 40, 0, EntityType.ENDERMAN.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(SHULKER, EntityType.SHULKER, 4f, 0, 30, 0, EntityType.SHULKER.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(PIGLIN, EntityType.PIGLIN, 8f, 0, 16, 0, EntityType.PIGLIN.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(PIGLIN_BRUTE, EntityType.PIGLIN_BRUTE, 13f, 0, 50, 0, EntityType.PIGLIN_BRUTE.getDefaultLootTable(), 20));
        types.add(new ExpeditionEncounter(HOGLIN, EntityType.HOGLIN, 8f, 0, 40, 0, EntityType.HOGLIN.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(DROWNED, EntityType.DROWNED, 3f, 0, 20, 0, EntityType.DROWNED.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(DROWNED_TRIDENT, EntityType.DROWNED, 11f, 0, 20, 0, EntityType.DROWNED.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(BLAZE, EntityType.BLAZE, 6f, 0, 20, 0, EntityType.BLAZE.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(MAGMA_CUBE_SMALL, EntityType.MAGMA_CUBE, 3f, 0, 1, 0, EntityType.MAGMA_CUBE.getDefaultLootTable(), 1));
        types.add(new ExpeditionEncounter(MAGMA_CUBE_MEDIUM, EntityType.MAGMA_CUBE, 4f, 0, 4, 0, EntityType.MAGMA_CUBE.getDefaultLootTable(), 2));
        types.add(new ExpeditionEncounter(MAGMA_CUBE_LARGE, EntityType.MAGMA_CUBE, 6f, 0, 16, 0, EntityType.MAGMA_CUBE.getDefaultLootTable(), 4));
        types.add(new ExpeditionEncounter(WITHER_SKELETON, EntityType.WITHER_SKELETON, 11f, 0, 20, 0, EntityType.WITHER_SKELETON.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(ZOMBIFIED_PIGLIN, EntityType.ZOMBIFIED_PIGLIN, 11f, 0, 20, 0, EntityType.ZOMBIFIED_PIGLIN.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(VINDICATOR, EntityType.VINDICATOR, 13f, 0, 24, 0, EntityType.VINDICATOR.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(EVOKER, EntityType.EVOKER, 6f, 0, 24, 0, EntityType.EVOKER.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(VEX, EntityType.VEX, 9f, 0, 14, 0, EntityType.VEX.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(GUARDIAN, EntityType.GUARDIAN, 6f, 0, 30, 0, EntityType.GUARDIAN.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(ELDER_GUARDIAN, EntityType.ELDER_GUARDIAN, 8f, 0, 80, 0, EntityType.ELDER_GUARDIAN.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(PILLAGER, EntityType.PILLAGER, 8f, 0, 80, 0, EntityType.PILLAGER.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(PILLAGER_CAPTAIN, EntityType.PILLAGER, 8f, 0, 80, 0, EntityType.PILLAGER.getDefaultLootTable(), 10));

        // Boss mobs
        types.add(new ExpeditionEncounter(WARDEN, EntityType.WARDEN, 30f, 0, 500, 0, EntityType.WARDEN.getDefaultLootTable(), 5));

        return types;
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(final @NotNull CachedOutput cachedOutput)
    {
        final PathProvider pathProvider = output.createPathProvider(Target.DATA_PACK, "colony/expedition_encounters");

        final List<ExpeditionEncounter> expeditionEncounters = generateTypes();
        final CompletableFuture<?>[] futures = new CompletableFuture<?>[expeditionEncounters.size()];
        for (int i = 0; i < expeditionEncounters.size(); i++)
        {
            futures[i] =
              DataProvider.saveStable(cachedOutput, ExpeditionEncounterParser.toJson(expeditionEncounters.get(i)), pathProvider.json(expeditionEncounters.get(i).getId()));
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Expedition Encounters Provider";
    }
}
