package com.mrteroblaze.travelanchorfix.client.render;

import crazypants.enderio.teleport.anchor.TileTravelAnchor;
import crazypants.enderio.teleport.anchor.TravelEntitySpecialRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class MyTravelEntitySpecialRenderer extends TravelEntitySpecialRenderer<TileTravelAnchor> {

    @Override
    protected void renderName(TileTravelAnchor te, double x, double y, double z, float partialTick) {
        // Принудительно включаем unicode-рендеринг, чтобы избежать квадратиков с Angelica
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        boolean prevUnicode = fr.getUnicodeFlag();
        fr.setUnicodeFlag(true);

        // Вызываем стандартный рендер
        super.renderName(te, x, y, z, partialTick);

        // Возвращаем исходный флаг
        fr.setUnicodeFlag(prevUnicode);
    }
}
