package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.storageblocks.registry.StorageBlockEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.TileEntityRack;
import com.minecolonies.core.tileentities.storagecontainers.RackStorageBlockInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public final class ModStorageBlocks
{
    public static final DeferredRegister<StorageBlockEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "storageblocks"), Constants.MOD_ID);

    public static final RegistryObject<StorageBlockEntry> rack;
    static
    {
        rack = DEFERRED_REGISTER.register("rack",
          () -> new StorageBlockEntry.Builder()
                  .setIsStorageBlock(blockEntity -> blockEntity instanceof TileEntityRack)
                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, "rack"))
                  .setStorageInterface(new RackStorageBlockInterface())
                  .build());
    }

    public static Optional<IStorageBlockInterface> getStorageBlockInterface(BlockEntity blockEntity) {
        for (StorageBlockEntry entry : MinecoloniesAPIProxy.getInstance().getStorageBlockRegistry()) {
            if (entry.matches(blockEntity)) {
                return Optional.of(entry.getStorageInterface());
            }
        }

        return Optional.empty();
    }

    /**
     * Private constructor so this class can't be instantiated.
     */
    private ModStorageBlocks() {}
}
