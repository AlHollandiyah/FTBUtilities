package com.feed_the_beast.ftbutilities.net;

import com.feed_the_beast.ftblib.FTBLibNotifications;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbutilities.FTBUtilitiesPermissions;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.feed_the_beast.ftbutilities.data.FTBUTeamData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.Collection;

public class MessageClaimedChunksModify extends MessageToServer<MessageClaimedChunksModify>
{
	public static final int CLAIM = 0;
	public static final int UNCLAIM = 1;
	public static final int LOAD = 2;
	public static final int UNLOAD = 3;

	private int startX, startZ;
	private int action;
	private Collection<ChunkPos> chunks;

	public MessageClaimedChunksModify()
	{
	}

	public MessageClaimedChunksModify(int sx, int sz, int a, Collection<ChunkPos> c)
	{
		startX = sx;
		startZ = sz;
		action = a;
		chunks = c;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBUNetHandler.CLAIMS;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(startX);
		data.writeInt(startZ);
		data.writeByte(action);
		data.writeCollection(chunks, DataOut.CHUNK_POS);
	}

	@Override
	public void readData(DataIn data)
	{
		startX = data.readInt();
		startZ = data.readInt();
		action = data.readUnsignedByte();
		chunks = data.readCollection(null, DataIn.CHUNK_POS);
	}

	@Override
	public void onMessage(MessageClaimedChunksModify m, EntityPlayer player)
	{
		if (ClaimedChunks.instance == null)
		{
			return;
		}

		ForgePlayer p = ClaimedChunks.instance.universe.getPlayer(player);

		if (!p.hasTeam())
		{
			FTBLibNotifications.NO_TEAM.send(((EntityPlayerMP) player).mcServer, player);
			return;
		}

		boolean canUnclaim = m.action == UNCLAIM && PermissionAPI.hasPermission(player, FTBUtilitiesPermissions.CLAIMS_CHUNKS_MODIFY_OTHERS);

		for (ChunkPos pos0 : m.chunks)
		{
			ChunkDimPos pos = new ChunkDimPos(pos0, player.dimension);

			switch (m.action)
			{
				case CLAIM:
					ClaimedChunks.instance.claimChunk(FTBUTeamData.get(p.team), pos);
					break;
				case UNCLAIM:
					if (canUnclaim || p.team.equalsTeam(ClaimedChunks.instance.getChunkTeam(pos)))
					{
						ClaimedChunks.instance.unclaimChunk(p.team, pos);
					}
					break;
				case LOAD:
					ClaimedChunks.instance.setLoaded(p.team, pos, true);
					break;
				case UNLOAD:
					ClaimedChunks.instance.setLoaded(p.team, pos, false);
					break;
			}
		}
	}
}