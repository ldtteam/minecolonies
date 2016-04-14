package com.schematica.world;

import com.minecolonies.util.Log;
import com.schematica.config.BlockInfo;
import com.schematica.world.storage.EmptySaveHandler;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.util.vector.Vector3f;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SchematicWorld extends World
{
    // private static final AnvilSaveHandler SAVE_HANDLER = new AnvilSaveHandler(Minecraft.getMinecraft().mcDataDir, "tmp/com.github.lunatrius.schematica", false);
    private static final WorldSettings         WORLD_SETTINGS   = new WorldSettings(0, WorldSettings.GameType.CREATIVE, false, false, WorldType.FLAT);
    private static final Comparator<ItemStack> BLOCK_COMPARATOR = new Comparator<ItemStack>()
    {
        @Override
        public int compare(ItemStack itemStackA, ItemStack itemStackB)
        {
            return itemStackA.getUnlocalizedName().compareTo(itemStackB.getUnlocalizedName());
        }
    };

    public static final ItemStack DEFAULT_ICON = new ItemStack(Blocks.grass);

    private ItemStack   icon;
    private short[][][] blocks;
    private byte[][][]  metadata;
    private final List<TileEntity> tileEntities = new ArrayList<>();
    private final List<ItemStack>  blockList    = new ArrayList<>();
    private short width;
    private short length;
    private short height;

    private boolean isRendering;
    private int     renderingLayer;

    private int xOffset = 0, yOffset = 0, zOffset = 0;

    public SchematicWorld()
    {
        super(new EmptySaveHandler(), "Schematica", WORLD_SETTINGS, null, null);
        this.icon = SchematicWorld.DEFAULT_ICON.copy();
        this.blocks = null;
        this.metadata = null;
        this.tileEntities.clear();
        this.width = 0;
        this.length = 0;
        this.height = 0;

        this.isRendering = false;
        this.renderingLayer = -1;
    }

    public SchematicWorld(ItemStack icon, short[][][] blocks, byte[][][] metadata, List<TileEntity> tileEntities, NBTTagList entities, short width, short height, short length, int xOffset, int yOffset, int zOffset)
    {
        this(icon, blocks, metadata, tileEntities, entities, width, height, length);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    public SchematicWorld(ItemStack icon, short[][][] blocks, byte[][][] metadata, List<TileEntity> tileEntities, NBTTagList entities, short width, short height, short length)
    {
        this();

        this.icon = icon != null ? icon : SchematicWorld.DEFAULT_ICON.copy();

        this.blocks = blocks.clone();
        this.metadata = metadata.clone();

        if(tileEntities != null)
        {
            this.tileEntities.addAll(tileEntities);
            for(TileEntity tileEntity : this.tileEntities)
            {
                tileEntity.setWorldObj(this);
                try
                {
                    tileEntity.validate();
                }
                catch(Exception e)
                {
                    Log.logger.error(String.format("TileEntity validation for %s failed!", tileEntity.getClass()), e);
                }
            }
        }

        if(entities != null)
        {
            for(int i = 0; i < entities.tagCount(); i++)
            {
                Entity entity = EntityList.createEntityFromNBT(entities.getCompoundTagAt(i), this);
                this.loadedEntityList.add(entity);
            }
        }

        this.width = width;
        this.length = length;
        this.height = height;

        generateBlockList();
    }

    public SchematicWorld(String iconName, short[][][] blocks, byte[][][] metadata, List<TileEntity> tileEntities, NBTTagList entities, short width, short height, short length)
    {
        this(getIconFromName(iconName), blocks, metadata, tileEntities, entities, width, height, length);
    }

    public static ItemStack getIconFromName(String iconName)
    {
        ItemStack icon;
        String name = "";
        int damage = 0;

        String[] parts = iconName.split(",");
        if(parts.length >= 1)
        {
            name = parts[0];
            if(parts.length >= 2)
            {
                try
                {
                    damage = Integer.parseInt(parts[1]);
                }
                catch(NumberFormatException ignored)
                {
                }
            }
        }

        icon = new ItemStack(GameData.getBlockRegistry().getObject(name), 1, damage);
        if(icon.getItem() != null)
        {
            return icon;
        }

        icon = new ItemStack(GameData.getItemRegistry().getObject(name), 1, damage);
        if(icon.getItem() != null)
        {
            return icon;
        }

        return SchematicWorld.DEFAULT_ICON.copy();
    }

    public static ItemStack getIconFromNBT(NBTTagCompound tagCompound)
    {
        ItemStack icon = SchematicWorld.DEFAULT_ICON.copy();

        if(tagCompound != null && tagCompound.hasKey("Icon"))
        {
            icon.readFromNBT(tagCompound.getCompoundTag("Icon"));

            if(icon.getItem() == null)
            {
                icon = SchematicWorld.DEFAULT_ICON.copy();
            }
        }

        return icon;
    }

    public static ItemStack getIconFromFile(File file)
    {
        try
        {
            InputStream stream = new FileInputStream(file);
            NBTTagCompound tagCompound = CompressedStreamTools.readCompressed(stream);

            return getIconFromNBT(tagCompound);
        }
        catch(Exception e)
        {
            Log.logger.error("Failed to read schematic icon!", e);
        }

        return SchematicWorld.DEFAULT_ICON.copy();
    }

    private void generateBlockList()
    {
        this.blockList.clear();

        int x, y, z, itemDamage;
        Block block;
        Item item;
        ItemStack itemStack;

        for(y = 0; y < this.height; y++)
        {
            for(z = 0; z < this.length; z++)
            {
                for(x = 0; x < this.width; x++)
                {
                    block = this.getBlock(x, y, z);
                    item = Item.getItemFromBlock(block);
                    itemDamage = this.metadata[x][y][z];

                    if(block == null || block == Blocks.air)
                    {
                        continue;
                    }

                    if(BlockInfo.BLOCK_LIST_IGNORE_BLOCK.contains(block))
                    {
                        continue;
                    }

                    if(BlockInfo.BLOCK_LIST_IGNORE_METADATA.contains(block))
                    {
                        itemDamage = 0;
                    }

                    if(BlockInfo.BLOCK_ITEM_MAP.containsKey(block))
                    {
                        item = BlockInfo.BLOCK_ITEM_MAP.get(block);
                        Block blockFromItem = Block.getBlockFromItem(item);
                        if(blockFromItem != Blocks.air)
                        {
                            block = blockFromItem;
                        }
                        else
                        {
                            itemDamage = 0;
                        }
                    }

                    if(block instanceof BlockLog || block instanceof BlockLeavesBase)
                    {
                        itemDamage &= 0x03;
                    }

                    if(block instanceof BlockSlab)
                    {
                        itemDamage &= 0x07;
                    }

                    if(block instanceof BlockDoublePlant)
                    {
                        if((itemDamage & 0x08) == 0x08)
                        {
                            continue;
                        }
                    }

                    if(block == Blocks.cocoa)
                    {
                        itemDamage = 0x03;
                    }

                    if(item == Items.skull)
                    {
                        TileEntity tileEntity = getTileEntity(x, y, z);
                        if(tileEntity instanceof TileEntitySkull)
                        {
                            itemDamage = ((TileEntitySkull) tileEntity).func_145904_a();
                        }
                    }

                    itemStack = null;
                    for(ItemStack stack : this.blockList)
                    {
                        if(stack.getItem() == item && stack.getItemDamage() == itemDamage)
                        {
                            itemStack = stack;
                            itemStack.stackSize++;
                            break;
                        }
                    }

                    if(itemStack == null)
                    {
                        itemStack = new ItemStack(item, 1, itemDamage);
                        if(itemStack.getItem() != null)
                        {
                            this.blockList.add(itemStack);
                        }
                    }
                }
            }
        }
        Collections.sort(this.blockList, BLOCK_COMPARATOR);
    }

    public int getBlockIdRaw(int x, int y, int z)
    {
        if(x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length)
        {
            return 0;
        }
        return this.blocks[x][y][z];
    }

    private int getBlockId(int x, int y, int z)
    {
        if(getRenderingLayer() != -1 && getRenderingLayer() != y)
        {
            return 0;
        }
        return getBlockIdRaw(x, y, z);
    }

    public Block getBlockRaw(int x, int y, int z)
    {
        return GameData.getBlockRegistry().getObjectById(getBlockIdRaw(x, y, z));
    }

    @Override
    public Block getBlock(int x, int y, int z)
    {
        return GameData.getBlockRegistry().getObjectById(getBlockId(x, y, z));
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        for(TileEntity tileEntity : this.tileEntities)
        {
            if(tileEntity.getPos() == pos)
            {
                return tileEntity;
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSkyBlockTypeBrightness(EnumSkyBlock skyBlock, int x, int y, int z)
    {
        return 15;
    }

    @Override
    public float getLightBrightness(BlockPos pos)
    {
        return 1.0f;
    }

    @Override
    public int getBlockMetadata(int x, int y, int z)
    {
        if(x < 0 || y < 0 || z < 0 || x >= this.width || y >= this.height || z >= this.length)
        {
            return 0;
        }
        return this.metadata[x][y][z];
    }

    @Override
    public boolean isBlockNormalCubeDefault(int x, int y, int z, boolean _default)
    {
        Block block = getBlock(x, y, z);
        if(block == null)
        {
            return false;
        }
        if(block.isNormalCube())
        {
            return true;
        }
        return _default;
    }

    @Override
    protected int func_152379_p()
    {
        return -1;//Render distance - view distance
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        Block block = getBlockState(pos).getBlock();
        if(block == null)
        {
            return true;
        }
        return block.isAir(this, pos);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(final BlockPos pos)
    {
        return BiomeGenBase.jungle;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getLength()
    {
        return this.length;
    }

    @Override
    public int getHeight()
    {
        return this.height;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean extendedLevelsInChunkCache()
    {
        return false;
    }

    @Override
    protected IChunkProvider createChunkProvider()
    {
        return null;
    }

    @Override
    public Entity getEntityByID(int id)
    {
        return null;
    }

    @Override
    public boolean blockExists(int x, int y, int z)
    {
        return false;
    }

    @Override
    public boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag)
    {
        this.metadata[x][y][z] = (byte) (metadata & 0xFF);
        return true;
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side)
    {
        return isSideSolid(x, y, z, side, false);
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default)
    {
        Block block = getBlock(x, y, z);
        if(block == null)
        {
            return false;
        }
        return block.isSideSolid(this, x, y, z, side);
    }

    public ItemStack getIcon()
    {
        return this.icon;
    }

    public void setTileEntities(List<TileEntity> tileEntities)
    {
        this.tileEntities.clear();
        this.tileEntities.addAll(tileEntities);
        for(TileEntity tileEntity : this.tileEntities)
        {
            tileEntity.setWorldObj(this);
            try
            {
                tileEntity.validate();
            }
            catch(Exception e)
            {
                Log.logger.error(String.format("TileEntity validation for %s failed!", tileEntity.getClass()), e);
            }
        }
    }

    public List<TileEntity> getTileEntities()
    {
        return this.tileEntities;
    }

    public List<ItemStack> getBlockList()
    {
        return this.blockList;
    }

    public boolean toggleRendering()
    {
        this.isRendering = !this.isRendering;
        return this.isRendering;
    }

    public boolean isRendering()
    {
        return this.isRendering;
    }

    public void setRendering(boolean isRendering)
    {
        this.isRendering = isRendering;
    }

    public int getRenderingLayer()
    {
        return this.renderingLayer;
    }

    public void setRenderingLayer(int renderingLayer)
    {
        this.renderingLayer = renderingLayer;
    }

    public void decrementRenderingLayer()
    {
        this.renderingLayer = MathHelper.clamp_int(this.renderingLayer - 1, -1, getHeight() - 1);
    }

    public void incrementRenderingLayer()
    {
        this.renderingLayer = MathHelper.clamp_int(this.renderingLayer + 1, -1, getHeight() - 1);
    }

    public void refreshChests()
    {
        for(TileEntity tileEntity : this.tileEntities)
        {
            if(tileEntity instanceof TileEntityChest)
            {
                TileEntityChest tileEntityChest = (TileEntityChest) tileEntity;
                tileEntityChest.adjacentChestChecked = false;
                tileEntityChest.checkForAdjacentChests();
            }
        }
    }

    public void flip()
    {
        /*
        int tmp;
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				for (int z = 0; z < (this.length + 1) / 2; z++) {
					tmp = this.blocks[x][y][z];
					this.blocks[x][y][z] = this.blocks[x][y][this.length - 1 - z];
					this.blocks[x][y][this.length - 1 - z] = tmp;

					if (z == this.length - 1 - z) {
						this.metadata[x][y][z] = BlockInfo.getTransformedMetadataFlip(this.blocks[x][y][z], this.metadata[x][y][z]);
					} else {
						tmp = this.metadata[x][y][z];
						this.metadata[x][y][z] = BlockInfo.getTransformedMetadataFlip(this.blocks[x][y][z], this.metadata[x][y][this.length - 1 - z]);
						this.metadata[x][y][this.length - 1 - z] = BlockInfo.getTransformedMetadataFlip(this.blocks[x][y][this.length - 1 - z], tmp);
					}
				}
			}
		}

		TileEntity tileEntity;
		for (int i = 0; i < this.tileEntities.size(); i++) {
			tileEntity = this.tileEntities.get(i);
			tileEntity.zCoord = this.length - 1 - tileEntity.zCoord;
			tileEntity.blockMetadata = this.metadata[tileEntity.xCoord][tileEntity.yCoord][tileEntity.zCoord];

			if (tileEntity instanceof TileEntitySkull && tileEntity.blockMetadata == 0x1) {
				TileEntitySkull skullTileEntity = (TileEntitySkull) tileEntity;
				int angle = skullTileEntity.func_82119_b();
				int base = 0;
				if (angle <= 7) {
					base = 4;
				} else {
					base = 12;
				}

				skullTileEntity.setSkullRotation((2 * base - angle) & 15);
			}
		}

		refreshChests();
		*/
    }

    public void rotate()
    {
        short[][][] localBlocks = new short[this.length][this.height][this.width];
        byte[][][] localMetadata = new byte[this.length][this.height][this.width];

        for(int y = 0; y < this.height; y++)
        {
            for(int z = 0; z < this.length; z++)
            {
                for(int x = 0; x < this.width; x++)
                {
                    getBlock(x, y, this.length - 1 - z).rotateBlock(this, x, y, this.length - 1 - z, ForgeDirection.UP);
                    localBlocks[z][y][x] = this.blocks[x][y][this.length - 1 - z];
                    localMetadata[z][y][x] = this.metadata[x][y][this.length - 1 - z];
                }
            }
        }

        this.blocks = localBlocks;
        this.metadata = localMetadata;

        int coord;
        for(TileEntity tileEntity : this.tileEntities)
        {
            coord = tileEntity.zCoord;
            tileEntity.zCoord = tileEntity.xCoord;
            tileEntity.xCoord = this.length - 1 - coord;
            tileEntity.blockMetadata = this.metadata[tileEntity.xCoord][tileEntity.yCoord][tileEntity.zCoord];

            if(tileEntity instanceof TileEntitySkull && tileEntity.blockMetadata == 0x1)
            {
                TileEntitySkull skullTileEntity = (TileEntitySkull) tileEntity;
                skullTileEntity.func_145903_a((skullTileEntity.func_145906_b() + 12) & 15);
            }
        }

        int tempZ = zOffset;
        zOffset = xOffset;
        xOffset = this.length - 1 - tempZ;

        short tmp = this.width;
        this.width = this.length;
        this.length = tmp;

        refreshChests();
    }

    public Vector3f dimensions()
    {
        return new Vector3f(this.width, this.height, this.length);
    }

    public void removeFromBlockList(ItemStack stack)
    {
        for(ItemStack blockStack : blockList)
        {
            if(blockStack.isItemEqual(stack))
            {
                int index = blockList.indexOf(blockStack);
                blockStack.stackSize--;
                if(blockStack.stackSize == 0)
                {
                    blockList.remove(index);
                }
                return;
            }
        }
    }

    public boolean hasOffset()
    {
        return xOffset != 0 || yOffset != 0 || zOffset != 0;
    }

    public int getOffsetX()
    {
        return xOffset;
    }

    public int getOffsetY()
    {
        return yOffset;
    }

    public int getOffsetZ()
    {
        return zOffset;
    }

    public String getType()
    {
        if(hasOffset())
        {
            return "Hut";
        }
        return "Decoration";
    }
}
