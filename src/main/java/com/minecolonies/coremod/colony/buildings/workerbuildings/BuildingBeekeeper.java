package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.compatibility.CompatibilityManager;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BeekeeperCollectionSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Building of the beekeeper (apiary).
 */
public class BuildingBeekeeper extends AbstractBuilding
{
    /**
     * The beekeeper mode.
     */
    public static final ISettingKey<BeekeeperCollectionSetting> MODE =
      new SettingKey<>(BeekeeperCollectionSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "beekeeper"));

    /**
     * Both setting options.
     */
    public static final String HONEYCOMB = "com.minecolonies.core.apiary.setting.honeycomb";
    public static final String HONEY     = "com.minecolonies.core.apiary.setting.honey";
    public static final String BOTH      = "com.minecolonies.core.apiary.setting.both";

    /**
     * Description of the job executed in the hut.
     */
    private static final String BEEKEEPER = "beekeeper";

    /**
     * List of hives.
     */
    private Set<BlockPos> hives = new HashSet<>();

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingBeekeeper(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
        keepX.put(stack -> Items.SHEARS == stack.getItem(), new Tuple<>(1, true));
        keepX.put(stack -> Items.GLASS_BOTTLE == stack.getItem(), new Tuple<>(4, true));
        keepX.put(stack -> ItemTags.FLOWERS.contains(stack.getItem()), new Tuple<>(STACKSIZE, true));
    }

    /**
     * Children must return the name of their structure.
     *
     * @return StructureProxy name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BEEKEEPER;
    }

    /**
     * Children must return their max building level.
     *
     * @return Max building level.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        NBTUtils.streamCompound(compound.getList(NbtTagConstants.TAG_HIVES, Constants.NBT.TAG_COMPOUND))
          .map(NBTUtil::readBlockPos)
          .forEach(this.hives::add);
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbt = super.serializeNBT();
        nbt.put(NbtTagConstants.TAG_HIVES, this.hives.stream().map(NBTUtil::writeBlockPos).collect(NBTUtils.toListNBT()));
        return nbt;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
    }

    /**
     * Get the hives/nests positions that belong to this beekeper
     *
     * @return te set of positions of hives/nests that belong to this beekeeper
     */
    public Set<BlockPos> getHives()
    {
        return Collections.unmodifiableSet(new HashSet<>(hives));
    }

    /**
     * Remove a hive/nest position from this beekeper
     *
     * @param pos the position to remove
     */
    public void removeHive(final BlockPos pos)
    {
        hives.remove(pos);
    }

    /**
     * Add a hive/nest position to this beekeper
     *
     * @param pos the position to add
     */
    public void addHive(final BlockPos pos)
    {
        hives.add(pos);
    }

    /**
     * Get what materials the beekeeper should harvest.
     *
     * @return honeycomb, honey bottle, or both
     */
    public String getHarvestTypes()
    {
        return getSetting(MODE).getValue();
    }

    /**
     * Get the maximum amount of hives that can belong to this beekeeper
     *
     * @return the number of maximum hives that can belong to this beekeeper
     */
    public int getMaximumHives()
    {
        return (int) Math.pow(2, getBuildingLevel() - 1);
    }

    /**
     * Bee herding module
     */
    public static class HerdingModule extends AnimalHerdingModule
    {
        // note that the beekeeper is a bit different from regular herders as they never
        // over-breed and kill the bees (bees don't drop any loot anyway).  currently this
        // doesn't matter but if we extend the AnimalHerdingModule with additional AI
        // functionality then this may need special behaviour.

        public HerdingModule()
        {
            super(ModJobs.beekeeper, EntityType.BEE, ItemStack.EMPTY);
        }

        @Override
        public @NotNull List<ItemStack> getBreedingItems()
        {
            if (building != null)
            {
                // todo: if we use this in AI then it should use the item list module settings from the building instead.
            }

            return CompatibilityManager.getAllBeekeeperFlowers().stream()
              .map(flower -> new ItemStack(flower.getItem(), 2))
              .collect(Collectors.toList());
        }

        @Override
        public @NotNull List<LootTableAnalyzer.LootDrop> getExpectedLoot()
        {
            final List<LootTableAnalyzer.LootDrop> drops = new ArrayList<>(super.getExpectedLoot());

            drops.add(new LootTableAnalyzer.LootDrop(Collections.singletonList(new ItemStack(Items.HONEYCOMB, 3)), 1, 0, false));
            drops.add(new LootTableAnalyzer.LootDrop(Collections.singletonList(new ItemStack(Items.HONEY_BOTTLE)), 1, 0, false));

            return drops;
        }
    }
}
