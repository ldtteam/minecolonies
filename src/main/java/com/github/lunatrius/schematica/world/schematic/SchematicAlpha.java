package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.lib.Reference;
import com.github.lunatrius.schematica.world.SchematicWorld;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class SchematicAlpha extends SchematicFormat {
	@Override
	public SchematicWorld readFromNBT(NBTTagCompound tagCompound) {
		ItemStack icon = SchematicWorld.getIconFromNBT(tagCompound);

		byte localBlocks[] = tagCompound.getByteArray("Blocks");
		byte localMetadata[] = tagCompound.getByteArray("Data");

		boolean extra = tagCompound.hasKey("Add") || tagCompound.hasKey("AddBlocks");
		byte extraBlocks[] = null;
		byte extraBlocksNibble[] = null;
		if (tagCompound.hasKey("AddBlocks")) {
			extraBlocksNibble = tagCompound.getByteArray("AddBlocks");
			extraBlocks = new byte[extraBlocksNibble.length * 2];
			for (int i = 0; i < extraBlocksNibble.length; i++) {
				extraBlocks[i * 2 + 0] = (byte) ((extraBlocksNibble[i] >> 4) & 0xF);
				extraBlocks[i * 2 + 1] = (byte) (extraBlocksNibble[i] & 0xF);
			}
		} else if (tagCompound.hasKey("Add")) {
			extraBlocks = tagCompound.getByteArray("Add");
		}

		short width = tagCompound.getShort("Width");
		short length = tagCompound.getShort("Length");
		short height = tagCompound.getShort("Height");

		short[][][] blocks = new short[width][height][length];
		byte[][][] metadata = new byte[width][height][length];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					blocks[x][y][z] = (short) ((localBlocks[x + (y * length + z) * width]) & 0xFF);
					metadata[x][y][z] = (byte) ((localMetadata[x + (y * length + z) * width]) & 0xFF);
					if (extra) {
						blocks[x][y][z] |= ((extraBlocks[x + (y * length + z) * width]) & 0xFF) << 8;
					}
				}
			}
		}

		List<TileEntity> tileEntities = new ArrayList<TileEntity>();
		NBTTagList tileEntitiesList = tagCompound.getTagList("TileEntities", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < tileEntitiesList.tagCount(); i++) {
			TileEntity tileEntity = TileEntity.createAndLoadEntity(tileEntitiesList.getCompoundTagAt(i));
			if (tileEntity != null) {
				tileEntities.add(tileEntity);
			}
		}

		return new SchematicWorld(icon, blocks, metadata, tileEntities, width, height, length);
	}

	@Override
	public boolean writeToNBT(NBTTagCompound tagCompound, SchematicWorld world) {
		NBTTagCompound tagCompoundIcon = new NBTTagCompound();
		ItemStack icon = world.getIcon();
		icon.writeToNBT(tagCompoundIcon);
		tagCompound.setTag("Icon", tagCompoundIcon);

		tagCompound.setShort("Width", (short) world.getWidth());
		tagCompound.setShort("Length", (short) world.getLength());
		tagCompound.setShort("Height", (short) world.getHeight());

		int size = world.getWidth() * world.getLength() * world.getHeight();
		byte localBlocks[] = new byte[size];
		byte localMetadata[] = new byte[size];
		byte extraBlocks[] = new byte[size];
		byte extraBlocksNibble[] = new byte[(int) Math.ceil(size / 2.0)];
		boolean extra = false;

		for (int x = 0; x < world.getWidth(); x++) {
			for (int y = 0; y < world.getHeight(); y++) {
				for (int z = 0; z < world.getLength(); z++) {
					localBlocks[x + (y * world.getLength() + z) * world.getWidth()] = (byte) world.getBlockIdRaw(x, y, z);
					localMetadata[x + (y * world.getLength() + z) * world.getWidth()] = (byte) world.getBlockMetadata(x, y, z);
					if ((extraBlocks[x + (y * world.getLength() + z) * world.getWidth()] = (byte) (world.getBlockIdRaw(x, y, z) >> 8)) > 0) {
						extra = true;
					}
				}
			}
		}

		for (int i = 0; i < extraBlocksNibble.length; i++) {
			if (i * 2 + 1 < extraBlocks.length) {
				extraBlocksNibble[i] = (byte) ((extraBlocks[i * 2 + 0] << 4) | extraBlocks[i * 2 + 1]);
			} else {
				extraBlocksNibble[i] = (byte) (extraBlocks[i * 2 + 0] << 4);
			}
		}

		int count = 20;
		NBTTagList tileEntitiesList = new NBTTagList();
		for (TileEntity tileEntity : world.getTileEntities()) {
			NBTTagCompound tileEntityTagCompound = new NBTTagCompound();
			try {
				tileEntity.writeToNBT(tileEntityTagCompound);
				tileEntitiesList.appendTag(tileEntityTagCompound);
			} catch (Exception e) {
				int pos = tileEntity.xCoord + (tileEntity.yCoord * world.getLength() + tileEntity.zCoord) * world.getWidth();
				if (--count > 0) {
					Block block = world.getBlockRaw(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
					Reference.logger.error(String.format("Block %s[%s] with TileEntity %s failed to save! Replacing with bedrock...", block, block != null ? GameData.getBlockRegistry().getNameForObject(block) : "?", tileEntity.getClass().getName()), e);
				}
				localBlocks[pos] = (byte) GameData.getBlockRegistry().getId(Blocks.bedrock);
				localMetadata[pos] = 0;
				extraBlocks[pos] = 0;
			}
		}

		tagCompound.setString("Materials", "Alpha");
		tagCompound.setByteArray("Blocks", localBlocks);
		tagCompound.setByteArray("Data", localMetadata);
		if (extra) {
			tagCompound.setByteArray("AddBlocks", extraBlocksNibble);
		}
		tagCompound.setTag("Entities", new NBTTagList());
		tagCompound.setTag("TileEntities", tileEntitiesList);

		return true;
	}
}
