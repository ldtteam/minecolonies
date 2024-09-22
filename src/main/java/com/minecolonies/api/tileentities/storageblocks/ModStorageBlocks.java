package com.minecolonies.api.tileentities.storageblocks;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.tileentities.AbstractTileEntityRack;
import com.minecolonies.api.tileentities.storageblocks.registry.StorageBlockEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.tileentities.TileEntityRack;
import com.minecolonies.core.tileentities.storageblocks.AbstractRackStorageBlockInterface;
import com.minecolonies.core.tileentities.storageblocks.RackStorageBlockInterface;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public final class ModStorageBlocks
{
    public static final DeferredRegister<StorageBlockEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "storageblocks"), Constants.MOD_ID);

    public static final RegistryObject<StorageBlockEntry> storageBlockRack;
    public static final RegistryObject<StorageBlockEntry> storageBlockAbstractRack;
    static
    {
        storageBlockRack = DEFERRED_REGISTER.register("rack",
          () -> new StorageBlockEntry.Builder()
                  .setIsStorageBlock(blockEntity -> blockEntity instanceof TileEntityRack)
                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, "rack"))
                  .setStorageInterface(RackStorageBlockInterface::new)
                  .build());

        storageBlockAbstractRack = DEFERRED_REGISTER.register("abstract_rack",
          () -> new StorageBlockEntry.Builder()
                  .setIsStorageBlock(blockEntity -> blockEntity instanceof AbstractTileEntityRack && !(blockEntity instanceof TileEntityRack))
                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, "abstract_rack"))
                  .setStorageInterface(AbstractRackStorageBlockInterface::new)
                  .build());
    }
    /**
     * Private constructor so this class can't be instantiated.
     */
    private ModStorageBlocks()
    {
    }

    /**
     * Tries to find a matching BlockInterface for the given BlockEntity
     *
     * @param blockEntity The block entity to find a matching BlockInterface for.
     * @return A StorageBlockInterface for the given BlockEntity if one exists.
     */
    public static Optional<IStorageBlockInterface> getStorageBlockInterface(BlockEntity blockEntity)
    {
        if (blockEntity == null)
        {
            return Optional.empty();
        }

        for (StorageBlockEntry entry : MinecoloniesAPIProxy.getInstance().getStorageBlockRegistry())
        {
            if (entry.matches(blockEntity))
            {
                return Optional.of(entry.getStorageInterface().apply(blockEntity));
            }
        }

        return Optional.empty();
    }
}
