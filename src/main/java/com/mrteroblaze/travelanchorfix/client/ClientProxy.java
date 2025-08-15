package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.CommonProxy;
import com.mrteroblaze.travelanchorfix.client.handler.AnchorNameOverlayHandler;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // Регистрируем только наш оверлей-хендлер. НИКАКИХ TESR для TileTravelAnchor!
        MinecraftForge.EVENT_BUS.register(new AnchorNameOverlayHandler());
    }
}
