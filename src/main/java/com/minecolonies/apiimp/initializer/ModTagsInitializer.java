package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.Log;

import com.minecolonies.api.util.TagUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModTagsInitializer
{
    /**
     * The name of the tag (crafter) for improving recipes
     */
    private static final String REDUCEABLE = "reduceable";

    /**
     * Tag specifier for Products to Include
     */
    private static final String PRODUCT = "_product";

    /**
     * Tag specifier for Products to Exclude
     */
    private static final String PRODUCT_EXCLUDED = "_product_excluded";

    /**
     * Tag specifier for Ingredients to include
     */
    private static final String INGREDIENT = "_ingredient";

    /**
     * Tag specifier for Ingredients to exclude
     */
    private static final String INGREDIENT_EXCLUDED = "_ingredient_excluded";

    private static final ResourceLocation DECORATION_ITEMS = new ResourceLocation(MOD_ID, "decoblocks");
    private static final ResourceLocation CONCRETE_POWDER = new ResourceLocation(MOD_ID, "concrete_powder");
    private static final ResourceLocation CONCRETE_BLOCK  = new ResourceLocation(MOD_ID, "concrete");
    private static final ResourceLocation PATHING_BLOCKS = new ResourceLocation(MOD_ID, "pathblocks");

    private static boolean loaded = false;

    public static void init()
    {
        if(loaded)
        {
            return;
        }
        loaded = true;

        ModTags.decorationItems = getBlockTags(DECORATION_ITEMS);
        ModTags.concretePowder = getItemTags(CONCRETE_POWDER);
        ModTags.concreteBlock = getBlockTags(CONCRETE_BLOCK);
        ModTags.pathingBlocks = getBlockTags(PATHING_BLOCKS);

        initCrafterRules("baker");
        initCrafterRules("blacksmith");
        initCrafterRules("chickenherder");
        initCrafterRules("concretemixer");
        initCrafterRules("cook");
        initCrafterRules("cowboy");
        initCrafterRules("crusher");
        initCrafterRules("dyer");
        initCrafterRules("farmer");
        initCrafterRules("fisherman");
        initCrafterRules("fletcher");
        initCrafterRules("glassblower");
        initCrafterRules("lumberjack");
        initCrafterRules("mechanic");
        initCrafterRules("plantation");
        initCrafterRules("rabbithutch");
        initCrafterRules("sawmill");
        initCrafterRules("shepherd");
        initCrafterRules("smelter");
        initCrafterRules("stonemason");
        initCrafterRules("stonesmeltery");
        initCrafterRules("swineherder");

        initCrafterRules(REDUCEABLE);
        Log.getLogger().info("Tags Loaded");
    }

    /**
     * Initialize the four tags for a particular crafter
     * @param crafterName the string name of the crafter to initialize
     */
    private static void initCrafterRules(final String crafterName)
    {
        final String lowerName = crafterName.toLowerCase();
        final ResourceLocation products = new ResourceLocation(MOD_ID, lowerName.concat(PRODUCT));
        final ResourceLocation ingredients = new ResourceLocation(MOD_ID, lowerName.concat(INGREDIENT));
        final ResourceLocation productsExcluded = new ResourceLocation(MOD_ID, lowerName.concat(PRODUCT_EXCLUDED));
        final ResourceLocation ingredientsExcluded = new ResourceLocation(MOD_ID, lowerName.concat(INGREDIENT_EXCLUDED));


        ModTags.crafterProduct.put(lowerName, getItemTags(products));
        ModTags.crafterProductExclusions.put(lowerName, getItemTags(productsExcluded));
        ModTags.crafterIngredient.put(lowerName, getItemTags(ingredients));
        ModTags.crafterIngredientExclusions.put(lowerName, getItemTags(ingredientsExcluded));
    }

    /**
     * Get the Tag<Item> from the underlying API
     * @param resourceLocation The resource location specifying the tag ID
     * @return the tag collection
     */
    private static ITag<Item> getItemTags(final ResourceLocation resourceLocation)
    {
        return TagUtils.getItem(resourceLocation).get();
    }

    /**
     * Get the Tag<Block> from the underlying API
     * @param resourceLocation The resource location specifying the tag ID
     * @return the tag collection
     */
    private static ITag<Block> getBlockTags(final ResourceLocation resourceLocation)
    {
        return TagUtils.getBlock(resourceLocation).get();
    }
}
