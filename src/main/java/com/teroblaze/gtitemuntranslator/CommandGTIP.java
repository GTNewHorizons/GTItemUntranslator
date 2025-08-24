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
        if (args.length != 1) {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
            return;
        }
        if ("on".equalsIgnoreCase(args[0])) {
            GTItemUntranslator.tooltipsEnabled = true;
            GTItemUntranslator.config.get("general", "tooltipsEnabled", true)
                    .set(GTItemUntranslator.tooltipsEnabled);
            GTItemUntranslator.config.save();
            sender.addChatMessage(new ChatComponentText("GT English tooltips activated."));
        } else if ("off".equalsIgnoreCase(args[0])) {
            GTItemUntranslator.tooltipsEnabled = false;
            GTItemUntranslator.config.get("general", "tooltipsEnabled", true)
                    .set(GTItemUntranslator.tooltipsEnabled);
            GTItemUntranslator.config.save();
            sender.addChatMessage(new ChatComponentText("GT English tooltips deactivated."));
        } else {
            sender.addChatMessage(new ChatComponentText("Usage: " + getCommandUsage(sender)));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
