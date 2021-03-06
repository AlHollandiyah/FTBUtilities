package com.feed_the_beast.ftbutilities.ranks;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.server.permission.DefaultPermissionHandler;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.context.IContext;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author LatvianModder
 */
public enum FTBUPermissionHandler implements IPermissionHandler
{
	INSTANCE;

	@Override
	public void registerNode(String s, DefaultPermissionLevel defaultPermissionLevel, String s1)
	{
		DefaultPermissionHandler.INSTANCE.registerNode(s, defaultPermissionLevel, s1);
	}

	@Override
	public Collection<String> getRegisteredNodes()
	{
		return DefaultPermissionHandler.INSTANCE.getRegisteredNodes();
	}

	@Override
	public boolean hasPermission(GameProfile profile, String permission, @Nullable IContext context)
	{
		if (context != null && context.getWorld() != null && context.getWorld().isRemote)
		{
			return true;
		}

		switch (Ranks.INSTANCE.getRank(context != null && context.getWorld() != null ? context.getWorld().getMinecraftServer() : null, profile, context).hasPermission(permission))
		{
			case ALLOW:
				return true;
			case DENY:
				return false;
			default:
				return DefaultPermissionHandler.INSTANCE.hasPermission(profile, permission, context);
		}
	}

	@Override
	public String getNodeDescription(String s)
	{
		return DefaultPermissionHandler.INSTANCE.getNodeDescription(s);
	}
}