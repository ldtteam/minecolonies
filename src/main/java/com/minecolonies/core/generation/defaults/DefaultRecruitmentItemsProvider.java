package com.minecolonies.core.generation.defaults;

import com.google.gson.JsonObject;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.minecolonies.core.generation.DataGeneratorConstants.COLONY_RECRUITMENT_ITEMS_DIR;

public class DefaultRecruitmentItemsProvider implements DataProvider
{
    private final PackOutput.PathProvider outputProvider;

    public DefaultRecruitmentItemsProvider(@NotNull final PackOutput packOutput)
    {
        this.outputProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, COLONY_RECRUITMENT_ITEMS_DIR);
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(final @NotNull CachedOutput cachedOutput)
    {
        return CompletableFuture.allOf(makeRecruitmentItem(cachedOutput, Items.BAKED_POTATO, 1),
            makeRecruitmentItem(cachedOutput, Items.COOKED_BEEF, 1),
            makeRecruitmentItem(cachedOutput, Items.COOKED_CHICKEN, 1),
            makeRecruitmentItem(cachedOutput, Items.COOKED_MUTTON, 1),
            makeRecruitmentItem(cachedOutput, Items.COOKED_PORKCHOP, 1),
            makeRecruitmentItem(cachedOutput, Items.PAPER, 1),
            makeRecruitmentItem(cachedOutput, Items.LEATHER, 1),
            makeRecruitmentItem(cachedOutput, Items.COOKED_COD, 2),
            makeRecruitmentItem(cachedOutput, Items.COOKED_SALMON, 2),
            makeRecruitmentItem(cachedOutput, Items.BOOK, 2),
            makeRecruitmentItem(cachedOutput, Items.COOKIE, 3),
            makeRecruitmentItem(cachedOutput, Items.MUSHROOM_STEW, 3),
            makeRecruitmentItem(cachedOutput, Items.HONEYCOMB, 3),
            makeRecruitmentItem(cachedOutput, Items.HONEY_BOTTLE, 3),
            makeRecruitmentItem(cachedOutput, Items.CAKE, 4),
            makeRecruitmentItem(cachedOutput, Items.PUMPKIN_PIE, 4),
            makeRecruitmentItem(cachedOutput, Items.RAW_COPPER, 4),
            makeRecruitmentItem(cachedOutput, Items.RAW_IRON, 4),
            makeRecruitmentItem(cachedOutput, Items.RAW_GOLD, 4),
            makeRecruitmentItem(cachedOutput, Items.CAKE, 5),
            makeRecruitmentItem(cachedOutput, ModItems.muffin, 5),
            makeRecruitmentItem(cachedOutput, ModItems.veggie_quiche, 5),
            makeRecruitmentItem(cachedOutput, ModItems.pasta_plain, 5),
            makeRecruitmentItem(cachedOutput, ModItems.pottage, 5),
            makeRecruitmentItem(cachedOutput, Items.COPPER_INGOT, 5),
            makeRecruitmentItem(cachedOutput, Items.IRON_INGOT, 5),
            makeRecruitmentItem(cachedOutput, Items.GOLD_INGOT, 5),
            makeRecruitmentItem(cachedOutput, ModItems.steak_dinner, 6),
            makeRecruitmentItem(cachedOutput, ModItems.hand_pie, 6),
            makeRecruitmentItem(cachedOutput, ModItems.schnitzel, 6),
            makeRecruitmentItem(cachedOutput, Items.REDSTONE, 6),
            makeRecruitmentItem(cachedOutput, Items.LAPIS_LAZULI, 6),
            makeRecruitmentItem(cachedOutput, ModItems.lamb_stew, 8),
            makeRecruitmentItem(cachedOutput, ModItems.sushi_roll, 8),
            makeRecruitmentItem(cachedOutput, ModItems.eggplant_dolma, 8),
            makeRecruitmentItem(cachedOutput, ModItems.pita_hummus, 8),
            makeRecruitmentItem(cachedOutput, Items.DIAMOND, 8),
            makeRecruitmentItem(cachedOutput, Items.EMERALD, 8),
            makeRecruitmentItem(cachedOutput, Items.ENCHANTED_BOOK, 9));
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Default Recruitment Items Provider";
    }

    private CompletableFuture<?> makeRecruitmentItem(final CachedOutput cachedOutput, final Item item, final int rarity)
    {
        final ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        final JsonObject object = new JsonObject();
        object.addProperty("item", itemId.toString());
        object.addProperty("rarity", rarity);

        return DataProvider.saveStable(cachedOutput, object, outputProvider.json(new ResourceLocation(Constants.MOD_ID, itemId.getPath())));
    }
}
