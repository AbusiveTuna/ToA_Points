package com.tuna.toa;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import javax.inject.Inject;
import javax.sound.sampled.Line;

import com.tuna.toa.UniqueConfigOptions.UniqueConfigOptions;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;


import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class ToAPointsOverlay extends OverlayPanel
{
    private static final DecimalFormat POINTS_FORMAT = new DecimalFormat("#,###");

    private Client client;
    private ToAPointsPlugin plugin;
    private ToAPointsConfig config;

    private double uniqueChance;

    @Inject
    private ToAPointsOverlay(Client client,
                             ToAPointsPlugin plugin,
                             ToAPointsConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_RIGHT);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {

        double totalPoints = ToAPointsPlugin.getTotalPoints();
        double roomPoints = ToAPointsPlugin.getRoomPoints();
        if (totalPoints >= 5000){
            totalPoints = totalPoints - 5000;
        }

        panelComponent.getChildren().add(TitleComponent.builder()
                        .text("ToA Point Tracker").build());

        panelComponent.getChildren().add(LineComponent.builder().build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Total:")
                .right(POINTS_FORMAT.format(totalPoints))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Room Points:")
                .right(POINTS_FORMAT.format(roomPoints))
                .build());

        if (config.raidsUniqueChance())
        {
            if (totalPoints < 1500)
            {
                uniqueChance = 0;
            }
            int invocationLevel = ToAPointsPlugin.getInvocationLevel();

            if (invocationLevel > 550)
            {
                invocationLevel = 550;
            }
            int invocationMod = 0;

            if(invocationLevel < 150)
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Unique:")
                        .right("Very low")
                        .build());
            }
            else {
                if (invocationLevel > 400)
                {
                    //take the remainder after 400
                    int invocOver = invocationLevel - 400;
                    //levels over 400 count only count as 1/3 more
                    invocOver = invocOver / 3;
                    int expertInvocationLevel = 400 + invocOver;
                    invocationMod = expertInvocationLevel * 20;
                }
                else
                {
                    invocationMod = invocationLevel * 20;
                }

                    int perOnePercentChance = 10500 - invocationMod;
                    totalPoints = totalPoints + roomPoints;
                    uniqueChance = totalPoints/perOnePercentChance;

                    panelComponent.getChildren().add(LineComponent.builder()
                        .left("Unique:")
                        .right(String.valueOf( Math.round(uniqueChance * 100.0) / 100.0 ) + "%")
                        .build());
            }
        }

        return super.render(graphics);
    }
}
