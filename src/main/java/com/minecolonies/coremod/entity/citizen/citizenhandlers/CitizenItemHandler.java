package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenItemHandler;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.BlockParticleEffectMessage;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.minecolonies.api.util.constant.CitizenConstants.*;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_PITCH_MULTIPLIER;
import static com.minecolonies.api.util.constant.Constants.DEFAULT_VOLUME;

/**
 * Handles the citizens interaction with an item with the world.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class CitizenItemHandler implements ICitizenItemHandler
{
    /**
     * The citizen assigned to this manager.
     */
    private final EntityCitizen citizen;

    /**
     * Constructor for the experience handler.
     * @param citizen the citizen owning the handler.
     */
    public CitizenItemHandler(final EntityCitizen citizen)
    {
        this.citizen = citizen;
    }

    /**
     * Citizen will try to pick up a certain item.
     *
     * @param entityItem the item he wants to pickup.
     */
    @Override
    public void tryPickupEntityItem(@NotNull final EntityItem entityItem)
    {
        if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote)
        {
            if (entityItem.cannotPickup())
            {
                return;
            }

            final ItemStack itemStack = entityItem.getItem();
            final ItemStack compareStack = itemStack.copy();

            final ItemStack resultStack = InventoryUtils.addItemStackToItemHandlerWithResult(new InvWrapper(citizen.getInventoryCitizen()), itemStack);
            final int resultingStackSize = ItemStackUtils.isEmpty(resultStack) ? 0 : ItemStackUtils.getSize(resultStack);

            if (ItemStackUtils.isEmpty(resultStack) || ItemStackUtils.getSize(resultStack) != ItemStackUtils.getSize(compareStack))
            {
                CompatibilityUtils.getWorldFromCitizen(citizen).playSound(null,
                  citizen.getPosition(),
                  SoundEvents.ENTITY_ITEM_PICKUP,
                  SoundCategory.AMBIENT,
                  (float) DEFAULT_VOLUME,
                  (float) ((citizen.getRandom().nextGaussian() * DEFAULT_PITCH_MULTIPLIER + 1.0D) * 2.0D));
                citizen.onItemPickup(entityItem, ItemStackUtils.getSize(itemStack) - resultingStackSize);

                final ItemStack overrulingStack = itemStack.copy();
                overrulingStack.setCount(ItemStackUtils.getSize(itemStack) - resultingStackSize);

                if (citizen.getCitizenJobHandler().getColonyJob() != null)
                {
                    citizen.getCitizenJobHandler().getColonyJob().onStackPickUp(overrulingStack);
                }

                if (ItemStackUtils.isEmpty(resultStack))
                {
                    entityItem.setDead();
                }
            }
        }
    }

    /**
     * Removes the currently held item.
     */
    @Override
    public void removeHeldItem()
    {
        citizen.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);
    }

    /**
     * Sets the currently held item.
     *
     * @param hand what hand we're setting
     * @param slot from the inventory slot.
     */
    @Override
    public void setHeldItem(final EnumHand hand, final int slot)
    {
        citizen.getCitizenData().getInventory().setHeldItem(hand, slot);
        if (hand.equals(EnumHand.MAIN_HAND))
        {
            citizen.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, citizen.getCitizenData().getInventory().getStackInSlot(slot));
        }
        else if (hand.equals(EnumHand.OFF_HAND))
        {
            citizen.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, citizen.getCitizenData().getInventory().getStackInSlot(slot));
        }
    }

    /**
     * Sets the currently held for mainHand item.
     *
     * @param slot from the inventory slot.
     */
    @Override
    public void setMainHeldItem(final int slot)
    {
        citizen.getCitizenData().getInventory().setHeldItem(EnumHand.MAIN_HAND, slot);
        citizen.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, citizen.getCitizenData().getInventory().getStackInSlot(slot));
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * Will not break the block.
     *
     * @param blockPos Block position.
     */
    @Override
    public void hitBlockWithToolInHand(@Nullable final BlockPos blockPos)
    {
        if (blockPos == null)
        {
            return;
        }
        hitBlockWithToolInHand(blockPos, false);
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * If breakBlock is true then it will break the block (different sound and
     * particles), and damage the tool in the citizens hand.
     *
     * @param blockPos   Block position.
     * @param breakBlock if we want to break this block.
     */
    @Override
    public void hitBlockWithToolInHand(@Nullable final BlockPos blockPos, final boolean breakBlock)
    {
        if (blockPos == null)
        {
            return;
        }

        citizen.getLookHelper().setLookPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ(), FACING_DELTA_YAW, citizen.getVerticalFaceSpeed());

        citizen.swingArm(citizen.getActiveHand());

        final IBlockState blockState = CompatibilityUtils.getWorldFromCitizen(citizen).getBlockState(blockPos);
        final Block block = blockState.getBlock();
        if (breakBlock)
        {
            if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote)
            {
                MineColonies.getNetwork().sendToAllAround(
                  new BlockParticleEffectMessage(blockPos, CompatibilityUtils.getWorldFromCitizen(citizen).getBlockState(blockPos), BlockParticleEffectMessage.BREAK_BLOCK),
                  new NetworkRegistry.TargetPoint(CompatibilityUtils.getWorldFromCitizen(citizen).provider.getDimension(),
                    blockPos.getX(), blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_SOUND_RANGE));
            }
            CompatibilityUtils.getWorldFromCitizen(citizen).playSound(null,
              blockPos,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getBreakSound(),
              SoundCategory.BLOCKS,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getVolume(),
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getPitch());
            CompatibilityUtils.getWorldFromCitizen(citizen).setBlockToAir(blockPos);

            damageItemInHand(citizen.getActiveHand(), 1);
        }
        else
        {
            if (!CompatibilityUtils.getWorldFromCitizen(citizen).isRemote)
            {
                final BlockPos vector = blockPos.subtract(citizen.getPosition());
                final EnumFacing facing = EnumFacing.getFacingFromVector(vector.getX(), vector.getY(), vector.getZ()).getOpposite();

                MineColonies.getNetwork().sendToAllAround(
                  new BlockParticleEffectMessage(blockPos, CompatibilityUtils.getWorldFromCitizen(citizen).getBlockState(blockPos), facing.ordinal()),
                  new NetworkRegistry.TargetPoint(CompatibilityUtils.getWorldFromCitizen(citizen).provider.getDimension(), blockPos.getX(),
                    blockPos.getY(), blockPos.getZ(), BLOCK_BREAK_PARTICLE_RANGE));
            }
            CompatibilityUtils.getWorldFromCitizen(citizen).playSound(null,
              blockPos,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getBreakSound(),
              SoundCategory.BLOCKS,
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getVolume(),
              block.getSoundType(blockState, CompatibilityUtils.getWorldFromCitizen(citizen), blockPos, citizen).getPitch());
        }
    }

    /**
     * Damage the current held item.
     *
     * @param damage amount of damage.
     */
    @Override
    public void damageItemInHand(final EnumHand hand, final int damage)
    {
        final ItemStack heldItem = citizen.getCitizenData().getInventory().getHeldItem(hand);
        //If we hit with bare hands, ignore
        if (heldItem == null)
        {
            return;
        }
        heldItem.damageItem(damage, citizen);

        //check if tool breaks
        if (ItemStackUtils.isEmpty(heldItem))
        {
            citizen.getInventoryCitizen().setInventorySlotContents(citizen.getInventoryCitizen().getHeldItemSlot(hand), ItemStackUtils.EMPTY);
            citizen.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStackUtils.EMPTY);
        }
    }

    /**
     * Pick up all items in a range around the citizen.
     */
    @Override
    public void pickupItems()
    {
        @NotNull final List<EntityItem> retList = new ArrayList<>();
        //I know streams look better but they are flawed in type erasure
        for (final Object o :
          CompatibilityUtils.getWorldFromCitizen(citizen).
                                             getEntitiesWithinAABB(EntityItem.class,
                                               new AxisAlignedBB(citizen.getPosition()).expand(2.0F, 1.0F, 2.0F).expand(-2.0F, -1.0F, -2.0F)))
        {
            if (o instanceof EntityItem)
            {
                retList.add((EntityItem) o);
            }
        }

        retList.stream()
          .filter(Objects::nonNull)
          .filter(item -> !item.isDead)
          .filter(item -> citizen.canPickUpLoot())
          .forEach(this::tryPickupEntityItem);
    }

    /**
     * Swing entity arm, create sound and particle effects.
     * <p>
     * This will break the block (different sound and particles),
     * and damage the tool in the citizens hand.
     *
     * @param blockPos Block position.
     */
    @Override
    public void breakBlockWithToolInHand(@Nullable final BlockPos blockPos)
    {
        if (blockPos == null)
        {
            return;
        }
        hitBlockWithToolInHand(blockPos, true);
    }

    /**
     * Handles the dropping of items from the entity.
     *
     * @param itemstack to drop.
     * @return the dropped item.
     */
    @Override
    public EntityItem entityDropItem(@NotNull final ItemStack itemstack)
    {
        return citizen.entityDropItem(itemstack, 0.0F);
    }

    /**
     * Updates the armour damage after being hit.
     *
     * @param damage damage dealt.
     */
    @Override
    public void updateArmorDamage(final double damage)
    {
        for (final ItemStack stack : citizen.getArmorInventoryList())
        {
            if (ItemStackUtils.isEmpty(stack) || !(stack.getItem() instanceof ItemArmor))
            {
                continue;
            }
            stack.damageItem(Math.max(1, (int) (damage / 4)), citizen);

            if (ItemStackUtils.isEmpty(stack))
            {
                citizen.setItemStackToSlot(EntityLiving.getSlotForItemStack(stack), ItemStackUtils.EMPTY);
            }
            citizen.setItemStackToSlot(EntityLiving.getSlotForItemStack(stack), stack);
        }
    }
}
