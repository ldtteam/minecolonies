package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.core.colony.buildings.modules.settings.BeekeeperCollectionSetting;
import com.minecolonies.core.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
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
        keepX.put(stack -> stack.is(ItemTags.FLOWERS), new Tuple<>(STACKSIZE,true));
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
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        NBTUtils.streamCompound(compound.getList(NbtTagConstants.TAG_HIVES, Tag.TAG_COMPOUND))
          .map(NbtUtils::readBlockPos)
          .forEach(this.hives::add);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag nbt = super.serializeNBT();
        nbt.put(NbtTagConstants.TAG_HIVES, this.hives.stream().map(NbtUtils::writeBlockPos).collect(NBTUtils.toListNBT()));
        return nbt;
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf, final boolean fullSync)
    {
        super.serializeToView(buf, fullSync);

        buf.writeVarInt(hives.size());
        for (final BlockPos hive : hives)
        {
            buf.writeBlockPos(hive);
        }
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (stack.getItem() == Items.HONEY_BOTTLE)
        {
            return false;
        }
        return super.canEat(stack);
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
     * The client side representation of the building.
     */
    public static class View extends AbstractBuildingView
    {
        private Set<BlockPos> hives;

        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        public void deserialize(@NotNull FriendlyByteBuf buf)
        {
            super.deserialize(buf);

            final int hiveCount = buf.readVarInt();
            this.hives = new HashSet<>();
            for (int i = 0; i < hiveCount; ++i)
            {
                this.hives.add(buf.readBlockPos());
            }
        }

        public Set<BlockPos> getHives()
        {
            return Collections.unmodifiableSet(new HashSet<>(hives));
        }
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
            super(ModJobs.beekeeper.get(), a -> a instanceof Bee, ItemStack.EMPTY);
        }

        @NotNull
        @Override
        public List<ItemStack> getBreedingItems()
        {
            if (building != null)
            {
                // todo: if we use this in AI then it should use the item list module settings from the building instead.
            }

            return IColonyManager.getInstance().getCompatibilityManager().getImmutableFlowers().stream()
              .map(flower -> new ItemStack(flower.getItem(), 2))
              .collect(Collectors.toList());
        }

        @NotNull
        @Override
        public List<IGenericRecipe> getRecipesForDisplayPurposesOnly(@NotNull Animal animal)
        {
            final List<IGenericRecipe> recipes = new ArrayList<>(); // we don't kill the bees so don't use the default

            recipes.add(new GenericRecipe(null, new ItemStack(Items.HONEYCOMB),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                    0, Blocks.AIR, null, ModEquipmentTypes.shears.get(), animal.getType(), Collections.emptyList(), 0));

            recipes.add(new GenericRecipe(null, new ItemStack(Items.HONEY_BOTTLE),
                    Collections.emptyList(), Collections.emptyList(),
                    Collections.singletonList(Collections.singletonList(new ItemStack(Items.GLASS_BOTTLE))),
                    0, Blocks.AIR, null, ModEquipmentTypes.none.get(), animal.getType(), Collections.emptyList(), 0));

            return recipes;
        }
    }
}
