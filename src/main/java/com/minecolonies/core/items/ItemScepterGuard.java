package com.minecolonies.core.items;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IGuardBuilding;
import com.minecolonies.api.items.ModDataComponents;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.modules.settings.GuardTaskSetting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.translation.ToolTranslationConstants.*;

/**
 * Guard Scepter Item class. Used to give tasks to guards.
 */
public class ItemScepterGuard extends AbstractItemMinecolonies
{
    /**
     * The compound tag for the last pos the tool has been clicked.
     */
    private static final String TAG_LAST_POS = "lastPos";

    /**
     * GuardScepter constructor. Sets max stack to 1, like other tools.
     *
     * @param properties the properties.
     */
    public ItemScepterGuard(final Item.Properties properties)
    {
        super("scepterguard", properties.stacksTo(1).durability(2));
    }

    @NotNull
    @Override
    public InteractionResult useOn(final UseOnContext ctx)
    {
        // if server world, do nothing
        if (ctx.getLevel().isClientSide)
        {
            return InteractionResult.FAIL;
        }

        final ItemStack scepter = ctx.getPlayer().getItemInHand(ctx.getHand());
        final @Nullable LastPos lastPosComp = scepter.get(ModDataComponents.LAST_POS_COMPONENT);
        if (lastPosComp != null)
        {
            if (lastPosComp.pos.equals(ctx.getClickedPos()))
            {
                ctx.getPlayer().getInventory().removeItemNoUpdate(ctx.getPlayer().getInventory().selected);
                MessageUtils.format(TOOL_GUARD_SCEPTER_ADD_PATROL_TARGETS_FINISHED).sendTo(ctx.getPlayer());
                return InteractionResult.FAIL;
            }
        }
        return handleItemUsage(ctx.getLevel(), ctx.getClickedPos(), scepter, ctx.getPlayer());
    }

    /**
     * Handles the usage of the item.
     *
     * @param worldIn  the world it is used in.
     * @param pos      the position.
     * @param stack    the stack.
     * @param playerIn the player using it.
     * @return if it has been successful.
     */
    @NotNull
    private static InteractionResult handleItemUsage(final Level worldIn, final BlockPos pos, final ItemStack stack, final Player playerIn)
    {
        final @Nullable ModDataComponents.ColonyId colonyIdcomp = stack.get(ModDataComponents.COLONY_ID_COMPONENT);
        final @Nullable ModDataComponents.Pos posComp = stack.get(ModDataComponents.POS_COMPONENT);
        final @Nullable LastPos lastPosComp = stack.get(ModDataComponents.LAST_POS_COMPONENT);
        if (colonyIdcomp == null || posComp == null)
        {
            return InteractionResult.FAIL;
        }
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyIdcomp.id(), colonyIdcomp.dimension());
        if (colony == null)
        {
            return InteractionResult.FAIL;
        }

        final IBuilding hut = colony.getBuildingManager().getBuilding(posComp.pos());
        if (!(hut instanceof AbstractBuildingGuards))
        {
            return InteractionResult.FAIL;
        }
        final IGuardBuilding tower = (IGuardBuilding) hut;

        if (BlockPosUtil.getDistance2D(pos, posComp.pos()) > tower.getPatrolDistance())
        {
            MessageUtils.format(TOOL_GUARD_SCEPTER_TOWER_TOO_FAR).sendTo(playerIn);
            return InteractionResult.FAIL;
        }

        if (hut.getSetting(AbstractBuildingGuards.GUARD_TASK).getValue().equals(GuardTaskSetting.GUARD))
        {
            MessageUtils.format(TOOL_GUARD_SCEPTER_ADD_GUARD_TARGET, pos.toShortString()).sendTo(playerIn);
            tower.setGuardPos(pos);
            playerIn.getInventory().removeItemNoUpdate(playerIn.getInventory().selected);
        }
        else
        {
            if (lastPosComp == null)
            {
                tower.resetPatrolTargets();
            }
            tower.addPatrolTargets(pos);
            MessageUtils.format(TOOL_GUARD_SCEPTER_ADD_PATROL_TARGET, pos.toShortString()).sendTo(playerIn);
        }
        stack.set(ModDataComponents.LAST_POS_COMPONENT, new LastPos(pos));
        return InteractionResult.SUCCESS;
    }

    public record LastPos(BlockPos pos)
    {
        public static       DeferredHolder<DataComponentType<?>, DataComponentType<LastPos>> TYPE  = null;
        public static final LastPos                                                          EMPTY = new LastPos(BlockPos.ZERO);

        public static final Codec<LastPos> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(BlockPos.CODEC.fieldOf("pos").forGetter(LastPos::pos))
                       .apply(builder, LastPos::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, LastPos> STREAM_CODEC =
          StreamCodec.composite(ByteBufCodecs.fromCodec(BlockPos.CODEC),
            LastPos::pos,
            LastPos::new);
    }
}
