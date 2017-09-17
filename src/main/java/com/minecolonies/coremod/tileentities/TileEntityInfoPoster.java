package com.minecolonies.coremod.tileentities;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileEntityInfoPoster extends TileEntity
{
    public final ITextComponent[] signText =
            new ITextComponent[] {new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")};

    /**
     * The index of the line currently being edited. Only used on client side, but defined on both. Note this is only
     * really used when the > < are going to be visible.
     */
    public  int     lineBeingEdited = -1;
    private boolean isEditable      = true;
    private EntityPlayer player;
    private final CommandResultStats stats = new CommandResultStats();

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        for (int i = 0; i < 4; ++i)
        {
            String s = ITextComponent.Serializer.componentToJson(this.signText[i]);
            compound.setString("Text" + (i + 1), s);
        }

        this.stats.writeStatsToNBT(compound);
        return compound;
    }

    protected void setWorldCreate(World worldIn)
    {
        this.setWorldObj(worldIn);
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        this.isEditable = false;
        super.readFromNBT(compound);
        ICommandSender icommandsender = new ICommandSender()
        {
            /**
             * Get the name of this object. For players this returns their username
             */
            public String getName()
            {
                return "Sign";
            }

            /**
             * Get the formatted ChatComponent that will be used for the sender's username in chat
             */
            public ITextComponent getDisplayName()
            {
                return new TextComponentString(this.getName());
            }

            /**
             * Send a chat message to the CommandSender
             */
            public void addChatMessage(ITextComponent component)
            {
            }

            /**
             * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
             */
            public boolean canCommandSenderUseCommand(int permLevel, String commandName)
            {
                return permLevel <= 2; //Forge: Fixes  MC-75630 - Exploit with signs and command blocks
            }

            /**
             * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world,
             * return the coordinates 0, 0, 0
             */
            public BlockPos getPosition()
            {
                return TileEntityInfoPoster.this.pos;
            }

            /**
             * Get the position vector. <b>{@code null} is not allowed!</b> If you are not an entity in the world,
             * return 0.0D, 0.0D, 0.0D
             */
            public Vec3d getPositionVector()
            {
                return new Vec3d((double) TileEntityInfoPoster.this.pos.getX() + 0.5D,
                        (double) TileEntityInfoPoster.this.pos.getY() + 0.5D,
                        (double) TileEntityInfoPoster.this.pos.getZ() + 0.5D);
            }

            /**
             * Get the world, if available. <b>{@code null} is not allowed!</b> If you are not an entity in the world,
             * return the overworld
             */
            public World getEntityWorld()
            {
                return TileEntityInfoPoster.this.worldObj;
            }

            /**
             * Returns the entity associated with the command sender. MAY BE NULL!
             */
            public Entity getCommandSenderEntity()
            {
                return null;
            }

            /**
             * Returns true if the command sender should be sent feedback about executed commands
             */
            public boolean sendCommandFeedback()
            {
                return false;
            }

            public void setCommandStat(CommandResultStats.Type type, int amount)
            {
            }

            /**
             * Get the Minecraft server instance
             */
            public MinecraftServer getServer()
            {
                return TileEntityInfoPoster.this.worldObj.getMinecraftServer();
            }
        };

        for (int i = 0; i < 4; ++i)
        {
            String s = compound.getString("Text" + (i + 1));
            ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(s);

            try
            {
                this.signText[i] = TextComponentUtils.processComponent(icommandsender, itextcomponent, (Entity) null);
            }
            catch (CommandException var7)
            {
                this.signText[i] = itextcomponent;
            }
        }

        this.stats.readStatsFromNBT(compound);
    }

    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, 9, this.getUpdateTag());
    }

    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    public boolean onlyOpsCanSetNbt()
    {
        return true;
    }

    public boolean getIsEditable()
    {
        return this.isEditable;
    }

    /**
     * Sets the sign's isEditable flag to the specified parameter.
     */
    @SideOnly(Side.CLIENT)
    public void setEditable(boolean isEditableIn)
    {
        this.isEditable = isEditableIn;

        if (!isEditableIn)
        {
            this.player = null;
        }
    }

    public void setPlayer(EntityPlayer playerIn)
    {
        this.player = playerIn;
    }

    public EntityPlayer getPlayer()
    {
        return this.player;
    }

    public boolean executeCommand(final EntityPlayer playerIn)
    {
        ICommandSender icommandsender = new ICommandSender()
        {
            /**
             * Get the name of this object. For players this returns their username
             */
            public String getName()
            {
                return playerIn.getName();
            }

            /**
             * Get the formatted ChatComponent that will be used for the sender's username in chat
             */
            public ITextComponent getDisplayName()
            {
                return playerIn.getDisplayName();
            }

            /**
             * Send a chat message to the CommandSender
             */
            public void addChatMessage(ITextComponent component)
            {
            }

            /**
             * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
             */
            public boolean canCommandSenderUseCommand(int permLevel, String commandName)
            {
                return permLevel <= 2;
            }

            /**
             * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world,
             * return the coordinates 0, 0, 0
             */
            public BlockPos getPosition()
            {
                return TileEntityInfoPoster.this.pos;
            }

            /**
             * Get the position vector. <b>{@code null} is not allowed!</b> If you are not an entity in the world,
             * return 0.0D, 0.0D, 0.0D
             */
            public Vec3d getPositionVector()
            {
                return new Vec3d((double) TileEntityInfoPoster.this.pos.getX() + 0.5D,
                        (double) TileEntityInfoPoster.this.pos.getY() + 0.5D,
                        (double) TileEntityInfoPoster.this.pos.getZ() + 0.5D);
            }

            /**
             * Get the world, if available. <b>{@code null} is not allowed!</b> If you are not an entity in the world,
             * return the overworld
             */
            public World getEntityWorld()
            {
                return playerIn.getEntityWorld();
            }

            /**
             * Returns the entity associated with the command sender. MAY BE NULL!
             */
            public Entity getCommandSenderEntity()
            {
                return playerIn;
            }

            /**
             * Returns true if the command sender should be sent feedback about executed commands
             */
            public boolean sendCommandFeedback()
            {
                return false;
            }

            public void setCommandStat(CommandResultStats.Type type, int amount)
            {
                if (TileEntityInfoPoster.this.worldObj != null && !TileEntityInfoPoster.this.worldObj.isRemote)
                {
                    TileEntityInfoPoster.this.stats.setCommandStatForSender(TileEntityInfoPoster.this.worldObj.getMinecraftServer(), this, type, amount);
                }
            }

            /**
             * Get the Minecraft server instance
             */
            public MinecraftServer getServer()
            {
                return playerIn.getServer();
            }
        };

        for (ITextComponent itextcomponent : this.signText)
        {
            Style style = itextcomponent == null ? null : itextcomponent.getStyle();

            if (style != null && style.getClickEvent() != null)
            {
                ClickEvent clickevent = style.getClickEvent();

                if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND)
                {
                    //todo doesn't seem to reach here!
                    playerIn.getServer().getCommandManager().executeCommand(icommandsender, clickevent.getValue());
                }
            }
        }

        return true;
    }

    public CommandResultStats getStats()
    {
        return this.stats;
    }
}
