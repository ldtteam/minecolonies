package com.jlgm.structurepreview.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;


public class CommandSaveStructure implements ICommand
{

    private final List aliases;

    public CommandSaveStructure()
    {
        aliases = new ArrayList();
        aliases.add("structure");
        aliases.add("struct");
    }

    @Override
    public int compareTo(ICommand arg0)
    {
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "structure";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "structure <text>";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return this.aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length != 0)
        {
            /*BlockPos firstPos = TESTMain.instance.firstPos;
            BlockPos secondPos = TESTMain.instance.secondPos;
            if (!(firstPos == null || secondPos == null))
            {

                BlockPos blockpos =
                        new BlockPos(Math.min(firstPos.getX(), secondPos.getX()), Math.min(firstPos.getY(), secondPos.getY()), Math.min(firstPos.getZ(), secondPos.getZ()));
                BlockPos blockpos1 =
                        new BlockPos(Math.max(firstPos.getX(), secondPos.getX()), Math.max(firstPos.getY(), secondPos.getY()), Math.max(firstPos.getZ(), secondPos.getZ()));
                BlockPos size = blockpos1.subtract(blockpos).add(1, 1, 1);

                WorldServer worldserver = (WorldServer) sender.getEntityWorld();
                MinecraftServer minecraftserver = server;
                TemplateManager templatemanager = worldserver.getStructureTemplateManager();
                Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(args[0]));
                template.takeBlocksFromWorld(sender.getEntityWorld(), blockpos, size, true, Blocks.STRUCTURE_VOID);
                template.setAuthor(sender.getName());
                templatemanager.writeTemplate(minecraftserver, new ResourceLocation(args[0]));
                sender.addChatMessage(new TextComponentString("Structure scanned! " + firstPos.subtract(secondPos)));

                Structure structure = new Structure(null, args[0], new PlacementSettings().setRotation(StructPrevMath.getRotationFromYaw()).setMirror(Mirror.NONE));
                System.out.println(args[0] + " " + structure.getBlockInfo().length);
            }
            else
            {
                sender.addChatMessage(new TextComponentString("Select a range using the build scanner tool."));
            }*/
        }
        else
        {
            sender.addChatMessage(new TextComponentString("Invalid argument."));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public List<String> getTabCompletionOptions(
            MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos pos)
    {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return false;
    }

	
	/*Load structure
			if(args.length != 0){
			Structure structure = new Structure(null, args[0], new PlacementSettings().setRotation(StructPrevMath.getRotationFromYaw()).setMirror(Mirror.NONE));
			if(structure.doesExist()){
				
			}else{
				sender.addChatMessage(new TextComponentString(args[0] + " doesn't exists."));
			}
		}else{
			sender.addChatMessage(new TextComponentString("Invalid argument"));
		}
	 */
}
