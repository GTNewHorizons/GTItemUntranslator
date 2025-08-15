package com.mrteroblaze.travelanchorfix;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(
    modid = TravelAnchorFix.MODID,
    name = TravelAnchorFix.MODNAME,
    version = TravelAnchorFix.VERSION,
    acceptedMinecraftVersions = "[1.7.10]"
)
public class TravelAnchorFix {

    public static final String MODID = "travelanchorfix";
    public static final String MODNAME = "Travel Anchor Fix";
	public static final String VERSION = "1.0.0";

    @SidedProxy(
        clientSide = "com.mrteroblaze.travelanchorfix.client.ClientProxy",
        serverSide = "com.mrteroblaze.travelanchorfix.client.ClientProxy" // серверной логики нет
    )
    public static com.mrteroblaze.travelanchorfix.client.ClientProxy proxy;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.initClient();
    }
}
