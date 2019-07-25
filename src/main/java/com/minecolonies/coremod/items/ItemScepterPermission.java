package com.minecolonies.coremod.items;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.network.messages.ChangeFreeToInteractBlockMessage;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
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
     * <p>
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

    @NotNull
    private static EnumActionResult handleAddBlockType(
                                                        final PlayerEntity playerIn,
                                                        final World worldIn,
                                                        final BlockPos pos,
                                                        final ColonyView colonyView)
    {
        final BlockState blockState = worldIn.getBlockState(pos);
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
                                                       final PlayerEntity playerIn,
                                                       final World worldIn,
                                                       final BlockPos pos,
                                                       final ColonyView colonyView)
    {
        final ChangeFreeToInteractBlockMessage message = new ChangeFreeToInteractBlockMessage(colonyView, pos, ChangeFreeToInteractBlockMessage.MessageType.ADD_BLOCK);
        MineColonies.getNetwork().sendToServer(message);

        return EnumActionResult.SUCCESS;
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
        if (!worldIn.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }
        final ItemStack scepter = playerIn.getHeldItem(hand);
        if (!scepter.hasTagCompound())
        {
            scepter.putCompound(new CompoundNBT());
        }

        final ColonyView colonyView = ColonyManager.getClosestColonyView(worldIn, pos);
        if (colonyView == null)
        {
            return EnumActionResult.FAIL;
        }
        final CompoundNBT compound = scepter.getTagCompound();
        return handleItemAction(compound, playerIn, worldIn, pos, colonyView);
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
        final ItemStack scepter = playerIn.getHeldItem(hand);
        if (worldIn.isRemote)
        {
            return new ActionResult<>(EnumActionResult.SUCCESS, scepter);
        }
        if (!scepter.hasTagCompound())
        {
            scepter.putCompound(new CompoundNBT());
        }
        final CompoundNBT compound = scepter.getTagCompound();

        toggleItemMode(playerIn, compound);

        return new ActionResult<>(EnumActionResult.SUCCESS, scepter);
    }

    private static void toggleItemMode(final PlayerEntity playerIn, final CompoundNBT compound)
    {
        final String itemMode = compound.getString(TAG_ITEM_MODE);

        switch (itemMode)
        {
            case TAG_VALUE_MODE_BLOCK:
                compound.putString(TAG_ITEM_MODE, TAG_VALUE_MODE_LOCATION);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.item.permissionscepter.setmode", "location");
                break;

            case TAG_VALUE_MODE_LOCATION:
            default:
                compound.putString(TAG_ITEM_MODE, TAG_VALUE_MODE_BLOCK);
                LanguageHandler.sendPlayerMessage(playerIn, "com.minecolonies.coremod.item.permissionscepter.setmode", "block");

                break;
        }
    }

    @NotNull
    private static EnumActionResult handleItemAction(
                                               final CompoundNBT compound,
                                               final PlayerEntity playerIn,
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
}
