package com.github.lunatrius.schematica.world.schematic;

import com.github.lunatrius.schematica.world.SchematicWorld;
import net.minecraft.nbt.NBTTagCompound;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

// TODO: http://minecraft.gamepedia.com/Data_values_%28Classic%29
public class SchematicClassic extends SchematicFormat {
	@Override
	public SchematicWorld readFromNBT(NBTTagCompound tagCompound) {
		throw new NotImplementedException();
	}

	@Override
	public boolean writeToNBT(NBTTagCompound tagCompound, SchematicWorld world) {
		throw new NotImplementedException();
	}
}
