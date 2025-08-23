package com.teroblaze.gtitemuntranslator.waila;

import com.teroblaze.gtitemuntranslator.TooltipEventHandler;
import com.teroblaze.gtitemuntranslator.GTItemUntranslator;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.block.Block;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class DataProvider implements IWailaDataProvider {

    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return null;
    }

    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!GTItemUntranslator.tooltipsEnabled) return currenttip;

        try {
            ItemStack stack = new ItemStack(accessor.getBlock(), 1, accessor.getMetadata());
            String unloc = stack.getUnlocalizedName();
            String name = TooltipEventHandler.getOriginalEnglishNameStatic(stack, unloc);
            if (name != null && !currenttip.get(0).contains(name)) {
                currenttip.add(1, EnumChatFormatting.GRAY + "[EN] " + name);
            }
        } catch (Throwable t) {
            System.err.println("[GT Item Untranslator] Waila error: " + t.getMessage());
        }
        return currenttip;
    }

    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip,
                                     IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }
}
