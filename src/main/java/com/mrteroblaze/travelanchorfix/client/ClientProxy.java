package com.mrteroblaze.travelanchorfix.client;

import com.mrteroblaze.travelanchorfix.client.handler.AnchorNameOverlayHandler;
import com.mrteroblaze.travelanchorfix.CommonProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // Регистрируем наш хендлер
        AnchorNameOverlayHandler overlayHandler = new AnchorNameOverlayHandler();
        MinecraftForge.EVENT_BUS.register(overlayHandler);
    }
}
