package com.minecolonies.event;

import com.minecolonies.blocks.BlockHut;
import com.minecolonies.blocks.BlockHutTownHall;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.IColony;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.entity.PlayerProperties;
import com.minecolonies.entity.pathfinding.Node;
import com.minecolonies.entity.pathfinding.Pathfinding;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Utils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.lwjgl.opengl.GL11;

public class EventHandler
{
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
        World world = event.world;

        if(!world.isRemote && event.block instanceof BlockHut)
        {
            Building building = ColonyManager.getBuilding(world, event.x, event.y, event.z);
            if (building == null)
            {
                return;
            }

            if (!building.getColony().getPermissions().hasPermission(event.getPlayer(), Permissions.Action.BREAK_HUTS))
            {
                event.setCanceled(true);
                return;
            }

            building.destroy();
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
        {
            EntityPlayer player = event.entityPlayer;
            World world = event.world;
            int x = event.x, y = event.y, z = event.z;

            if(!player.isSneaking() || player.getHeldItem() == null || player.getHeldItem().getItem() == null || player.getHeldItem().getItem().doesSneakBypassUse(world, x, y, z, player))
            {
                if(world.getBlock(x, y, z) instanceof BlockHut)//this was the simple way of doing it, minecraft calls onBlockActivated
                {                                              // and uses that return value, but I didn't want to call it twice
                    IColony colony = ColonyManager.getIColony(world, x, y, z);
                    if (colony != null &&
                            !colony.getPermissions().hasPermission(player, Permissions.Action.ACCESS_HUTS))
                    {
                        event.setCanceled(true);
                    }

                    return;
                }
            }

            if(player.getHeldItem() == null || player.getHeldItem().getItem() == null) return;

            Block heldBlock = Block.getBlockFromItem(player.getHeldItem().getItem());
            if(heldBlock instanceof BlockHut)
            {
                switch(event.face)
                {
                    case 0:
                        y--;
                        break;
                    case 1:
                        y++;
                        break;
                    case 2:
                        z--;
                        break;
                    case 3:
                        z++;
                        break;
                    case 4:
                        x--;
                        break;
                    case 5:
                        x++;
                        break;
                }
                event.setCanceled(!onBlockHutPlaced(event.world, player, heldBlock, x, y, z));
            }
        }
    }

    /**
     * Called when a player tries to place a BlockHut. Returns true if successful and false to cancel the block placement.
     *
     * @param world  The world the player is in
     * @param player The player
     * @param block  The block type the player is placing
     * @param x      The x coordinate of the block
     * @param y      The y coordinate of the block
     * @param z      The z coordinate of the block
     * @return false to cancel the event
     */
    private boolean onBlockHutPlaced(World world, EntityPlayer player, Block block, int x, int y, int z)//TODO use permissions
    {
        //  Check if this Hut Block can be placed
        if (block instanceof BlockHutTownHall)
        {
            IColony colony = ColonyManager.getClosestIColony(world, x, y, z);
            if (colony != null)
            {
                //  Town Halls must be far enough apart
                if (colony.isCoordInColony(world, x, y, z))
                {
                    if (colony.hasTownhall())
                    {
                        //  Placing in a colony which already has a town hall
                        LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownhall.messageTooClose");
                        return false;
                    }
                    else if (!colony.getPermissions().hasPermission(player, Permissions.Action.PLACE_HUTS))
                    {
                        //  No permission to place hut in colony
                        LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoPermission");
                        return false;
                    }
                }
                else if (colony.getDistanceSquared(x, y, z) <= Utils.square(ColonyManager.getMinimumDistanceBetweenTownHalls()))
                {
                    //  Placing too close to an existing colony
                    LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownhall.messageTooClose");
                    return false;
                }
            }
            else if (!ColonyManager.getIColoniesByOwner(world, player).isEmpty())
            {
                //  Players are currently only allowed a single colony
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHutTownhall.messagePlacedAlready");
                return false;
            }
        }
        else //  Not a Townhall
        {
            IColony colony = ColonyManager.getIColony(world, x, y, z);

            if (colony == null)
            {
                //  Not in a colony
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoTownhall");
                return false;
            }
//            else if (!colony.isCoordInColony(world, x, y, z))
//            {
//                //  Not close enough to colony
//                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageTooFarFromTownhall");
//                return false;
//            }
            else if (!colony.getPermissions().hasPermission(player, Permissions.Action.PLACE_HUTS))
            {
                //  No permission to place hut in colony
                LanguageHandler.sendPlayerLocalizedMessage(player, "tile.blockHut.messageNoPermission");
                return false;
            }
        }

        return true;
    }

    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event)
    {
        if(event.entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.entity;
            if(PlayerProperties.get(player) == null)
            {
                PlayerProperties.register(player);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
        {
            PlayerProperties.saveProxyData((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event)
    {
        if(!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
        {
            PlayerProperties.loadProxyData((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        ColonyManager.onWorldLoad(event.world);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        ColonyManager.onWorldUnload(event.world);
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event)
    {
        ColonyManager.onWorldSave(event.world);
    }

    public void drawNode(Node n, byte r, byte g, byte b)
    {
        GL11.glPushMatrix();
        GL11.glTranslated((double) n.x + 0.375, (double) n.y + 0.375, (double) n.z + 0.375);

        float f = 1.6F;
        float f1 = 0.016666668F * f / 2;

        //  Nameplate

        Entity entity = Minecraft.getMinecraft().renderViewEntity;
        double dx = n.x - entity.posX;
        double dy = n.y - entity.posY;
        double dz = n.z - entity.posZ;
        if (Math.sqrt(dx*dx + dy*dy + dz*dz) <= 5D)
        {
            String s1 = String.format("F: %.3f [%d]", n.cost, n.counterAdded);
            String s2 = String.format("G: %.3f [%d]", n.score, n.counterVisited);
            FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 0.75F, 0.0F);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-f1, -f1, f1);
            GL11.glTranslatef(0.0F, 0.25F / f1, 0.0F);
            GL11.glDepthMask(false);
            Tessellator tessellator = Tessellator.instance;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            tessellator.startDrawingQuads();
            int i = Math.max(fontrenderer.getStringWidth(s1), fontrenderer.getStringWidth(s2)) / 2;
            tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
            tessellator.addVertex((double) (-i - 1), -5.0D, 0.0D);
            tessellator.addVertex((double) (-i - 1), 12.0D, 0.0D);
            tessellator.addVertex((double) (i + 1), 12.0D, 0.0D);
            tessellator.addVertex((double) (i + 1), -5.0D, 0.0D);
            tessellator.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(true);
            GL11.glTranslatef(0.0F, -5F, 0.0F);
            fontrenderer.drawString(s1, -fontrenderer.getStringWidth(s1) / 2, 0, 553648127);
            GL11.glTranslatef(0.0F, 8F, 0.0F);
            fontrenderer.drawString(s2, -fontrenderer.getStringWidth(s2) / 2, 0, 553648127);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }

        GL11.glScaled(0.25, 0.25, 0.25);

        //Tessellator tessellator = Tessellator.instance;
        //tessellator.startDrawingQuads();

        //tessellator.setColorOpaque(r, g, b);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor3ub(r, g, b);

        //  X+ Facing
        //tessellator.setNormal(1.0F, 0.0F, 0.0F);
        GL11.glVertex3d(1.0, 0.0, 0.0);
        GL11.glVertex3d(1.0, 1.0, 0.0);
        GL11.glVertex3d(1.0, 1.0, 1.0);
        GL11.glVertex3d(1.0, 0.0, 1.0);

        //  X- Facing
        //tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        GL11.glVertex3d(0.0, 0.0, 1.0);
        GL11.glVertex3d(0.0, 1.0, 1.0);
        GL11.glVertex3d(0.0, 1.0, 0.0);
        GL11.glVertex3d(0.0, 0.0, 0.0);

        //  Z-
        //tessellator.setNormal(0.0F, 0.0F, -1.0F);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, 1.0, 0.0);
        GL11.glVertex3d(1.0, 1.0, 0.0);
        GL11.glVertex3d(1.0, 0.0, 0.0);

        //  Z+
        //tessellator.setNormal(0.0F, 0.0F, 1.0F);
        GL11.glVertex3d(1.0, 0.0, 1.0);
        GL11.glVertex3d(1.0, 1.0, 1.0);
        GL11.glVertex3d(0.0, 1.0, 1.0);
        GL11.glVertex3d(0.0, 0.0, 1.0);

        //  Y+
        //tessellator.setNormal(0.0F, 1.0F, 0.0F);
        GL11.glVertex3d(1.0, 1.0, 1.0);
        GL11.glVertex3d(1.0, 1.0, 0.0);
        GL11.glVertex3d(0.0, 1.0, 0.0);
        GL11.glVertex3d(0.0, 1.0, 1.0);

        //  Y-
        //tessellator.setNormal(0.0F, -1.0F, 0.0F);
        GL11.glVertex3d(0.0, 0.0, 1.0);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(1.0, 0.0, 0.0);
        GL11.glVertex3d(1.0, 0.0, 1.0);

        //tessellator.draw();
        GL11.glEnd();

        if (n.parent != null)
        {
            GL11.glBegin(GL11.GL_LINES);
            GL11.glColor3f(0.75F, 0.75F, 0.75F);

            double pdx = n.parent.x - n.x + 0.125;
            double pdy = n.parent.y - n.y + 0.125;
            double pdz = n.parent.z - n.z + 0.125;

            GL11.glVertex3d(0.5, 0.5, 0.5);
            GL11.glVertex3d(pdx / 0.25, pdy / 0.25, pdz / 0.25);

            GL11.glEnd();
        }

        GL11.glPopMatrix();
    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent event)
    {
        Pathfinding.debugDraw(event.partialTicks);
    }
}
