package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.generation.SimpleLootTableProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

import static com.minecolonies.api.loot.ModLootConditions.EXPEDITION_PARAMS;
import static com.minecolonies.core.generation.ExpeditionResourceManager.createStructureLootReference;
import static com.minecolonies.core.generation.defaults.DefaultExpeditionStructureLootProvider.ANCIENT_CITY_ID;

/**
 * Loot table generator for expeditions.
 */
public class DefaultColonyExpeditionLootProvider extends SimpleLootTableProvider
{
    /**
     * Expedition constants.
     */
    public static final ResourceLocation EXPEDITION_OVERWORLD_LOOT = new ResourceLocation(Constants.MOD_ID, "expeditions/expedition_overworld");
    public static final ResourceLocation EXPEDITION_NETHER_LOOT    = new ResourceLocation(Constants.MOD_ID, "expeditions/expedition_nether");
    public static final ResourceLocation EXPEDITION_END_LOOT       = new ResourceLocation(Constants.MOD_ID, "expeditions/expedition_end");

    /**
     * Default constructor.
     */
    public DefaultColonyExpeditionLootProvider(final PackOutput output)
    {
        super(output);
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Expedition Loot";
    }

    @Override
    protected void registerTables(final @NotNull LootTableRegistrar registrar)
    {
        createExpeditionLootTable(EXPEDITION_OVERWORLD_LOOT, registrar, builder -> builder.withPool(
          new LootPool.Builder()
            .setRolls(UniformGenerator.between(5, 8))
            .add(LootItem.lootTableItem(Items.RED_MUSHROOM))
            .add(createStructureLootReference(ANCIENT_CITY_ID))
        ));
    }

    @Override
    protected void validate(final @NotNull Map<ResourceLocation, LootTable> map, final @NotNull ValidationContext validationtracker)
    {
        ValidationContext newTracker = new ValidationContext(EXPEDITION_PARAMS, new LootDataResolver()
        {
            @Nullable
            public <T> T getElement(@NotNull LootDataId<T> id)
            {
                if (id.location().getPath().startsWith("expeditions/structures"))
                {
                    return (T) map.get(id.location());
                }
                return null;
            }
        });

        super.validate(map, newTracker);
    }

    /**
     * Simple builder to automatically build an expedition loot table.
     *
     * @param id        the id of the expedition.
     * @param registrar the loot table registrar.
     * @param configure the further configuration handler.
     */
    private void createExpeditionLootTable(final ResourceLocation id, final @NotNull LootTableRegistrar registrar, final Consumer<Builder> configure)
    {
        final Builder builder = new Builder();
        configure.accept(builder);

        registrar.register(id, EXPEDITION_PARAMS, builder);
    }
}
