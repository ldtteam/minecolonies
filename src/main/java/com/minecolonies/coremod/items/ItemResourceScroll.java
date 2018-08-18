package com.minecolonies.coremod.items;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.tileentities.TileEntityColonyBuilding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
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
                                       final EntityPlayer playerIn,
                                       final World worldIn,
                                       final BlockPos pos,
                                       final EnumHand hand,
                                       final EnumFacing facing,
                                       final float hitX,
                                       final float hitY,
                                       final float hitZ)
    {
        final ItemStack scroll = playerIn.getHeldItem(hand);

        final NBTTagCompound compound = checkForCompound(scroll);
        final TileEntity entity = worldIn.getTileEntity(pos);

        if (entity instanceof TileEntityColonyBuilding)
        {
            compound.setInteger(TAG_COLONY_ID, ((TileEntityColonyBuilding) entity).getColonyId());
            BlockPosUtil.writeToNBT(compound, TAG_BUILDER, ((TileEntityColonyBuilding) entity).getPosition());

            if (!worldIn.isRemote)
            {
                LanguageHandler.sendPlayerMessage(playerIn, TranslationConstants.COM_MINECOLONIES_CLIPBOARD_COLONY_SET, ((TileEntityColonyBuilding) entity).getColonyId());
            }
        }
        else if (compound.hasKey(TAG_COLONY_ID) && compound.hasKey(TAG_BUILDER) && worldIn.isRemote)
        {
            final int colonyId = compound.getInteger(TAG_COLONY_ID);
            final BlockPos builderPos = BlockPosUtil.readFromNBT(compound, TAG_BUILDER);
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
                                                     final EntityPlayer playerIn,
                                                     final EnumHand hand)
    {
        final ItemStack cllipboard = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote)
        {
            return new ActionResult<>(EnumActionResult.SUCCESS, cllipboard);
        }

        final NBTTagCompound compound = checkForCompound(cllipboard);

        if (compound.hasKey(TAG_COLONY_ID) && compound.hasKey(TAG_BUILDER))
        {
            final int colonyId = compound.getInteger(TAG_COLONY_ID);
            final BlockPos builderPos = BlockPosUtil.readFromNBT(compound, TAG_BUILDER);
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
    private static NBTTagCompound checkForCompound(final ItemStack item)
    {
        if (!item.hasTagCompound())
        {
            item.setTagCompound(new NBTTagCompound());
        }
        return item.getTagCompound();
    }
}
