package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a new building for the Shepherd.
 */
public class BuildingShepherd extends AbstractBuilding
{
    /**
     * Automatic dyeing.
     */
    public static final ISettingKey<BoolSetting> DYEING = new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "dyeing"));

    /**
     * Automatic shearing.
     */
    public static final ISettingKey<BoolSetting> SHEARING = new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "shearing"));

    /**
     * Description of the job executed in the hut.
     */
    private static final String SHEPHERD = "shepherd";

    /**
     * The hut name, used for the lang string in the GUI
     */
    private static final String HUT_NAME = "shepherdhut";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingShepherd(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SHEPHERD;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (stack.getItem() == Items.WHEAT)
        {
            return false;
        }
        return super.canEat(stack);
    }

    /**
     * Sheep herding module
     */
    public static class HerdingModule extends AnimalHerdingModule
    {
        public HerdingModule()
        {
            super(ModJobs.shepherd.get(), a -> a instanceof Sheep, new ItemStack(Items.WHEAT, 2));
        }

        @Override
        public @NotNull List<LootTableAnalyzer.LootDrop> getExpectedLoot(@NotNull final Animal animal)
        {
            final List<LootTableAnalyzer.LootDrop> drops = new ArrayList<>(super.getExpectedLoot(animal));
            if (animal instanceof Sheep)
            {
                final List<ItemStack> wool = ForgeRegistries.ITEMS.tags().getTag(ItemTags.WOOL).stream()
                        .map(ItemStack::new)
                        .collect(Collectors.toList());
                drops.add(new LootTableAnalyzer.LootDrop(wool, 1, 0, false));
            }
            return drops;
        }

        // we *could* add a custom crafting module to show shears -> wool as well, but it's good
        // enough to show it as a drop on kill (which also happens).
    }
}
