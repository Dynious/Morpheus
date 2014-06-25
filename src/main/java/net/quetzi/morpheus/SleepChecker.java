package net.quetzi.morpheus;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class SleepChecker {

    public void updatePlayerStates(World world) {

        // Iterate players and update their status
        for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
            String username = player.getGameProfile().getName();
            if (player.isPlayerFullyAsleep() && !Morpheus.playerSleepStatus.get(player.dimension).isPlayerSleeping(username)) {
                Morpheus.playerSleepStatus.get(player.dimension).setPlayerAsleep(username);
                // Alert players that this player has gone to bed
                alertPlayers(createAlert(player.dimension, username, Morpheus.onSleepText), world);
            } else if (!player.isPlayerFullyAsleep() && Morpheus.playerSleepStatus.get(player.dimension).isPlayerSleeping(username)) {
                Morpheus.playerSleepStatus.get(player.dimension).setPlayerAwake(username);
                // Alert players that this player has woken up
                if (!world.isDaytime()) {
                    alertPlayers(createAlert(player.dimension, username, Morpheus.onWakeText), world);
                }
            }
        }
        if (areEnoughPlayersAsleep(world)) {
            advanceToMorning(world);
        }
    }

    private void alertPlayers(ChatComponentText alert, World world) {

        if ((alert != null) && (Morpheus.isAlertEnabled())) {
            for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
                player.addChatMessage(alert);
            }
        }
    }

    private ChatComponentText createAlert(int dimension, String username, String text) {

        String alertText = EnumChatFormatting.GOLD + "Player " + EnumChatFormatting.WHITE + username + EnumChatFormatting.GOLD + " " + text + " "
                + Morpheus.playerSleepStatus.get(dimension).toString();
        Morpheus.mLog.info("Player " + username + " " + text + " " + Morpheus.playerSleepStatus.get(dimension).toString());
        return new ChatComponentText(alertText);
    }

    private void advanceToMorning(World world) {

        if (world.provider.dimensionId == 0) {
            world.setWorldTime(world.getWorldTime() + getTimeToSunrise(world));
        }
        // Send Good morning message
        alertPlayers(new ChatComponentText(EnumChatFormatting.GOLD + Morpheus.onMorningText), world);
        world.provider.resetRainAndThunder();
    }

    private long getTimeToSunrise(World world) {

        long dayLength = 24000;
        return dayLength - (world.getWorldTime() % dayLength);
    }

    private boolean areEnoughPlayersAsleep(World world) {

        if ((world.provider.dimensionId == 0)) { // || (MorpheusRegistry.registry.get(world.provider.dimensionId) != null)) {
            return Morpheus.playerSleepStatus.get(world.provider.dimensionId).getPercentSleeping() >= Morpheus.perc;
        }
        return false;
    }
}
