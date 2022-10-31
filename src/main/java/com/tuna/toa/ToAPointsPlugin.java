package com.tuna.toa;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

@Slf4j
@PluginDescriptor(
		name = "ToA Points Overlay"
)
public class ToAPointsPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private ToAPointsOverlay overlay;

	@Inject
	private ToAPointsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClientThread clientThread;

	public static double totalPoints = 5000;

	public static double roomPoints = 0;

	public static int invocationLevel = 0;

	public static int partySize = 0;

	boolean inRaid = false;
	int[] npcIds = {11707,11730,11778,11758,11770,11751,11756,11757,11761,11732,11783,11748,11749,11760,11759,
			11755,11753,11754,11762,
			11709,11711,11710,11715,11718,11717,11716,
			11727,11726,11725,11724,11697};

	int[] raidRegions = {14160,15186,15188,15698,15700,14162,14164,14674,14676,15184,15696,14672};
	private int currentRegion = 0;

	public static int getInvocationLevel()
	{
		return invocationLevel;
	}

	@Provides
	ToAPointsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ToAPointsConfig.class);
	}

	public static double getTotalPoints()
	{
		return totalPoints;
	}

	public static double getRoomPoints()
	{
		return roomPoints;
	}
	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);

	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);

		reset();
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		Actor target = hitsplatApplied.getActor();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();

		//p2 warden and palm are weird. So take the damage done to warden/palm divide it by group members then multiply by the modifier.
		//Isn't super accurate, but it'll be as close as it can get currently.
		if(!hitsplat.isMine() && !hitsplat.isOthers() &&
				(hitsplat.getHitsplatType() == 53 || hitsplat.getHitsplatType() == 55 || hitsplat.getHitsplatType() == 11))
		{
			NPC npc = (NPC) target;
			List<Player> teamMembers = client.getPlayers();

			int averageHitSplat = hitsplat.getAmount() / teamMembers.size();

			//p2 warden and palm are 2.0 modifier
			averageHitSplat = averageHitSplat * 2;
			roomPoints = roomPoints + averageHitSplat;
		}
		else if (!hitsplat.isMine() || target == client.getLocalPlayer())
		{
			//do nothing
		}
		else
		{
			NPC npc = (NPC) target;
			pointCalc(hitsplat, npc);
		}

	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		LocalPoint lp = client.getLocalPlayer().getLocalLocation();
		int newRegion = lp == null ? -1 : WorldPoint.fromLocalInstance(client, lp).getRegionID();

		//we are within ToA
		if(ArrayUtils.contains(raidRegions,newRegion)){
			inRaid = true;
			overlayManager.add(overlay);

			Widget invoWidget = client.getWidget(WidgetID.TOA_RAID_GROUP_ID, 42);

			if(invoWidget != null) {
				String invoLevel = invoWidget.getText();
				invocationLevel = Integer.parseInt(invoLevel.replaceAll("[^0-9]", ""));
			}

		}
		else{
			inRaid = false;
			overlayManager.remove(overlay);
		}
		//still in the raid, but we moved to a new area
		if(newRegion != currentRegion && inRaid)
		{

			if(config.puzzlePointsAssumption()){
				//assuming 100 points per puzzle, average 3 puzzles completed
				if(newRegion == 14164){
					totalPoints = totalPoints + 300;
				}
				//assuming 75 points per trap, average 6 traps completed
				if(newRegion == 15188){
					totalPoints = totalPoints + 450;
				}
			}
			//if we didnt just leave the nexus, or loot room add mvp points
			if(currentRegion != 13454 && currentRegion != 14160 && currentRegion != 14672 && config.mvpAssumption()){
				totalPoints = totalPoints + 300;
			}
			currentRegion = newRegion;

			if(ArrayUtils.contains(raidRegions, currentRegion))
			{
				totalPoints = totalPoints + roomPoints;
				roomPoints = 0;

				//hard cap on total points
				if(totalPoints > 64000){
					totalPoints = 64000;
				}
			}
		}
		currentRegion = newRegion;
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		if (actorDeath.getActor() == client.getLocalPlayer())
		{
			double pointLoss = totalPoints * 20;
			pointLoss = pointLoss/100;

			if(pointLoss < 1000)
			{
				pointLoss = 1000;
			}
			if (totalPoints < 1000)
			{
				totalPoints = 0;
			}
			else
			{
				totalPoints = totalPoints - pointLoss;
			}

		}
	}

	public void pointCalc(Hitsplat hitsplat, NPC target)
	{

		double modHit = 0;
		double modifier = 0;
		int rawHit = hitsplat.getAmount();

		String npcName = target.getName();
		int npcId = target.getId();


		int[] scarabIds = {11727,11726,11725,11724,11697};
		int[] monkeyIds = {11712,11713,11709,11711,11710,11715,11718,11717,11716};

		if (ArrayUtils.contains(npcIds,npcId))
		{

			//path of monk
			if(ArrayUtils.contains(monkeyIds,npcId))
			{
				modifier = 1.2;
			}
			else if(npcId == 11778)
			{
				modifier = 2.0;
			}
			//boulders
			else if(npcId == 11783){
				modifier = 0.0;
			}

			//path of het (11707)
			else if(npcId == 11707)
			{
				modifier = 2.5;
			}

			//path of croc
			else if(npcId == 11730 || npcId == 11732)
			{
				modifier = 1.5;
			}

			//path of dung
			else if(ArrayUtils.contains(scarabIds,npcId))
			{
				modifier = 0.5;
			}

			//Warden: p1 obelisk
			else if(npcId == 11751)
			{
				modifier = 1.5;
			}

			//don't count hitsplats done to downed wardens
			else if(npcId == 11758 || npcId == 11770 || npcId == 11748 || npcId == 11755 || npcId == 11749 || npcId == 11760 || npcId == 11759)
			{
				//warden is down, count nothing.
				modifier = 0;

			}

			else if(npcId == 11756 || npcId == 11757 || npcId == 11753 || npcId == 11754){
				//p2 warden non core
				modifier = 2.0;
			}

			//p3 E warden
			else if(npcId == 11761 || npcId == 11762){
				modifier = 2.5;
			}

		}
		else
		{
			modifier = 1;
		}

		modHit = rawHit * modifier;


		roomPoints = roomPoints + modHit;

		//hard cap on room points
		if(roomPoints > 20000)
		{
			roomPoints = 20000;
		}

	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{

		if (event.getGameState() == GameState.LOGGED_IN
				&& client.getLocalPlayer() != null
				&& !inRaid)
		{
			reset();
		}
	}

	public void reset()
	{
		roomPoints = 0;
		totalPoints = 5000;
		inRaid = false;
	}


}
