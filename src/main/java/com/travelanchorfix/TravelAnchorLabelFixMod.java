package com.mrteroblaze.travelanchorfix;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TravelAnchorLabelFixMod.MODID, version = TravelAnchorLabelFixMod.VERSION, dependencies = "required-after:EnderIO")
public class TravelAnchorLabelFixMod {
    public static final String MODID = "travelanchorfix";
    public static final String VERSION = "1.0";

    @SidedProxy(
        clientSide = "com.mrteroblaze.travelanchorfix.client.ClientProxy",
        serverSide = "com.mrteroblaze.travelanchorfix.server.ServerProxy"
    )
    public static ServerProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.registerRenderers();
    }
}
