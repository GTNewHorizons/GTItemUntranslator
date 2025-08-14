package com.mrteroblaze.travelanchorfix;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import com.mrteroblaze.travelanchorfix.server.ServerProxy;

@Mod(
    modid = TravelAnchorLabelFixMod.MODID,
    name = "Travel Anchor Label Fix",
    version = TravelAnchorLabelFixMod.VERSION,
    dependencies = "required-after:EnderIO"
)
public class TravelAnchorLabelFixMod {
    public static final String MODID = "travelanchorfix";
    public static final String VERSION = "1.0.0";

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
