package com.mrteroblaze.travelanchorfix;

import com.mrteroblaze.travelanchorfix.client.render.TravelEntitySpecialRenderer;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import crazypants.enderio.teleport.anchor.TileTravelAnchor;

@Mod(
    modid = "travelanchorfix",
    name = "Travel Anchor Fix",
    version = "1.0.0",
    dependencies = "required-after:EnderIO;required-after:EnderCore;required-after:angelica")
public class TravelAnchorFix {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTravelAnchor.class, new TravelEntitySpecialRenderer());
    }
}
