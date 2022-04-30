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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
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
    public ActionResultType useOn(ItemUseContext ctx)
    {
        final ItemStack scroll = ctx.getPlayer().getItemInHand(ctx.getHand());

        final CompoundNBT compound = checkForCompound(scroll);
        TileEntity entity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

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
                    final IFormattableTextComponent buildingTypeComponent = MessageUtils.format(buildingEntity.getBuilding().getBuildingType().getTranslationKey()).create();
                    MessageUtils.format(COM_MINECOLONIES_SCROLL_WRONG_BUILDING, buildingTypeComponent, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
                }
            }
        }
        else if (ctx.getLevel().isClientSide)
        {
            openWindow(compound, ctx.getPlayer(), null);
        }

        return ActionResultType.SUCCESS;
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
    public ActionResult<ItemStack> use(
      final World worldIn,
      final PlayerEntity playerIn,
      final Hand hand)
    {
        final ItemStack clipboard = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide)
        {
            return new ActionResult<>(ActionResultType.SUCCESS, clipboard);
        }

        openWindow(checkForCompound(clipboard), playerIn, null);

        return new ActionResult<>(ActionResultType.SUCCESS, clipboard);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (worldIn == null)
        {
            return;
        }

        final CompoundNBT compound = checkForCompound(stack);
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
                              ? new StringTextComponent(TextFormatting.DARK_PURPLE + name)
                              : new TranslationTextComponent(COM_MINECOLONIES_SCROLL_BUILDING_NO_WORKER));
            }
        }
    }

    /**
     * Check for the compound and return it. If not available create and return it.
     *
     * @param item the item to check in for.
     * @return the compound of the item.
     */
    private static CompoundNBT checkForCompound(final ItemStack item)
    {
        if (!item.hasTag())
        {
            item.setTag(new CompoundNBT());
        }
        return item.getTag();
    }

    /**
     * Opens the scroll window if there is a valid builder linked
     *
     * @param compound the item compound
     * @param player   the player entity opening the window
     */
    private static void openWindow(CompoundNBT compound, PlayerEntity player, BlockPos warehousePos)
    {
        if (compound.getAllKeys().contains(TAG_COLONY_ID) && compound.getAllKeys().contains(TAG_BUILDER))
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            final BlockPos builderPos = BlockPosUtil.read(compound, TAG_BUILDER);
            MineColonies.proxy.openResourceScrollWindow(colonyId, builderPos, warehousePos, compound);
        }
        else
        {
            player.displayClientMessage(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_SCROLL_NO_COLONY), true);
        }
    }
}
