package com.minecolonies.core.generation.defaults;

import com.google.gson.JsonObject;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.datalistener.RecruitmentItemsListener;
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
        return CompletableFuture.allOf(
            makeRecruitmentItem(cachedOutput, Items.DRIED_KELP, 1),
            makeRecruitmentItem(cachedOutput, Items.BREAD, 1),
            makeRecruitmentItem(cachedOutput, Items.PAPER, 1),
            makeRecruitmentItem(cachedOutput, Items.APPLE, 1),
            makeRecruitmentItem(cachedOutput, Items.BAKED_POTATO, 1),
            makeRecruitmentItem(cachedOutput, Items.SUGAR, 1),
            makeRecruitmentItem(cachedOutput, Items.EGG, 1),

            makeRecruitmentItem(cachedOutput, Items.MUSHROOM_STEW, 2),
            makeRecruitmentItem(cachedOutput, Items.FEATHER, 2),
            makeRecruitmentItem(cachedOutput, Items.FLINT, 2),
            makeRecruitmentItem(cachedOutput, Items.COPPER_INGOT, 2),
            makeRecruitmentItem(cachedOutput, Items.LEATHER, 2),
            makeRecruitmentItem(cachedOutput, Items.COOKED_COD, 2),
            makeRecruitmentItem(cachedOutput, Items.COOKED_SALMON, 2),

            makeRecruitmentItem(cachedOutput, Items.COOKED_CHICKEN, 3),
            makeRecruitmentItem(cachedOutput, Items.COOKED_PORKCHOP, 3),
            makeRecruitmentItem(cachedOutput, Items.COOKED_MUTTON, 3),
            makeRecruitmentItem(cachedOutput, Items.COOKED_BEEF, 3),
            makeRecruitmentItem(cachedOutput, Items.BOOK, 3),

            makeRecruitmentItem(cachedOutput, Items.IRON_INGOT, 4),
            makeRecruitmentItem(cachedOutput, Items.GOLD_INGOT, 4),
            makeRecruitmentItem(cachedOutput, Items.COOKIE, 4),
            makeRecruitmentItem(cachedOutput, Items.REDSTONE, 4),
            makeRecruitmentItem(cachedOutput, Items.LAPIS_LAZULI, 4),
            makeRecruitmentItem(cachedOutput, Items.QUARTZ, 4),
            makeRecruitmentItem(cachedOutput, Items.AMETHYST_CLUSTER, 4),

            makeRecruitmentItem(cachedOutput, Items.HONEYCOMB, 5),
            makeRecruitmentItem(cachedOutput, Items.HONEY_BOTTLE, 5),
            makeRecruitmentItem(cachedOutput, Items.NETHER_WART, 5),
            makeRecruitmentItem(cachedOutput, ModItems.muffin, 5),
            makeRecruitmentItem(cachedOutput, ModItems.veggie_quiche, 5),
            makeRecruitmentItem(cachedOutput, ModItems.pasta_plain, 5),
            makeRecruitmentItem(cachedOutput, ModItems.pottage, 5),

            makeRecruitmentItem(cachedOutput, Items.INK_SAC, 6),
            makeRecruitmentItem(cachedOutput, Items.BLAZE_POWDER, 6),
            makeRecruitmentItem(cachedOutput, Items.SPIDER_EYE, 6),
            makeRecruitmentItem(cachedOutput, Items.SLIME_BALL, 6),

            makeRecruitmentItem(cachedOutput, ModItems.steak_dinner, 7),
            makeRecruitmentItem(cachedOutput, ModItems.hand_pie, 7),
            makeRecruitmentItem(cachedOutput, ModItems.schnitzel, 7),

            makeRecruitmentItem(cachedOutput, Items.DIAMOND, 8),
            makeRecruitmentItem(cachedOutput, Items.EMERALD, 8),
            makeRecruitmentItem(cachedOutput, Items.ENDER_PEARL, 8),
            makeRecruitmentItem(cachedOutput, Items.CAKE, 8),
            makeRecruitmentItem(cachedOutput, ModItems.lamb_stew, 8),
            makeRecruitmentItem(cachedOutput, ModItems.sushi_roll, 8),
            makeRecruitmentItem(cachedOutput, ModItems.eggplant_dolma, 8),
            makeRecruitmentItem(cachedOutput, ModItems.pita_hummus, 8),

            makeRecruitmentItem(cachedOutput, Items.GHAST_TEAR, 9),
            makeRecruitmentItem(cachedOutput, Items.EXPERIENCE_BOTTLE, 9),
            makeRecruitmentItem(cachedOutput, Items.ENCHANTED_BOOK, 9),
            makeRecruitmentItem(cachedOutput, Items.GOLDEN_APPLE, 9));
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
        object.addProperty(RecruitmentItemsListener.KEY_ITEM, itemId.toString());
        object.addProperty(RecruitmentItemsListener.KEY_RARITY, rarity);

        return DataProvider.saveStable(cachedOutput, object, outputProvider.json(new ResourceLocation(Constants.MOD_ID, itemId.getPath())));
    }
}
