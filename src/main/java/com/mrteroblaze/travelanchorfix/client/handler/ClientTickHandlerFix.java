package com.mrteroblaze.travelanchorfix.client.handler;

import crazypants.enderio.teleport.TravelController;
import crazypants.enderio.teleport.TileTravelAnchor;
import crazypants.enderio.item.ItemTravelStaff;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientTickHandlerFix {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        EntityPlayer player = mc.thePlayer;

        boolean holdingStaff = isHoldingStaff(player);
        boolean standingOnAnchor = isStandingOnAnchor(player);

        if (holdingStaff || standingOnAnchor) {
            // Показываем якоря в радиусе
            TravelController.instance.updateTargets(player);
        }
    }

    private boolean isHoldingStaff(EntityPlayer player) {
        ItemStack held = player.getCurrentEquippedItem();
        return held != null && held.getItem() instanceof ItemTravelStaff;
    }

    private boolean isStandingOnAnchor(EntityPlayer player) {
        TileTravelAnchor te = TravelController.instance.getTileEntityTravelAnchor(player);
        return te != null;
    }
}
