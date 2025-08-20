package com.teroblaze.gtitemuntranslator;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandGTIP extends CommandBase {

    @Override
    public String getCommandName() {
        return "gtip";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/gtip <on|off>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            if ("on".equalsIgnoreCase(args[0])) {
                TooltipEventHandler.TOOLTIPS_ENABLED = true;
                sender.addChatMessage(new ChatComponentText("GT English tooltips activated."));
                return;
            }
            if ("off".equalsIgnoreCase(args[0])) {
                TooltipEventHandler.TOOLTIPS_ENABLED = false;
                sender.addChatMessage(new ChatComponentText("GT English tooltips deactivated."));
                return;
            }
        }
        sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // available to all players
    }
}
