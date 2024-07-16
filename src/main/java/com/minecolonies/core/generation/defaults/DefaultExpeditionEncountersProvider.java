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
    public static final ResourceLocation ZOMBIE   = new ResourceLocation(Constants.MOD_ID, "zombie");
    public static final ResourceLocation SKELETON = new ResourceLocation(Constants.MOD_ID, "skeleton");
    public static final ResourceLocation CREEPER  = new ResourceLocation(Constants.MOD_ID, "creeper");
    public static final ResourceLocation ENDERMAN = new ResourceLocation(Constants.MOD_ID, "enderman");
    public static final ResourceLocation WARDEN   = new ResourceLocation(Constants.MOD_ID, "warden");

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

        types.add(new ExpeditionEncounter(ZOMBIE, EntityType.ZOMBIE, 2.5f, 0, 20, 0, EntityType.ZOMBIE.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(SKELETON, EntityType.SKELETON, 3f, 0, 20, 0, EntityType.SKELETON.getDefaultLootTable(), 10));
        types.add(new ExpeditionEncounter(CREEPER, EntityType.CREEPER, 22f, 20, 20, 0, EntityType.CREEPER.getDefaultLootTable(), 5));
        types.add(new ExpeditionEncounter(ENDERMAN, EntityType.ENDERMAN, 7f, 0, 40, 0, EntityType.ENDERMAN.getDefaultLootTable(), 5));
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
