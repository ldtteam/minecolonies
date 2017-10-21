package com.minecolonies.coremod.items;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.network.messages.ChangeFreeToInteractBlockMessage;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Permission scepter. used to add free to interact blocks or positions to the colonies permission list
 */
public class ItemScepterPermission extends AbstractItemMinecolonies
{
    /**
     * The NBT tag of the mode
     */
    private static final String TAG_ITEM_MODE = "scepterMode";

    /**
     * the scepters block mode tag value
     */
    private static final String TAG_VALUE_MODE_BLOCK = "modeBlock";

    /**
     * the scepters location mode tag value
     */
    private static final String TAG_VALUE_MODE_LOCATION = "modeLocation";

    /**
     * constructor.
     *
     * - set the name
     * - set max damage value
     * - set creative tab
     * - set max stack size
     */
    public ItemScepterPermission()
    {
        super("scepterPermission");
        this.setMaxDamage(2);

        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        maxStackSize = 1;
    }

    /**
     * Used when clicking on block in world.
     *
     * @param scepter the item stack
     * @param playerIn the player
     * @param worldIn the world
     * @param pos the position
     * @param hand the hand
     * @param facing the facing hit
     * @param hitX the x coordinate
     * @param hitY the y coordinate
     * @param hitZ the z coordinate
     * @return the result
     */
    @Override
    @NotNull
    public EnumActionResult onItemUse(
            final ItemStack scepter,
            final EntityPlayer playerIn,
            final World worldIn,
            final BlockPos pos,
            final EnumHand hand,
            final EnumFacing facing,
            final float hitX,
            final float hitY,
            final float hitZ)
    {
        if (!worldIn.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = scepter.getTagCompound();

        final ColonyView colonyView = ColonyManager.getClosestColonyView(worldIn, pos);
        if (colonyView == null)
        {
            return EnumActionResult.FAIL;
        }

        return handleItemAction(compound, playerIn, worldIn, pos, colonyView);
    }

    /**
     * Handles mid air use.
     *
     * @param scepter the item stack
     * @param worldIn the world
     * @param playerIn the player
     * @param hand the hand
     * @return the result
     */
    @Override
    @NotNull
    public ActionResult<ItemStack> onItemRightClick(
            @NotNull final ItemStack scepter,
            final World worldIn,
            final EntityPlayer playerIn,
            final EnumHand hand)
    {
        if (worldIn.isRemote)
        {
            return new ActionResult<>(EnumActionResult.SUCCESS, scepter);
        }

        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = scepter.getTagCompound();

        toggleItemMode(playerIn, compound);

        return new ActionResult<>(EnumActionResult.SUCCESS, scepter);
    }

    @NotNull
    private EnumActionResult handleItemAction(
            final NBTTagCompound compound,
            final EntityPlayer playerIn,
            final World worldIn,
            final BlockPos pos,
            final ColonyView colonyView)
    {
        final String tagItemMode = compound.getString(TAG_ITEM_MODE);

        switch (tagItemMode)
         {
            case TAG_VALUE_MODE_BLOCK:
                return handleAddBlockType(playerIn, worldIn, pos, colonyView);
            case TAG_VALUE_MODE_LOCATION:
                return handleAddLocation(playerIn, worldIn, pos, colonyView);
            default:
                toggleItemMode(playerIn, compound);
                return handleItemAction(compound, playerIn, worldIn, pos, colonyView);
        }
    }

    private static void toggleItemMode(final EntityPlayer playerIn, final NBTTagCompound compound)
    {
        final String itemMode = compound.getString(TAG_ITEM_MODE);

        switch (itemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                compound.setString(TAG_ITEM_MODE, TAG_VALUE_MODE_LOCATION);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.item.permissionscepter.setmode", "location");
                break;

            case TAG_VALUE_MODE_LOCATION:
            default:
                compound.setString(TAG_ITEM_MODE, TAG_VALUE_MODE_BLOCK);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.item.permissionscepter.setmode", "block");

                break;
        }
    }

    @NotNull
    private static EnumActionResult handleAddBlockType(
            final EntityPlayer playerIn,
            final World worldIn,
            final BlockPos pos,
            final ColonyView colonyView)
    {
        final IBlockState blockState = worldIn.getBlockState(pos);
        final Block block = blockState.getBlock();

        final ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(
                colonyView,
                block,
                ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK);
        MineColonies.getNetwork().sendToServer(message);

        return EnumActionResult.SUCCESS;
    }

    @NotNull
    private static EnumActionResult handleAddLocation(
            final EntityPlayer playerIn,
            final World worldIn,
            final BlockPos pos,
            final ColonyView colonyView)
    {
        ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(colonyView, pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK);
        MineColonies.getNetwork().sendToServer(message);

        return EnumActionResult.SUCCESS;
    }

}
