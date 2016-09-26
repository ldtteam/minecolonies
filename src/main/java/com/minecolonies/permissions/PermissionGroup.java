package com.minecolonies.permissions;

import java.util.List;

import com.minecolonies.api.permission.IPermissionGroup;
import com.minecolonies.api.permission.IPermissionKey;

/**
 */
public class PermissionGroup implements IPermissionGroup {

	private List<PermissionKey> keys;

	@Override
	public String serialize() {
		// TODO
		return null;
	}

	@Override
	public void deserialize(final String data) {

	}

	@Override
	public void addKey(IPermissionKey key) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean hasKey(IPermissionKey key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeKey(IPermissionKey key) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addKey(String key) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean hasKey(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeKey(String key) {
		// TODO Auto-generated method stub
	}

}
