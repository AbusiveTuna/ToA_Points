package com.tuna.toa;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

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

	private ToARegion currentRegion = null;

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
	public void onGameTick(GameTick e)
	{
		LocalPoint lp = client.getLocalPlayer().getLocalLocation();
		int newRegionID = lp == null ? -1 : WorldPoint.fromLocalInstance(client, lp).getRegionID();
		ToARegion newRegion = ToARegion.fromRegionID(newRegionID);
		//we are within ToA
		if(newRegion != null && newRegion != ToARegion.TOA_LOBBY){
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

				switch(newRegion)
				{
					case BOSS_BABA:
						totalPoints = totalPoints + 450;
						break;
					case BOSS_KEPHRI:
						totalPoints = totalPoints + 300;
						break;
				}

			}
			//if we didnt just leave the nexus, or loot room add mvp points
			if(currentRegion != null && currentRegion != ToARegion.TOA_LOBBY && currentRegion != ToARegion.MAIN_AREA && currentRegion != ToARegion.CHEST_ROOM
			   && config.mvpAssumption())
			{
				totalPoints = totalPoints + 300;
			}

			currentRegion = newRegion;

			if(currentRegion != null)
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

		ToANpc currentTarget = ToANpc.fromNpcID(target.getId());

		if(currentTarget == null){
			modifier = 1;
		}
		else {

			switch (currentTarget) {
				case BABOON_BRAWLER:
				case BABOON_THROWER:
				case BABOON_MAGE:
				case BABOON_SHAMAN:
				case BABOON_THRALL:
				case BABOON_CURSED:
				case BABOON_VOLATILE: {
					modifier = 1.2;
					break;
				}

				case BABA:

				case WARDEN_TUMEKEN_RANGE:
				case WARDEN_TUMEKEN_MAGE:
				case WARDEN_ELIDINIS_MAGE:
				case WARDEN_ELIDINIS_RANGE: {
					modifier = 2.0;
					break;
				}

				case BOULDER: {
					modifier = 0.0;
					break;
				}

				case HET_OBELISK:
				case WARDEN_TUMEKEN_FINAL:
				case WARDEN_ELIDINIS_FINAL: {
					modifier = 2.5;
					break;
				}

				case ZEBAK:
				case ZEBAK_ENRAGED:
				case WARDEN_OBELISK: {
					modifier = 1.5;
					break;
				}

				case SCARAB_ARCANE:
				case SCARAB_SPITTING:
				case SCARAB_SOLDIER: {
					modifier = 0.5;
					break;
				}

				case WARDEN_ELIDINIS_INACTIVE_P1:
				case WARDEN_ELIDINIS_INACTIVE_P2:
				case WARDEN_ELIDINIS_INACTIVE_P3:
				case WARDEN_TUMEKEN_INACTIVE_P1:
				case WARDEN_TUMEKEN_INACTIVE_P2:
				case WARDEN_TUMEKEN_INACTIVE_P3:
				case WARDEN_CORE: {
					modifier = 0;
					break;
				}

				default: {
					modifier = 1.0;
					break;
				}


			}
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
		    && client.getLocalPlayer() != null && !inRaid)
		{
			reset();
		}
	}

	public void reset()
	{
		roomPoints = 0;
		currentRegion = null;
		totalPoints = 5000;
		inRaid = false;
	}


}
