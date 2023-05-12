package com.minecolonies.coremod.items;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.tileentities.TileEntityWareHouse;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Class describing the resource scroll item.
 */
public class ItemResourceScroll extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the resource scroll item.
     *
     * @param properties the properties.
     */
    public ItemResourceScroll(final Item.Properties properties)
    {
        super("resourcescroll", properties.stacksTo(STACKSIZE).tab(ModCreativeTabs.MINECOLONIES));
    }

    /**
     * Used when clicking on block in world.
     *
     * @param ctx the context of use.
     * @return the result
     */
    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext ctx)
    {
        final ItemStack scroll = ctx.getPlayer().getItemInHand(ctx.getHand());

        final CompoundTag compound = checkForCompound(scroll);
        BlockEntity entity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

        if (entity instanceof TileEntityColonyBuilding)
        {
            final AbstractTileEntityColonyBuilding buildingEntity = (AbstractTileEntityColonyBuilding) entity;

            if (buildingEntity.getBuilding() instanceof BuildingBuilder)
            {
                compound.putInt(TAG_COLONY_ID, buildingEntity.getColonyId());
                BlockPosUtil.write(compound, TAG_BUILDER, buildingEntity.getPosition());

                if (!ctx.getLevel().isClientSide)
                {
                    MessageUtils.format(COM_MINECOLONIES_SCROLL_BUILDING_SET, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
                }
            }
            else if (buildingEntity instanceof TileEntityWareHouse)
            {
                if (ctx.getLevel().isClientSide)
                {
                    openWindow(compound, ctx.getPlayer(), buildingEntity.getPosition());
                }
            }
            else
            {
                if (!ctx.getLevel().isClientSide)
                {
                    final MutableComponent buildingTypeComponent = MessageUtils.format(buildingEntity.getBuilding().getBuildingType().getTranslationKey()).create();
                    MessageUtils.format(COM_MINECOLONIES_SCROLL_WRONG_BUILDING, buildingTypeComponent, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
                }
            }
        }
        else if (ctx.getLevel().isClientSide)
        {
            openWindow(compound, ctx.getPlayer(), null);
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Handles mid air use.
     *
     * @param worldIn  the world
     * @param playerIn the player
     * @param hand     the hand
     * @return the result
     */
    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(
      final Level worldIn,
      final Player playerIn,
      final InteractionHand hand)
    {
        final ItemStack clipboard = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide)
        {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, clipboard);
        }

        openWindow(checkForCompound(clipboard), playerIn, null);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, clipboard);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (worldIn == null)
        {
            return;
        }

        final CompoundTag compound = checkForCompound(stack);
        final int colonyId = compound.getInt(TAG_COLONY_ID);
        final BlockPos builderPos = BlockPosUtil.read(compound, TAG_BUILDER);

        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyId, worldIn.dimension());
        if (colonyView != null)
        {
            final IBuildingView buildingView = colonyView.getBuilding(builderPos);
            if (buildingView instanceof BuildingBuilder.View)
            {
                String name = ((BuildingBuilder.View) buildingView).getWorkerName();
                tooltip.add(name != null && !name.trim().isEmpty()
                              ? Component.literal(ChatFormatting.DARK_PURPLE + name)
                              : Component.translatable(COM_MINECOLONIES_SCROLL_BUILDING_NO_WORKER));
            }
        }
    }

    /**
     * Check for the compound and return it. If not available create and return it.
     *
     * @param item the item to check in for.
     * @return the compound of the item.
     */
    private static CompoundTag checkForCompound(final ItemStack item)
    {
        if (!item.hasTag())
        {
            item.setTag(new CompoundTag());
        }
        return item.getTag();
    }

    /**
     * Opens the scroll window if there is a valid builder linked
     *
     * @param compound the item compound
     * @param player   the player entity opening the window
     */
    private static void openWindow(CompoundTag compound, Player player, BlockPos warehousePos)
    {
        if (compound.contains(TAG_COLONY_ID) && compound.contains(TAG_BUILDER))
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            final BlockPos builderPos = BlockPosUtil.read(compound, TAG_BUILDER);
            MineColonies.proxy.openResourceScrollWindow(colonyId, builderPos, warehousePos, compound);
        }
        else
        {
            player.displayClientMessage(Component.translatable(TranslationConstants.COM_MINECOLONIES_SCROLL_NO_COLONY), true);
        }
    }
}
