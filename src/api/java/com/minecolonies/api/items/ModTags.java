package com.minecolonies.api.items;

import com.minecolonies.api.util.constant.TagConstants;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public class ModTags
{
    /**
     * Flag to check if tags are already loaded.
     */
    public static boolean tagsLoaded = false;

    public static final Tags.IOptionalNamedTag<Block> decorationItems = BlockTags.createOptional(TagConstants.DECORATION_ITEMS);
    public static final Tags.IOptionalNamedTag<Item>  concretePowder = ItemTags.createOptional(TagConstants.CONCRETE_POWDER);
    public static final Tags.IOptionalNamedTag<Block> concreteBlock = BlockTags.createOptional(TagConstants.CONCRETE_BLOCK);
    public static final Tags.IOptionalNamedTag<Block> pathingBlocks = BlockTags.createOptional(TagConstants.PATHING_BLOCKS);

    public static final Tags.IOptionalNamedTag<Block> colonyProtectionException = BlockTags.createOptional(TagConstants.COLONYPROTECTIONEXCEPTION);
    public static final Tags.IOptionalNamedTag<Block> indestructible = BlockTags.createOptional(TagConstants.INDESTRUCTIBLE);

    public static final Tags.IOptionalNamedTag<Block> oreChanceBlocks = BlockTags.createOptional(TagConstants.ORECHANCEBLOCKS);

    public static final Tags.IOptionalNamedTag<Item> fungi = ItemTags.createOptional(TagConstants.FUNGI);
    public static final Tags.IOptionalNamedTag<Item> compostables = ItemTags.createOptional(TagConstants.COMPOSTABLES);
    public static final Tags.IOptionalNamedTag<Item> compostables_poor = ItemTags.createOptional(TagConstants.COMPOSTABLES_POOR);
    public static final Tags.IOptionalNamedTag<Item> compostables_rich = ItemTags.createOptional(TagConstants.COMPOSTABLES_RICH);

    public static final Tags.IOptionalNamedTag<Item> meshes = ItemTags.createOptional(TagConstants.MESHES);

    public static final Tags.IOptionalNamedTag<Item> floristFlowers = ItemTags.createOptional(TagConstants.FLORIST_FLOWERS);
    public static final Tags.IOptionalNamedTag<Item> excludedFood = ItemTags.createOptional(TagConstants.EXCLUDED_FOOD);

    public static final Tags.IOptionalNamedTag<EntityType<?>> hostile = EntityTypeTags.createOptional(TagConstants.HOSTILE);
    public static final Tags.IOptionalNamedTag<EntityType<?>> mobAttackBlacklist = EntityTypeTags.createOptional(TagConstants.MOB_ATTACK_BLACKLIST);

    public static final Map<String, Tags.IOptionalNamedTag<Item>> crafterProduct              = new HashMap<>();
    public static final Map<String, Tags.IOptionalNamedTag<Item>> crafterProductExclusions    = new HashMap<>();
    public static final Map<String, Tags.IOptionalNamedTag<Item>> crafterIngredient           = new HashMap<>();
    public static final Map<String, Tags.IOptionalNamedTag<Item>> crafterIngredientExclusions = new HashMap<>();

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

    public static void init()
    {
        initCrafterRules(TagConstants.CRAFTING_BAKER);  // both crafting and smelting
        initCrafterRules(TagConstants.CRAFTING_BLACKSMITH);
        initCrafterRules(TagConstants.CRAFTING_COOK);   // both crafting and smelting
        initCrafterRules(TagConstants.CRAFTING_DYER);
        initCrafterRules(TagConstants.CRAFTING_DYER_SMELTING);
        initCrafterRules(TagConstants.CRAFTING_FARMER);
        initCrafterRules(TagConstants.CRAFTING_FLETCHER);
        initCrafterRules(TagConstants.CRAFTING_GLASSBLOWER);
        initCrafterRules(TagConstants.CRAFTING_GLASSBLOWER_SMELTING);
        initCrafterRules(TagConstants.CRAFTING_MECHANIC);
        initCrafterRules(TagConstants.CRAFTING_PLANTATION);
        initCrafterRules(TagConstants.CRAFTING_SAWMILL);
        initCrafterRules(TagConstants.CRAFTING_STONEMASON);
        initCrafterRules(TagConstants.CRAFTING_STONE_SMELTERY);

        initCrafterRules(TagConstants.CRAFTING_REDUCEABLE);
    }

    /**
     * Initialize the four tags for a particular crafter
     * @param crafterName the string name of the crafter to initialize
     */
    private static void initCrafterRules(@NotNull final String crafterName)
    {
        final ResourceLocation products = new ResourceLocation(MOD_ID, crafterName.concat(PRODUCT));
        final ResourceLocation ingredients = new ResourceLocation(MOD_ID, crafterName.concat(INGREDIENT));
        final ResourceLocation productsExcluded = new ResourceLocation(MOD_ID, crafterName.concat(PRODUCT_EXCLUDED));
        final ResourceLocation ingredientsExcluded = new ResourceLocation(MOD_ID, crafterName.concat(INGREDIENT_EXCLUDED));

        crafterProduct.put(crafterName, ItemTags.createOptional(products));
        crafterProductExclusions.put(crafterName, ItemTags.createOptional(productsExcluded));
        crafterIngredient.put(crafterName, ItemTags.createOptional(ingredients));
        crafterIngredientExclusions.put(crafterName, ItemTags.createOptional(ingredientsExcluded));
    }

    private ModTags()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModTags. This is a utility class");
    }
}
