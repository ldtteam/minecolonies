package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.IItemListModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingPublicCrafter;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.modules.ItemListModule;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingWorkerView;
import com.minecolonies.coremod.colony.jobs.JobLumberjack;
import com.minecolonies.coremod.util.AttributeModifierUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.CitizenConstants.SKILL_BONUS_ADD;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_NETHER_TREE_LIST;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack.SAPLINGS_LIST;

/**
 * The lumberjacks building.
 */
public class BuildingLumberjack extends AbstractBuildingWorker implements IBuildingPublicCrafter
{
    /**
     * Replant setting.
     */
    public static final ISettingKey<BoolSetting> REPLANT = new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "replant"));
    public static final ISettingKey<BoolSetting> RESTRICT = new SettingKey<>(BoolSetting.class, new ResourceLocation(com.minecolonies.api.util.constant.Constants.MOD_ID, "restrict"));

    /**
     * NBT tag for lj restriction start
     */
    private static final String TAG_RESTRICT_START = "startRestrictionPosition";

    /**
     * NBT tag for lj restriction end
     */
    private static final String TAG_RESTRICT_END = "endRestrictionPosition";

    /**
     * The start position of the restricted area.
     */
    private BlockPos startRestriction = null;

    /**
     * The end position of the restricted area.
     */
    private BlockPos endRestriction = null;

    /**
     * The maximum upgrade of the building.
     */
    private static final int    MAX_BUILDING_LEVEL = 5;

    /**
     * The job description.
     */
    private static final String LUMBERJACK         = "lumberjack";

    /**
     * A list of all planted nether trees
     */
    private final Set<BlockPos> netherTrees = new HashSet<>();

    /**
     * Modifier for fungi growing time. Increase to speed up.
     */
    private static final int FUNGI_MODIFIER = 10;

    /**
     * Public constructor of the building, creates an object of the building.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingLumberjack(final IColony c, final BlockPos l)
    {
        super(c, l);

        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.AXE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
        keepX.put(itemStack -> ItemStackUtils.hasToolLevel(itemStack, ToolType.HOE, TOOL_LEVEL_WOOD_OR_GOLD, getMaxToolLevel()), new Tuple<>(1, true));
    }

    @Override
    public boolean canBeGathered()
    {
        // Normal crafters are only gatherable when they have a task, i.e. while producing stuff.
        // BUT, the lumberjack both gathers and crafts things now, so it should always be gatherable.
        // This unfortunately means that the dman will sometimes "steal" ingredients from the LJ.
        // Fortunately, the dman is smart enough to not instantly gather the ingredients it brought to the LJ.
        // Might be improved in the future. For now, it's a bit annoying, but not too bad imho.
        return true;
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(super.getRequiredItemsAndAmount());
        final IItemListModule saplingList = getModuleMatching(ItemListModule.class, m -> m.getId().equals(SAPLINGS_LIST));
        for (final ItemStorage sapling : IColonyManager.getInstance().getCompatibilityManager().getCopyOfSaplings())
        {
            if (!saplingList.isItemInList(sapling))
            {
                toKeep.put(stack -> ItemStackUtils.compareItemStacksIgnoreStackSize(sapling.getItemStack(), stack), new Tuple<>(com.minecolonies.api.util.constant.Constants.STACKSIZE, true));
            }
        }
        return toKeep;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.lumberjack;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return LUMBERJACK;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobLumberjack(citizen);
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);

        if (compound.getAllKeys().contains(TAG_RESTRICT_START))
        {
            startRestriction = NbtUtils.readBlockPos(compound.getCompound(TAG_RESTRICT_START));
        }
        else
        {
            startRestriction = null;
        }

        if (compound.getAllKeys().contains(TAG_RESTRICT_END))
        {
            endRestriction = NbtUtils.readBlockPos(compound.getCompound(TAG_RESTRICT_END));
        }
        else
        {
            endRestriction = null;
        }

        final ListTag netherTreeBinTagList = compound.getList(TAG_NETHER_TREE_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < netherTreeBinTagList.size(); i++)
        {
            netherTrees.add(BlockPosUtil.readFromListNBT(netherTreeBinTagList, i));
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        if (startRestriction != null)
        {
            compound.put(TAG_RESTRICT_START, NbtUtils.writeBlockPos(startRestriction));
        }

        if (endRestriction != null)
        {
            compound.put(TAG_RESTRICT_END, NbtUtils.writeBlockPos(endRestriction));
        }

        @NotNull final ListTag netherTreeBinCompoundList = new ListTag();
        for (@NotNull final BlockPos pos : netherTrees)
        {
            BlockPosUtil.writeToListNBT(netherTreeBinCompoundList, pos);
        }
        compound.put(TAG_NETHER_TREE_LIST, netherTreeBinCompoundList);
        return compound;
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return LUMBERJACK;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Strength;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Focus;
    }

    /**
     * Whether or not the LJ should replant saplings.
     *
     * @return true if so.
     */
    public boolean shouldReplant()
    {
        return getSetting(REPLANT).getValue();
    }

    /**
     * Whether or not the LJ should be restricted.
     *
     * @return true if it should restrict.
     */
    public boolean shouldRestrict()
    {
        if (getSetting(RESTRICT).getValue())
        {
            if (startRestriction == null || endRestriction == null)
            {
                getSetting(RESTRICT).trigger();
                markDirty();
            }
        }
        return getSetting(RESTRICT).getValue();
    }

    public void setRestrictedArea(final BlockPos startPosition, final BlockPos endPosition)
    {
        this.startRestriction = startPosition;
        this.endRestriction = endPosition;
    }

    public BlockPos getStartRestriction()
    {
        return this.startRestriction;
    }

    public BlockPos getEndRestriction()
    {
        return this.endRestriction;
    }

    @Override
    public void removeCitizen(final ICitizenData citizen)
    {
        if (citizen != null)
        {
            final Optional<AbstractEntityCitizen> optCitizen = citizen.getEntity();
            optCitizen.ifPresent(entityCitizen -> AttributeModifierUtils.removeModifier(entityCitizen, SKILL_BONUS_ADD, Attributes.MOVEMENT_SPEED));
        }
        super.removeCitizen(citizen);
    }

    /**
     * Returns early if no worker is assigned Iterates over the nether tree position list If position is a fungus, grows it depending on worker's level If the block has changed,
     * removes the position from the list and returns early If the position is not a fungus, removes the position from the list
     */
    private void bonemealFungi()
    {
        if (getMainCitizen() == null)
        {
            return;
        }
        final int modifier = Math.max(0, Math.min(FUNGI_MODIFIER, 100));
        for (Iterator<BlockPos> iterator = netherTrees.iterator(); iterator.hasNext(); )
        {
            final BlockPos pos = iterator.next();
            final Level world = colony.getWorld();
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockState blockState = world.getBlockState(pos);
                final Block block = blockState.getBlock();
                if (block == Blocks.CRIMSON_FUNGUS || block == Blocks.WARPED_FUNGUS)
                {
                    int threshold = modifier + (int) Math.ceil(getMainCitizen().getCitizenSkillHandler().getLevel(getPrimarySkill()) * (1 - ((float) modifier / 100)));
                    final int rand = world.getRandom().nextInt(100);
                    if (rand < threshold)
                    {
                        final BonemealableBlock growable = (BonemealableBlock) block;
                        if (growable.isValidBonemealTarget(world, pos, blockState, world.isClientSide))
                        {
                            if (!world.isClientSide)
                            {
                                if (growable.isBonemealSuccess(world, world.random, pos, blockState))
                                {
                                    growable.performBonemeal((ServerLevel) world, world.random, pos, blockState);
                                    return;
                                }
                            }
                        }
                    }
                }
                else
                {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Returns a list of the registered nether trees to grow.
     *
     * @return a copy of the list
     */
    public Set<BlockPos> getNetherTrees()
    {
        return new HashSet<>(netherTrees);
    }

    /**
     * Removes a position from the nether trees
     *
     * @param pos the position
     */
    public void removeNetherTree(BlockPos pos)
    {
        netherTrees.remove(pos);
    }

    /**
     * Adds a position to the nether trees
     *
     * @param pos the position
     */
    public void addNetherTree(BlockPos pos)
    {
        netherTrees.add(pos);
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        bonemealFungi();
    }

    @Override
    public Skill getCraftSpeedSkill()
    {
        return getPrimarySkill();
    }

    /**
     * Provides a view of the lumberjack building class.
     */
    public static class View extends AbstractBuildingWorkerView
    {
        /**
         * Public constructor of the view, creates an instance of it.
         *
         * @param c the colony.
         * @param l the position.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, LUMBERJACK);
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Custom
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobLumberjack(null));
        }

        @Override
        public boolean canRecipeBeAdded(@NotNull final IToken<?> token)
        {
            return false;
        }
    }
}
