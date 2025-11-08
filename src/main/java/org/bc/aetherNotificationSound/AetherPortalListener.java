package org.bc.aetherNotificationSound;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.StringUtils;


public class AetherPortalListener implements Listener {

    Plugin plugin;

    AetherPortalListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // block faces on portal:
    // up == clicked on top of block
    // down == clicked on bottom of block
    // west == player facing east, clicked on front of block

    private boolean isPortalShape(World world, int x, int y, int z, BlockFace face) {

        // assume x axis by default
        char axis = 'x';

        int bottomLeftPos[] = new int[]{0,0,0};

        // TODO: Support Clicking Any Block In The Portal
        // clicked on bottom of portal
        if (face == BlockFace.UP) {
            // we need 4 blocks in a row, the head or tail can be null so long as there are 4 in a row
            Block bottomBlocks[] = new Block[]{null, null, null, null, null};




            int glowstoneCount = 0;
            // always try x-axis first
            {
                // get block 2 to the left
                bottomBlocks[0] = world.getBlockAt(x-2, y, z);
                // get block to the left
                bottomBlocks[1] = world.getBlockAt(x-1, y, z);
                // get block below
                bottomBlocks[2] = world.getBlockAt(x, y, z);
                // get block to the right
                bottomBlocks[3] = world.getBlockAt(x+1, y, z);
                // get block 2 to the right
                bottomBlocks[4] = world.getBlockAt(x+2, y, z);
                for (Block b : bottomBlocks) {
                    if (b.getType() == Material.GLOWSTONE) glowstoneCount++;
                }
                axis = 'x';
            }
            // then try z-axis
            if (glowstoneCount < 4) {
                // get block 2 to the left
                bottomBlocks[0] = world.getBlockAt(x, y, z-2);
                // get block to the left
                bottomBlocks[1] = world.getBlockAt(x, y, z-1);
                // get block below
                bottomBlocks[2] = world.getBlockAt(x, y, z);
                // get block to the right
                bottomBlocks[3] = world.getBlockAt(x, y, z+1);
                // get block 2 to the right
                bottomBlocks[4] = world.getBlockAt(x, y, z+2);
                axis = 'z';
            }

            StringBuilder sb = new StringBuilder();
            for (Block b : bottomBlocks) {
                sb.append(b.getType() == Material.GLOWSTONE ? "G" : "_");
            }

            // check to make sure all glowstone is consecutive
            // i.e. 0..3 or 1..4 or 0..4 are all glowstone
            // 4 glowstone must also be present
            boolean consecutiveGlowstone = true;
            switch (sb.toString().indexOf('_')) {
                case -1:
                case 0:
                case 4:
                    consecutiveGlowstone = true;
                    break;
                default:
                    consecutiveGlowstone = false;
                    break;
            }
            consecutiveGlowstone = consecutiveGlowstone && StringUtils.countMatches(sb.toString(), "G") >= 4;
            if (!consecutiveGlowstone) {
                return false;
            }

            // if last element in bottom blocks is empty, the player placed on the right side
            // if first element in bottom blocks is empty, the player placed on the left side
            char waterPlaced = (bottomBlocks[0].getType() != Material.GLOWSTONE ? 'l' : 'r');

            // make sure to set the bottom left point of the portal
            if (axis == 'x') {
                bottomLeftPos = new int[]{((waterPlaced == 'l') ? x - 1 : x - 2), y, z};
            }  else if (axis == 'z') {
                bottomLeftPos = new int[]{x, y, ((waterPlaced == 'l') ? z - 1 : z - 2)};
            }

        }

        // once we know the bottom of the portal is complete, we can check the rest of the shape
        // check left pillar
        for (int i = 1; i <= 4; i++) {
            var b = world.getBlockAt(bottomLeftPos[0], bottomLeftPos[1]+i, bottomLeftPos[2]);
            if (b.getType() != Material.GLOWSTONE) {
                return false;
            }
        }
        // check right pillar
        for (int i = 1; i <= 4; i++) {
            var b = world.getBlockAt(bottomLeftPos[0]+ (axis == 'z' ? 0 : 3), bottomLeftPos[1]+i, bottomLeftPos[2] + (axis == 'z' ? 3 : 0));
            if (b.getType() != Material.GLOWSTONE) {
                return false;
            }
        }

        // check top
        for (int i = 0; i < 3; i++) {
            var b = world.getBlockAt(bottomLeftPos[0] + (axis == 'z' ? 0 : i),bottomLeftPos[1] + 4,bottomLeftPos[2] + (axis == 'z' ? i : 0));
            if (b.getType() != Material.GLOWSTONE) {
                return false;
            }
        }

        return true;
    }


    @EventHandler
    public void onPlayerAttemptPortal(PlayerBucketEmptyEvent event) {
        var player = event.getPlayer();
        var blockLoc = event.getBlockClicked().getLocation();
        var blockFace = event.getBlockFace();

        boolean isPortalShaped = isPortalShape(player.getWorld(), blockLoc.getBlockX(), blockLoc.getBlockY(), blockLoc.getBlockZ(), blockFace);
        if (isPortalShaped) {
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.BLOCKS, 3, 1);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.BLOCKS, 3, 1);
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.BLOCKS, 3, 1);
        }
    }

}
