package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.compatibility.ICompatibilityManager;
import com.minecolonies.api.crafting.*;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.Suppression.MAGIC_NUMBERS_SHOULD_NOT_BE_USED;
import static com.minecolonies.api.util.constant.Suppression.OVERRIDE_EQUALS;

/**
 * Class of the smeltery building.
 */
@SuppressWarnings(OVERRIDE_EQUALS)
public class BuildingSmeltery extends AbstractBuilding
{
    /**
     * The smelter string.
     */
    private static final String SMELTERY_DESC = "smeltery";

    /**
     * Max building level of the smeltery.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Amount of swords and armor to keep at the worker.
     */
    private static final int STUFF_TO_KEEP = 10;

    /**
     * Instantiates a new smeltery building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSmeltery(final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(IColonyManager.getInstance().getCompatibilityManager()::isOre, new Tuple<>(Integer.MAX_VALUE, true));
        keepX.put(stack -> !ItemStackUtils.isEmpty(stack)
                             && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof DiggerItem || stack.getItem() instanceof ArmorItem)
          , new Tuple<>(STUFF_TO_KEEP, true));
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SMELTERY_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @SuppressWarnings(MAGIC_NUMBERS_SHOULD_NOT_BE_USED)
    public int ingotMultiplier(final int skillLevel, final Random random)
    {
        switch (getBuildingLevel())
        {
            case 1:
                return random.nextInt(ONE_HUNDRED_PERCENT - skillLevel / 2) == 0 ? DOUBLE : 1;
            case 2:
                return random.nextInt(ONE_HUNDRED_PERCENT - skillLevel) == 0 ? DOUBLE : 1;
            case 3:
                return 2;
            case 4:
                return random.nextInt(ONE_HUNDRED_PERCENT - skillLevel / 2) == 0 ? TRIPLE : DOUBLE;
            case 5:
                return random.nextInt(ONE_HUNDRED_PERCENT - skillLevel) == 0 ? TRIPLE : DOUBLE;
            default:
                return 1;
        }
    }

    public static class SmeltingModule extends AbstractCraftingBuildingModule.Smelting
    {
        /**
         * Create a new module.
         *
         * @param jobEntry the entry of the job.
         */
        public SmeltingModule(final JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            // all "recipes" are handled by the AI, and queried via the job
            return false;
        }

        @Override
        public boolean isVisible()
        {
            return false;
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly()
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly());

            final ICompatibilityManager compatibility = IColonyManager.getInstance().getCompatibilityManager();
            for (final ItemStack stack : compatibility.getListOfAllItems())
            {
                if (ItemStackUtils.IS_SMELTABLE.and(compatibility::isOre).and(s -> !s.is(ModTags.breakable_ore)).test(stack))
                {
                    final ItemStack output = FurnaceRecipes.getInstance().getSmeltingResult(stack);
                    recipes.add(createSmeltingRecipe(new ItemStorage(stack), output, Blocks.FURNACE));
                }
            }
            return recipes;
        }

        private static IGenericRecipe createSmeltingRecipe(final ItemStorage input, final ItemStack output, final Block intermediate)
        {
            return GenericRecipe.of(StandardFactoryController.getInstance().getNewInstance(
                    TypeConstants.RECIPE,
                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                    Collections.singletonList(input),
                    1,
                    output,
                    intermediate));
        }
    }

    public static class OreBreakingModule extends AbstractCraftingBuildingModule.Custom
    {
        public OreBreakingModule(JobEntry jobEntry)
        {
            super(jobEntry);
        }

        @NotNull
        @Override
        public List<ResourceLocation> getAdditionalLootTables()
        {
            final List<ResourceLocation> lootTables = new ArrayList<>(super.getAdditionalLootTables());

            //noinspection ConstantConditions
            for (final Item input : ForgeRegistries.ITEMS.tags().getTag(ModTags.breakable_ore))
            {
                lootTables.add(getLootTable(input));
            }

            return lootTables;
        }

        @Override
        protected boolean isPreTaughtRecipe(final IRecipeStorage storage, final Map<ResourceLocation, CustomRecipe> crafterRecipes)
        {
            if (storage.getPrimaryOutput().isEmpty() && storage.getLootTable() != null)
            {
                return true;
            }

            return super.isPreTaughtRecipe(storage, crafterRecipes);
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getAdditionalRecipesForDisplayPurposesOnly()
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(super.getAdditionalRecipesForDisplayPurposesOnly());

            //noinspection ConstantConditions
            for (final Item input : ForgeRegistries.ITEMS.tags().getTag(ModTags.breakable_ore))
            {
                recipes.add(new GenericRecipe(
                        null,                    //recipe
                        ItemStack.EMPTY,            //output
                        Collections.emptyList(),    //additional outputs
                        Collections.singletonList(Collections.singletonList(new ItemStack(input))), //inputs
                        1,                   //grid
                        Blocks.AIR,                 //intermediate
                        getLootTable(input),        //loottable
                        ToolType.PICKAXE,
                        Collections.emptyList(),    //restrictions
                        -1));               //levelsort
            }

            return recipes;
        }

        @Override
        public void checkForWorkerSpecificRecipes()
        {
            super.checkForWorkerSpecificRecipes();

            for (final Item input : ForgeRegistries.ITEMS.tags().getTag(ModTags.breakable_ore))
            {
                Block b = Block.byItem(input);
                List<ItemStack> drops = Block.getDrops(b.defaultBlockState(), (ServerLevel) building.getColony().getWorld(), building.getID(), null);
                for (ItemStack drop : drops)
                {
                    if (!drop.isEmpty())
                    {
                        drop.setCount(1);
                    }
                }

                final RecipeStorage tempRecipe = StandardFactoryController.getInstance().getNewInstance(
                    TypeConstants.RECIPE,
                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                    Collections.singletonList(new ItemStorage(new ItemStack(input))),
                    1,                  //gridsize
                    ItemStack.EMPTY,    //Output
                    null,               //Intermediate
                    null,               //Source
                    null,               //Type
                    null,               //Altoutputs
                    drops,              //SecOutputs
                    getLootTable(input) //Loot Table
                    );
                IToken<?> token = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(tempRecipe);
                this.addRecipeToList(token, false);
            }
        }

        @Override
        public ItemStack getCraftingTool(final AbstractEntityCitizen worker)
        {
            ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
            int fortuneLevel = building.getBuildingLevel() - 1;
            if (fortuneLevel > 0)
            {
                pick.enchant(Enchantments.BLOCK_FORTUNE, fortuneLevel);
            }
            return pick;
        }

        protected ResourceLocation getLootTable(Item item)
        {
            if (item instanceof BlockItem)
            {
                Block itemBlock = Block.byItem(item);
                return itemBlock.getLootTable();
            }
            return null;
        }
    }
}
