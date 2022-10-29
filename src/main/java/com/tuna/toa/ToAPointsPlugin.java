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
	private OverlayManager overlayManager;

	@Inject
	private ClientThread clientThread;

	public static double totalPoints = 5000;

	public static double roomPoints = 0;

	public static int invocationLevel = 0;

	boolean inRaid = false;
	private String[] npcModList = {"Scarab Swarm","Agile Scarab","Scarab","Core","Het's Seal","Het's Seal(weakened)","Elidinis' Warden","Tumeken's Warden","Obelisk","Zebak","Ba-Ba","Baboon Brawler",
			"Baboon Thrower","Baboon Mage","Baboon Shaman","Volatile Baboon","Cursed Baboon","Baboon Thrall",
			"Arcane Scarab","Spitting Scarab","Soldier Scarab"};

	int[] raidRegions = {14160,15186,15188,15698,15700,14162,14164,14674,14676,15184,15696};
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
		// Ignore all hitsplats other than mine
		if (!hitsplat.isMine() || target == client.getLocalPlayer())
		{
			return;
		}

		NPC npc = (NPC) target;

		pointCalc(hitsplat, npc);

	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		LocalPoint lp = client.getLocalPlayer().getLocalLocation();
		int newRegion = lp == null ? -1 : WorldPoint.fromLocalInstance(client, lp).getRegionID();
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
		if(newRegion != currentRegion && inRaid)
		{
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

		String[] monkeyNames = {"Baboon Brawler","Baboon Thrower","Baboon Mage","Baboon Shaman","Volatile Baboon","Cursed Baboon","Baboon Thrall"};
		String[] scarabNames = {"Scarab Swarm","Agile Scarab","Arcane Scarab","Spitting Scarab","Soldier Scarab","Scarab"};
		if (ArrayUtils.contains(npcModList,npcName))
		{

			//path of monk
			if(ArrayUtils.contains(monkeyNames,npcName))
			{
				modifier = 1.2;
			}
			if(npcName.equals("Ba-Ba"))
			{
				modifier = 2.0;
			}

			//path of het
			if(npcName.equals("Het's Seal") || npcName.equals("Het's Seal(weakened)"))
			{
				modifier = 2.5;
			}

			//path of croc
			if(npcName.equals("Zebak"))
			{
				modifier = 1.5;
			}

			//path of dung
			if(ArrayUtils.contains(scarabNames,npcName))
			{
				modifier = 0.5;
			}

			//Warden: p1
			if(npcName.equals("Obelisk"))
			{
				modifier = 1.5;
			}

			if(npcName.equals("Core"))
			{
				modifier = 0;
			}
			//Warden: p2
			if(npcName.equals("Tumeken's Warden") || npcName.equals("Elidinis' Warden"))
			{
				if(currentRegion == 15184)
				{
					modifier = 2.0;
				}
				if(currentRegion == 15696)
				{
					modifier = 2.5;
				}

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