package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BUILDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;

/**
 * Class describing the resource scroll item.
 */
public class ItemResourceScroll extends AbstractItemMinecolonies
{
    /**
     * Sets the name, creative tab, and registers the resource scroll item.
     */
    public ItemResourceScroll()
    {
        super("resourcescroll");
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setMaxStackSize(STACKSIZE);
    }

    /**
     * Used when clicking on block in world.
     *
     * @param playerIn the player
     * @param worldIn  the world
     * @param pos      the position
     * @param hand     the hand
     * @param facing   the facing hit
     * @param hitX     the x coordinate
     * @param hitY     the y coordinate
     * @param hitZ     the z coordinate
     * @return the result
     */
    @Override
    @NotNull
    public EnumActionResult onItemUse(
                                       final PlayerEntity playerIn,
                                       final World worldIn,
                                       final BlockPos pos,
                                       final EnumHand hand,
                                       final Direction facing,
                                       final float hitX,
                                       final float hitY,
                                       final float hitZ)
    {
        final ItemStack scroll = playerIn.getHeldItem(hand);

        final CompoundNBT compound = checkForCompound(scroll);
        final TileEntity entity = worldIn.getTileEntity(pos);

        if (entity instanceof TileEntityColonyBuilding)
        {
            compound.putInt(TAG_COLONY_ID, ((AbstractTileEntityColonyBuilding) entity).getColonyId());
            BlockPosUtil.write(compound, TAG_BUILDER, ((AbstractTileEntityColonyBuilding) entity).getPosition());

            if (!worldIn.isRemote)
            {
                LanguageHandler.sendPlayerMessage(playerIn, TranslationConstants.COM_MINECOLONIES_CLIPBOARD_COLONY_SET, ((AbstractTileEntityColonyBuilding) entity).getColonyId());
            }
        }
        else if (compound.keySet().contains(TAG_COLONY_ID) && compound.keySet().contains(TAG_BUILDER) && worldIn.isRemote)
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            final BlockPos builderPos = BlockPosUtil.read(compound, TAG_BUILDER);
            MineColonies.proxy.openResourceScrollWindow(colonyId, builderPos);
        }

        return EnumActionResult.SUCCESS;
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
    public ActionResult<ItemStack> onItemRightClick(
                                                     final World worldIn,
                                                     final PlayerEntity playerIn,
                                                     final EnumHand hand)
    {
        final ItemStack cllipboard = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote)
        {
            return new ActionResult<>(EnumActionResult.SUCCESS, cllipboard);
        }

        final CompoundNBT compound = checkForCompound(cllipboard);

        if (compound.keySet().contains(TAG_COLONY_ID) && compound.keySet().contains(TAG_BUILDER))
        {
            final int colonyId = compound.getInt(TAG_COLONY_ID);
            final BlockPos builderPos = BlockPosUtil.read(compound, TAG_BUILDER);
            MineColonies.proxy.openResourceScrollWindow(colonyId, builderPos);
        }
        else
        {
            LanguageHandler.sendPlayerMessage(playerIn, TranslationConstants.COM_MINECOLONIES_CLIPBOARD_NEED_COLONY);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, cllipboard);
    }

    /**
     * Check for the compound and return it.
     * If not available create and return it.
     *
     * @param item the item to check in for.
     */
    private static CompoundNBT checkForCompound(final ItemStack item)
    {
        if (!item.hasTagCompound())
        {
            item.put(new CompoundNBT());
        }
        return item.getTag();
    }
}
