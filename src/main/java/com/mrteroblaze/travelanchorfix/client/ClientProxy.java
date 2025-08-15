package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.client.handler.ClientTickHandlerFix;
import com.mrteroblaze.travelanchorfix.client.render.TravelAnchorTESRFix;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import crazypants.enderio.teleport.TileTravelAnchor;

public class ClientProxy extends com.mrteroblaze.travelanchorfix.CommonProxy {

    @Override
    public void init() {
        super.init();
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelAnchorTESRFix());
        FMLCommonHandler.instance().bus().register(new ClientTickHandlerFix());
    }
}
