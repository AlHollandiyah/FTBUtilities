package com.feed_the_beast.ftbutilities.data;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftblib.lib.util.ServerUtils;
import com.feed_the_beast.ftbutilities.FTBUtilities;
import com.feed_the_beast.ftbutilities.FTBUtilitiesConfig;
import com.feed_the_beast.ftbutilities.FTBUtilitiesPermissions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.IContext;
import net.minecraftforge.server.permission.context.WorldContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class FTBULoadedChunkManager implements ForgeChunkManager.LoadingCallback
{
	public static final FTBULoadedChunkManager INSTANCE = new FTBULoadedChunkManager();

	public final Map<TicketKey, ForgeChunkManager.Ticket> ticketMap = new HashMap<>();
	private final Map<ChunkDimPos, ForgeChunkManager.Ticket> chunkTickets = new HashMap<>();

	public void clear()
	{
		ticketMap.clear();
		chunkTickets.clear();
	}

	@Override
	public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world)
	{
		final int dim = world.provider.getDimension();
		/* Uncomment?
		Iterator<TicketKey> ticketMapItr = ticketMap.keySet().iterator();

		while (ticketMapItr.hasNext())
		{
			if (ticketMapItr.next().dimension == dim)
			{
				ticketMapItr.remove();
			}
		}

		Iterator<ChunkDimPos> chunkTicketsItr = chunkTickets.keySet().iterator();

		while (chunkTicketsItr.hasNext())
		{
			if (chunkTicketsItr.next().dim == dim)
			{
				chunkTicketsItr.remove();
			}
		}*/

		for (ForgeChunkManager.Ticket ticket : tickets)
		{
			TicketKey key = new TicketKey(dim, ticket.getModData().getString("Team"));

			if (!key.teamId.isEmpty())
			{
				ticketMap.put(key, ticket);

				for (ChunkPos pos : ticket.getChunkList())
				{
					chunkTickets.put(new ChunkDimPos(pos, key.dimension), ticket);
					ForgeChunkManager.forceChunk(ticket, pos);
				}
			}
		}
	}

	@Nullable
	public ForgeChunkManager.Ticket requestTicket(MinecraftServer server, TicketKey key)
	{
		ForgeChunkManager.Ticket ticket = ticketMap.get(key);

		if (ticket == null && DimensionManager.isDimensionRegistered(key.dimension))
		{
			WorldServer worldServer = server.getWorld(key.dimension);
			ticket = ForgeChunkManager.requestTicket(FTBUtilities.INST, worldServer, ForgeChunkManager.Type.NORMAL);

			if (ticket != null)
			{
				ticketMap.put(key, ticket);
				ticket.getModData().setString("Team", key.teamId);
			}
		}

		return ticket;
	}

	public void forceChunk(MinecraftServer server, ClaimedChunk chunk)
	{
		if (chunk.forced != null && chunk.forced)
		{
			return;
		}

		ChunkDimPos pos = chunk.getPos();
		ForgeChunkManager.Ticket ticket = requestTicket(server, new TicketKey(pos.dim, chunk.getTeam().getName()));

		if (ticket == null)
		{
			return;
		}

		ChunkPos chunkPos = pos.getChunkPos();
		ForgeChunkManager.forceChunk(ticket, chunkPos);
		chunk.forced = true;
		chunkTickets.put(pos, ticket);

		if (FTBUtilitiesConfig.debugging.log_chunkloading)
		{
			FTBUtilities.LOGGER.info(chunk.getTeam().getTitle().getUnformattedText() + " forced " + pos.posX + "," + pos.posZ + " in " + ServerUtils.getDimensionName(null, pos.dim).getUnformattedText());
		}
	}

	public void unforceChunk(ClaimedChunk chunk)
	{
		if (chunk.forced != null && !chunk.forced)
		{
			return;
		}

		ChunkDimPos pos = chunk.getPos();
		ForgeChunkManager.Ticket ticket = chunkTickets.get(pos);

		if (ticket == null)
		{
			return;
		}

		ForgeChunkManager.unforceChunk(ticket, pos.getChunkPos());
		chunkTickets.remove(pos);
		chunk.forced = false;

		if (ticket.getChunkList().isEmpty())
		{
			ticketMap.remove(new TicketKey(pos.dim, chunk.getTeam().getName()));
			ForgeChunkManager.releaseTicket(ticket);
		}

		if (FTBUtilitiesConfig.debugging.log_chunkloading)
		{
			FTBUtilities.LOGGER.info(chunk.getTeam().getTitle().getUnformattedText() + " unforced " + pos.posX + "," + pos.posZ + " in " + ServerUtils.getDimensionName(null, pos.dim).getUnformattedText());
		}
	}

	public boolean canForceChunks(ForgeTeam team)
	{
		Collection<ForgePlayer> members = team.getMembers();

		for (ForgePlayer player : members)
		{
			if (player.isOnline())
			{
				return true;
			}
		}

		IContext context = new WorldContext(team.universe.world);

		for (ForgePlayer player : members)
		{
			if (PermissionAPI.hasPermission(player.getProfile(), FTBUtilitiesPermissions.CHUNKLOADER_LOAD_OFFLINE, context))
			{
				return true;
			}
		}

		return false;
	}
}