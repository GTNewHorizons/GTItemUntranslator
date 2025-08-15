package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorTESRFix;
import com.mrteroblaze.travelanchorfix.handler.ClientTickHandlerFix;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import crazypants.enderio.machine.travel.TileTravelAnchor;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());
        TickRegistry.registerTickHandler(new ClientTickHandlerFix(), Side.CLIENT);
    }
}
