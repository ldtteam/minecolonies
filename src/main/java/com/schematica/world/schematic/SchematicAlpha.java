package com.schematica.world.schematic;

import com.schematica.nbt.NBTHelper;
import com.schematica.reference.Names;
import com.schematica.reference.Reference;
import com.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SchematicAlpha extends SchematicFormat {

    private static final String OFFSET_X           = "OffsetX";
    private static final String OFFSET_Y           = "OffsetY";
    private static final String OFFSET_Z           = "OffsetZ";

    private static final FMLControlledNamespacedRegistry<Block> BLOCK_REGISTRY = GameData.getBlockRegistry();

    @Override
    public Schematic readFromNBT(final NBTTagCompound tagCompound) {
        final ItemStack icon = SchematicUtil.getIconFromNBT(tagCompound);

        final byte[] localBlocks = tagCompound.getByteArray(Names.NBT.BLOCKS);
        final byte[] localMetadata = tagCompound.getByteArray(Names.NBT.DATA);

        boolean extra = false;
        byte extraBlocks[] = null;
        byte extraBlocksNibble[] = null;
        if (tagCompound.hasKey(Names.NBT.ADD_BLOCKS)) {
            extra = true;
            extraBlocksNibble = tagCompound.getByteArray(Names.NBT.ADD_BLOCKS);
            extraBlocks = new byte[extraBlocksNibble.length * 2];
            for (int i = 0; i < extraBlocksNibble.length; i++) {
                extraBlocks[i * 2 + 0] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
                extraBlocks[i * 2 + 1] = (byte) (extraBlocksNibble[i] & 0xF);
            }
        } else if (tagCompound.hasKey(Names.NBT.ADD_BLOCKS_SCHEMATICA)) {
            extra = true;
            extraBlocks = tagCompound.getByteArray(Names.NBT.ADD_BLOCKS_SCHEMATICA);
        }

        final short width = tagCompound.getShort(Names.NBT.WIDTH);
        final short length = tagCompound.getShort(Names.NBT.LENGTH);
        final short height = tagCompound.getShort(Names.NBT.HEIGHT);

        Short id = null;
        final Map<Short, Short> oldToNew = new HashMap<>();
        if (tagCompound.hasKey(Names.NBT.MAPPING_SCHEMATICA)) {
            final NBTTagCompound mapping = tagCompound.getCompoundTag(Names.NBT.MAPPING_SCHEMATICA);
            final Set<String> names = mapping.getKeySet();
            for (final String name : names) {
                oldToNew.put(mapping.getShort(name), (short) BLOCK_REGISTRY.getId(new ResourceLocation(name)));
            }
        }

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final Schematic schematic = new Schematic(icon, width, height, length);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    final int index = x + (y * length + z) * width;
                    int blockID = (localBlocks[index] & 0xFF) | (extra ? ((extraBlocks[index] & 0xFF) << 8) : 0);
                    final int meta = localMetadata[index] & 0xFF;

                    if ((id = oldToNew.get((short) blockID)) != null) {
                        blockID = id;
                    }

                    final Block block = BLOCK_REGISTRY.getObjectById(blockID);
                    pos.set(x, y, z);
                    try {
                        final IBlockState blockState = block.getStateFromMeta(meta);
                        schematic.setBlockState(pos, blockState);
                    } catch (final Exception e) {
                        Reference.logger.error("Could not set block state at {} to {} with metadata {}", pos, BLOCK_REGISTRY.getNameForObject(block), meta, e);
                    }
                }
            }
        }

        final NBTTagList tileEntitiesList = tagCompound.getTagList(Names.NBT.TILE_ENTITIES, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tileEntitiesList.tagCount(); i++) {
            try {
                final TileEntity tileEntity = NBTHelper.readTileEntityFromCompound(tileEntitiesList.getCompoundTagAt(i));
                if (tileEntity != null) {
                    schematic.setTileEntity(tileEntity.getPos(), tileEntity);
                }
            } catch (final Exception e) {
                Reference.logger.error("TileEntity failed to load properly!", e);
            }
        }


        if(tagCompound.hasKey(OFFSET_X))
        {
            int xOffset = tagCompound.getShort(OFFSET_X);
            int yOffset = tagCompound.getShort(OFFSET_Y);
            int zOffset = tagCompound.getShort(OFFSET_Z);

            schematic.setOffset(new BlockPos(xOffset, yOffset, zOffset));
        }

        return schematic;
    }

    @Override
    public void writeToNBT(final NBTTagCompound tagCompound, final Schematic schematic) {
        final NBTTagCompound tagCompoundIcon = new NBTTagCompound();
        final ItemStack icon = schematic.getIcon();
        icon.writeToNBT(tagCompoundIcon);
        tagCompound.setTag(Names.NBT.ICON, tagCompoundIcon);

        tagCompound.setShort(Names.NBT.WIDTH, (short) schematic.getWidth());
        tagCompound.setShort(Names.NBT.LENGTH, (short) schematic.getLength());
        tagCompound.setShort(Names.NBT.HEIGHT, (short) schematic.getHeight());

        final int size = schematic.getWidth() * schematic.getLength() * schematic.getHeight();
        final byte[] localBlocks = new byte[size];
        final byte[] localMetadata = new byte[size];
        final byte[] extraBlocks = new byte[size];
        final byte[] extraBlocksNibble = new byte[(int) Math.ceil(size / 2.0)];
        boolean extra = false;

        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        final Map<String, Short> mappings = new HashMap<>();
        for (int x = 0; x < schematic.getWidth(); x++) {
            for (int y = 0; y < schematic.getHeight(); y++) {
                for (int z = 0; z < schematic.getLength(); z++) {
                    final int index = x + (y * schematic.getLength() + z) * schematic.getWidth();
                    final IBlockState blockState = schematic.getBlockState(pos.set(x, y, z));
                    final Block block = blockState.getBlock();
                    final int blockId = BLOCK_REGISTRY.getId(block);
                    localBlocks[index] = (byte) blockId;
                    localMetadata[index] = (byte) block.getMetaFromState(blockState);
                    if ((extraBlocks[index] = (byte) (blockId >> 8)) > 0) {
                        extra = true;
                    }

                    final String name = String.valueOf(BLOCK_REGISTRY.getNameForObject(block));
                    if (!mappings.containsKey(name)) {
                        mappings.put(name, (short) blockId);
                    }
                }
            }
        }

        int count = 20;
        final NBTTagList tileEntitiesList = new NBTTagList();
        for (final TileEntity tileEntity : schematic.getTileEntities()) {
            try {
                final NBTTagCompound tileEntityTagCompound = NBTHelper.writeTileEntityToCompound(tileEntity);
                tileEntitiesList.appendTag(tileEntityTagCompound);
            } catch (final Exception e) {
                final BlockPos tePos = tileEntity.getPos();
                final int index = tePos.getX() + (tePos.getY() * schematic.getLength() + tePos.getZ()) * schematic.getWidth();
                if (--count > 0) {
                    final IBlockState blockState = schematic.getBlockState(tePos);
                    final Block block = blockState.getBlock();
                    Reference.logger.error("Block {}[{}] with TileEntity {} failed to save! Replacing with bedrock...", block, block != null ? BLOCK_REGISTRY.getNameForObject(block) : "?", tileEntity.getClass().getName(), e);
                }
                localBlocks[index] = (byte) BLOCK_REGISTRY.getId(Blocks.bedrock);
                localMetadata[index] = 0;
                extraBlocks[index] = 0;
            }
        }

        for (int i = 0; i < extraBlocksNibble.length; i++) {
            if (i * 2 + 1 < extraBlocks.length) {
                extraBlocksNibble[i] = (byte) ((extraBlocks[i * 2 + 0] << 4) | extraBlocks[i * 2 + 1]);
            } else {
                extraBlocksNibble[i] = (byte) (extraBlocks[i * 2 + 0] << 4);
            }
        }

        final NBTTagList entityList = new NBTTagList();
        final List<Entity> entities = schematic.getEntities();
        for (final Entity entity : entities) {
            try {
                final NBTTagCompound entityCompound = NBTHelper.writeEntityToCompound(entity);
                if (entityCompound != null) {
                    entityList.appendTag(entityCompound);
                }
            } catch (final Exception e) {
                Reference.logger.error("Entity {} failed to save, skipping!", entity, e);
            }
        }

        final NBTTagCompound nbtMapping = new NBTTagCompound();
        for (final Map.Entry<String, Short> entry : mappings.entrySet()) {
            nbtMapping.setShort(entry.getKey(), entry.getValue());
        }

        tagCompound.setString(Names.NBT.MATERIALS, Names.NBT.FORMAT_ALPHA);
        tagCompound.setByteArray(Names.NBT.BLOCKS, localBlocks);
        tagCompound.setByteArray(Names.NBT.DATA, localMetadata);
        if (extra) {
            tagCompound.setByteArray(Names.NBT.ADD_BLOCKS, extraBlocksNibble);
        }
        tagCompound.setTag(Names.NBT.ENTITIES, entityList);
        tagCompound.setTag(Names.NBT.TILE_ENTITIES, tileEntitiesList);
        tagCompound.setTag(Names.NBT.MAPPING_SCHEMATICA, nbtMapping);

        tagCompound.setShort(OFFSET_X, (short) schematic.getOffset().getX());
        tagCompound.setShort(OFFSET_Y, (short) schematic.getOffset().getY());
        tagCompound.setShort(OFFSET_Z, (short) schematic.getOffset().getZ());

    }
}
