package com.minecolonies.permissions;

import com.minecolonies.api.permission.IPermissionKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Keys have a fixed syntax:
 * <ul>
 *     <li>block.break.[id]</li>
 *     <li>block.drop.[id]</li>
 *     <li>block.pickup.[id]</li>
 *     <li>block.place.[id]</li>
 *     <li>item.drop.[id]</li>
 *     <li>item.pickup.[id]</li>
 *     <li>item.use.[id]</li>
 * </ul>
 * 
 * The negate keyword is: ^
 */
public class PermissionKey implements IPermissionKey
{

    private static final String PATTERN = "/^(\\^?)block.(place|break).([a-zA-Z]+|\\*):([a-zA-Z,*]+)$/";
    
	private Map<String, Object> dataStorage;
    private String key;

    /**
     * Constructor.
     * 
     * @param key
     * @throws IllegalArgumentException If the key does not match the pattern.
     */
    public PermissionKey(String key)
    {
    	if (!key.matches(this.getRegex()))
    		throw new IllegalArgumentException("The given key does not match the pattern.");
    	
        this.key = key;
        dataStorage = new HashMap<>();
    }

    @Override
    public String serialize()
    {
        // TODO

        return null;
    }

    @Override
    public void deserialize()
    {
        // TODO
    }

    /**
     * Retrieves data stored in this data storage.
     *
     * @param key The key to retrieve
     * @param <T> The class to cast to
     * @return The cast object data
     */
    public <T> T getData(String key)
    {
        return (T) dataStorage.get(key);
    }

    /**
     * Stores custom data for this key.
     *
     * @param key The key for storage
     * @param data The data to store
     * @return True, if the data value didn't exist before.
     */
    public boolean setData(String key, Object data)
    {
        if (!dataStorage.containsKey(key))
        {
            return dataStorage.put(key, data) == null;
        }

        return false;
    }

    public boolean compare(IPermissionKey iPermKey)
    {
        if (this == iPermKey)
        {
            return true;
        }

        if (iPermKey instanceof PermissionKey)
        {
            PermissionKey permKey = (PermissionKey) iPermKey;

            if (this.key.matches(permKey.key))
            {
                return true;
            }
        }

        return false;
    }

	@Override
	public String getRegex() {
		return PermissionKey.PATTERN;
	}

}
