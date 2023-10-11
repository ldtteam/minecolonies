package com.minecolonies.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * Overwritten {@link FireworkRocketEntity} for spawning on buildings that does not damage entities in the vicinity.
 */
public class NoDamageFireworkRocketEntity extends FireworkRocketEntity
{
    public NoDamageFireworkRocketEntity(final Level world, final int x, final int y, final int z, final ItemStack itemStack)
    {
        super(world, x, y, z, itemStack);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult)
    {
        super.onHitEntity(entityHitResult);
        if (!this.level.isClientSide)
        {
            this.explodeNoDamage();
        }
    }

    /**
     * Mirror of the FireworkRocketEntity.explode() method, without its call to the dealExplosionDamage() method.
     */
    private void explodeNoDamage()
    {
        this.level.broadcastEntityEvent(this, (byte) 17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult entityHitResult)
    {
        BlockPos blockpos = new BlockPos(entityHitResult.getBlockPos());
        this.level.getBlockState(blockpos).entityInside(this.level, blockpos, this);
        if (!this.level.isClientSide() && this.hasExplosionData())
        {
            this.explodeNoDamage();
        }

        super.onHitBlock(entityHitResult);
    }

    /**
     * Mirror of the FireworkRocketEntity.hasExplosion() method, however slightly modified to use input NBT due to no access to
     * the EntityDataAccessors.
     */
    private boolean hasExplosionData()
    {
        final CompoundTag compoundTag = new CompoundTag();
        addAdditionalSaveData(compoundTag);
        final CompoundTag fireworksItem = compoundTag.getCompound("FireworksItem");
        ListTag listtag = fireworksItem.getList("Explosions", 10);
        return !listtag.isEmpty();
    }
}
