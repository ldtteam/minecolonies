package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.items.IBlockOverlayItem;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.core.entity.ai.workers.production.EntityAIWorkLumberjack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Lumberjack Scepter Item class. Used to give tasks to Lumberjacks.
 */
public class ItemScepterLumberjack extends AbstractItemMinecolonies implements IBlockOverlayItem
{
    private static final int RED_OVERLAY = 0xFFFF0000;
    private static final int GREEN_OVERLAY = 0xFF00FF00;
    private static final String NBT_START_POS = Constants.MOD_ID + ":" + "start_pos";
    private static final String NBT_END_POS   = Constants.MOD_ID + ":" + "end_pos";

    /**
     * LumberjackScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemScepterLumberjack(final Properties properties)
    {
        super("scepterlumberjack", properties.stacksTo(1));
    }

    @NotNull
    @Override
    public InteractionResult useOn(final UseOnContext context)
    {
        if (context.getLevel().isClientSide)
        {
            return InteractionResult.FAIL;
        }

        final ItemStack scepter = context.getPlayer().getItemInHand(context.getHand());
        MessageUtils.format(TOOL_LUMBERJACK_SCEPTER_POSITION_B_SET).sendTo(context.getPlayer());
        BlockPosUtil.write(scepter.getOrCreateTag(), NBT_START_POS, context.getClickedPos());
        storeRestrictedArea(context.getPlayer(), scepter.getOrCreateTag(), context.getLevel());
        return InteractionResult.FAIL;
    }

    @Override
    public boolean canAttackBlock(@NotNull final BlockState state, @NotNull final Level world, @NotNull final BlockPos pos, @NotNull final Player player)
    {
        if (!world.isClientSide)
        {
            final ItemStack tool = player.getMainHandItem();
            MessageUtils.format(TOOL_LUMBERJACK_SCEPTER_POSITION_A_SET).sendTo(player);
            BlockPosUtil.write(tool.getOrCreateTag(), NBT_END_POS, pos);
            storeRestrictedArea(player, tool.getOrCreateTag(), world);
        }

        return false;
    }

    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 3.4028235E38F;
    }

    private void storeRestrictedArea(final Player player, final CompoundTag compound, final Level worldIn)
    {
        final Box box = getBox(worldIn, compound);

        if (box.anchor() == null || box.corners() == null)
        {
            return;
        }
        assert box.corners().getA() != null && box.corners().getB() != null;

        // Check restricted area isn't too large
        final int minX = Math.min(box.corners().getA().getX(), box.corners().getB().getX());
        final int minY = Math.min(box.corners().getA().getY(), box.corners().getB().getY());
        final int minZ = Math.min(box.corners().getA().getZ(), box.corners().getB().getZ());
        final int maxX = Math.max(box.corners().getA().getX(), box.corners().getB().getX());
        final int maxY = Math.max(box.corners().getA().getY(), box.corners().getB().getY());
        final int maxZ = Math.max(box.corners().getA().getZ(), box.corners().getB().getZ());

        final int distX = maxX - minX;
        final int distY = maxY - minY;
        final int distZ = maxZ - minZ;

        final int volume = distX * distY * distZ;
        final int maxVolume = (int) Math.floor(2 * Math.pow(EntityAIWorkLumberjack.SEARCH_RANGE, 3));

        if (volume > maxVolume)
        {
            MessageUtils.format(TOOL_LUMBERJACK_SCEPTER_AREA_TOO_BIG, volume, maxVolume).sendTo(player);
            return;
        }

        MessageUtils.format(TOOL_LUMBERJACK_SCEPTER_AREA_SET, minX, maxX, minY, maxY, minZ, maxZ, volume, maxVolume).sendTo(player);
        final IColony colony = IColonyManager.getInstance().getColonyByWorld(compound.getInt(TAG_ID), worldIn);
        final BuildingLumberjack hut = colony.getBuildingManager().getBuilding(box.anchor(), BuildingLumberjack.class);
        if (hut == null)
        {
            return;
        }

        hut.setRestrictedArea(box.corners().getA(), box.corners().getB());
    }

    @NotNull
    @Override
    public List<OverlayBox> getOverlayBoxes(@NotNull final Level world, @NotNull final Player player, @NotNull ItemStack stack)
    {
        final Box box = getBox(world, stack.getOrCreateTag());

        if (box.anchor() != null)
        {
            final OverlayBox anchorBox = new OverlayBox(new AABB(box.anchor()), RED_OVERLAY, 0.02f, true);

            if (box.corners() != null)
            {
                assert box.corners().getA() != null && box.corners().getB() != null;
                final AABB bounds = AABB.encapsulatingFullBlocks(box.corners().getA(), box.corners().getB().offset(1, 1, 1)).inflate(1);
                // inflate(1) is due to implementation of BlockPosUtil.isInArea

                return List.of(anchorBox, new OverlayBox(bounds, GREEN_OVERLAY, 0.02f, true));
            }

            return Collections.singletonList(anchorBox);
        }

        return Collections.emptyList();
    }

    private record Box(@Nullable BlockPos anchor, @Nullable Tuple<BlockPos, BlockPos> corners) { }

    @NotNull
    private Box getBox(@NotNull final Level world, final CompoundTag compound)
    {
        final int colonyId = compound.getInt(TAG_ID);
        final BlockPos pos = BlockPosUtil.read(compound, TAG_POS);
        final BlockPos start = compound.contains(NBT_START_POS) ? BlockPosUtil.read(compound, NBT_START_POS) : null;
        final BlockPos end = compound.contains(NBT_END_POS) ? BlockPosUtil.read(compound, NBT_END_POS) : null;

        if (world.isClientSide())
        {
            return getBox(world, colonyId, pos, start, end);
        }

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, world);
        if (colony != null && colony.getBuildingManager().getBuilding(pos) instanceof final BuildingLumberjack hut)
        {
            final BlockPos startRestriction = start != null ? start : Objects.requireNonNullElse(hut.getStartRestriction(), BlockPos.ZERO);
            final BlockPos endRestriction = end != null ? end : Objects.requireNonNullElse(hut.getEndRestriction(), BlockPos.ZERO);
            if (!startRestriction.equals(BlockPos.ZERO) && !endRestriction.equals(BlockPos.ZERO))
            {
                return new Box(pos, new Tuple<>(startRestriction, endRestriction));
            }
            return new Box(pos, null);
        }

        return new Box(null, null);
    }

    @NotNull
    private Box getBox(@NotNull final Level world, final int colonyId, @NotNull final BlockPos pos,
                       @Nullable final BlockPos start, @Nullable final BlockPos end)
    {
        final IColonyView colony = IColonyManager.getInstance().getColonyView(colonyId, world.dimension());

        if (colony != null && colony.getBuilding(pos) instanceof final BuildingLumberjack.View hut)
        {
            final BlockPos startRestriction = start != null ? start : hut.getStartRestriction();
            final BlockPos endRestriction = end != null ? end : hut.getEndRestriction();
            if (!startRestriction.equals(BlockPos.ZERO) && !endRestriction.equals(BlockPos.ZERO))
            {
                return new Box(pos, new Tuple<>(startRestriction, endRestriction));
            }
            return new Box(pos, null);
        }

        return new Box(null, null);
    }
}
