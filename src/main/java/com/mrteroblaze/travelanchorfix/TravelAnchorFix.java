package com.mrteroblaze.travelanchorfix;

import com.mrteroblaze.travelanchorfix.client.ClientProxy;
import com.mrteroblaze.travelanchorfix.server.ServerProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = TravelAnchorFix.MODID, name = TravelAnchorFix.MODNAME, version = TravelAnchorFix.VERSION, dependencies = "required-after:EnderIO")
public class TravelAnchorFix {

    public static final String MODID = "travelanchorfix";
    public static final String MODNAME = "Travel Anchor Fix";
    public static final String VERSION = "1.0.0";

    @SidedProxy(clientSide = "com.mrteroblaze.travelanchorfix.client.ClientProxy",
                serverSide = "com.mrteroblaze.travelanchorfix.server.ServerProxy")
    public static ServerProxy proxy;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }
}
