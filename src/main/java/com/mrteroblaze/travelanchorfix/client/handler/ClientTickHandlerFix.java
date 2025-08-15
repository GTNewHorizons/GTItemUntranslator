package com.mrteroblaze.travelanchorfix.client.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import crazypants.enderio.teleport.TravelController;

public class ClientTickHandlerFix {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // Pass event to Ender IO controller (GTNH signature expects the event)
        TravelController.instance.onClientTick(event);
    }
}
