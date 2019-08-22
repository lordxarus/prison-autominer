package com.lordxarus.autominer.npc

import com.lordxarus.autominer.util.*
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import me.mrCookieSlime.QuickSell.Shop
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.trait.trait.Equipment
import net.md_5.bungee.api.ChatColor
import net.minecraft.server.v1_8_R3.BlockPosition
import net.minecraft.server.v1_8_R3.MinecraftServer
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_8_R3.CraftServer
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.util.*
import kotlin.random.Random


/* This class contains all of the data and logic that the Autominer Trait uses to operate the miner
 *
 */
class AutominerModel(val npc: NPC) : Listener {

    val rand = Random

    val tool = ItemStack(Material.DIAMOND_PICKAXE).also {
        it.amount = 1
        // for show
        it.addEnchantment(Enchantment.DIG_SPEED, 5)
    }
    var state = State(MinerState.WAITING, ThoughtState.NO_TARGET, ScanState.NARROW_SCAN)

    var previousState = state
    var successRate = .8

    var armReach = 2
    var wideScanRadius = 100

    val wideScanRange = -(wideScanRadius)..(wideScanRadius)
    val narrowScanRange = -(armReach)..(armReach)

    var target: Block? = null

    val mined = arrayListOf<ItemStack>()
    var timeLeft = 0


    var spawnTime = 0L

    // only needed because whenever we try to despawn the NPC it still loads and gives an NPE for a tick
    var run = true

    lateinit var player: Player

    lateinit var shop: Shop

    lateinit var region: ProtectedRegion
    lateinit var breaker: BlockBreaker


    // STATUS WAITING
    // Triggered when a miner is not doing anything at all
    // Scan area for candidate blocks
    // Find nearest one

    /**
     *
     * Design of AI
     *
     * Waiting is the starting state and quasi-default state
     *
     *
     * wide-scan is the way that the finds blocks after it has spawned or exhausted the blocks in it's arm reach. It is used to find a new location to mine at
     * narrow-scan is used after the bot has found suitable blocks to mine and will only ever search for blocks in the miner's arm's reach.
     *
     * A. The waiting state will read in the scan type of the bot as it only should be in the waiting state after spawning or after breaking a block / trying to find a target
     *
     * If the miner has no target and is WAITING then a scan should be initiated based on the scan state
     *
     *
     * The miner will first use widescan to find a target block. If it is not successful the bot will be set to stuck and will wait for x seconds until resetting.
     * If the bot does find a block in the widescan, it will evaluate how far away the target is. If it is farther than the predefined arm length it will set it's
     * state to State(MinerState.WALKING, ThoughtState.FAR_TARGET, ThoughtState.NARROW_SCAN). Else it will set itself MINING, CLOSE_TARGET, NARROW_SCAN
     *
     * It will then walk to the target to within it's arms length. Then set to MINING, CLOSE_TARGET, NARROW_SCAN.
     *
     * B. While in the mining state the bot will try to mine the block. The breakBlock() function will return whether the block was broken or not.
     *
     * If the bot is unsuccessful in mining the block the bot will remain in the same state (based on the random factor, not being stuck).
     *
     * C. However if the block is broken the bot will be set to WAITING, NO_TARGET, NARROW_SCAN
     *
     * Upon being in the waiting state and having a NARROW_SCAN state the bot will execute it's BreakPattern to find the next block to break and go back to B.
     * If the NARROW_SCAN is unable to find a suitable block then the state will be set to
     *
     */

    init {
        npc.getTrait(Equipment::class.java).set(Equipment.EquipmentSlot.HAND, tool)
        val filtered = plugin.npcs.entries.filter { it.value == npc }
        if (filtered.isNotEmpty()) {
            player = filtered[0].key
        } else {
            println("Something is fucked")
        }
    }

    fun onSpawn() {
        region = getWorldGuardRegions(npc.storedLocation).filter { it.id.contains("-am-") }[0]
        breaker = DefaultBlockBreaker(this)
        spawnTime = System.currentTimeMillis()
    }

    fun run() {
        if (run && npc.isSpawned) {
            if ((timeLeft < (System.currentTimeMillis() - spawnTime))) {
                timeLeft = 0
                player.sendMessage(plugin.messages.outOfTime)
                npc.despawn()
            } else {
                timeLeft -= (System.currentTimeMillis() - spawnTime).toInt()
                spawnTime = System.currentTimeMillis()
            }

            when (state.minerState) {
                // If we're just sitting around we need to figure out what to do
                MinerState.WAITING -> {

                    if (state.thoughtState == ThoughtState.NO_TARGET) {

                        if (state.scanState == ScanState.WIDE_SCAN) {
                            val wideScan = breaker.wideScan()
                            if (wideScan.size > 0) {
                                target = getClosest(wideScan)
                                if (target!!.location.distance(npc.entity.location.block.location) >= armReach) {
                                    npc.navigator.setTarget(target!!.location)
                                    updateState(State(MinerState.WALKING, ThoughtState.FAR_TARGET, ScanState.WIDE_SCAN))
                                } else {
                                    updateState(State(MinerState.MINING, ThoughtState.CLOSE_TARGET, ScanState.NARROW_SCAN))
                                }
                            } else {
                                updateState(state.also { it.minerState = MinerState.STUCK })
                            }
                        } else if (state.scanState == ScanState.NARROW_SCAN) {
                            val candidate = getClosest(breaker.narrowScan())
                            if (candidate == null) {
                                updateState(State(MinerState.WAITING, ThoughtState.NO_TARGET, ScanState.WIDE_SCAN))
                            } else {
                                target = candidate
                                updateState(State(MinerState.MINING, ThoughtState.CLOSE_TARGET, ScanState.NARROW_SCAN))
                            }

                        }

                    }
                }

                MinerState.WALKING -> {
                    if (target!!.location.distance(npc.entity.location) > armReach) {
                        if (!npc.navigator.isNavigating) {
                            if (previousState != state) {
                                npc.navigator.setTarget(target!!.location)
                            } else {
                                updateState(State(MinerState.STUCK, ThoughtState.NO_TARGET, ScanState.WIDE_SCAN))
                            }
                        }
                        clearBlocked()
                    } else {
                        updateState(State(MinerState.MINING, ThoughtState.CLOSE_TARGET, ScanState.NARROW_SCAN))
                    }
                }

                MinerState.MINING -> {
                    if (breakBlock(target!!, successRate)) {
                        updateState(State(MinerState.WAITING, ThoughtState.NO_TARGET, ScanState.NARROW_SCAN))
                    }

                }

                MinerState.STUCK -> {
                    var scan = breaker.wideScan()
                    if (scan.isEmpty()) {
                        scan = breaker.scan(wideScanRange, 0..20)
                        if (scan.isEmpty()) {
                            player.sendMessage("${ChatColor.RED}Miner is stuck or done. Despawning in 5 seconds.")
                            run = false
                            object : BukkitRunnable() {
                                override fun run() {
                                    npc.despawn()
                                }

                            }.runTaskLater(plugin, (5 * MinecraftServer.getServer().recentTps[0]).toLong())
                        } else {
                            target = getClosest(scan)
                            updateState(State(MinerState.MINING, ThoughtState.CLOSE_TARGET, ScanState.NARROW_SCAN))
                        }
                    } else {
                        val blocks = breaker.wideScan().filter { it != target }
                        target = getClosest(blocks)
                        updateState(State(MinerState.WALKING, ThoughtState.FAR_TARGET, ScanState.NARROW_SCAN))
                    }

                }
            }
        }
    }


    fun updateState(newState: State) {
        previousState = state
        state = newState
    }

    /**
     * Scan will scan blocks around the miner in the appropriate region in a defined radius
     *
     * Use it to find a suitable block to walk to
     */

    private fun clearBlocked() {
        val blocks = ArrayList<Block>()
        for (i in -1..1) {
            for (j in -1..1) {
                for (k in -1..1) {
                    val block = npc.entity.location.block.getRelative(i, j, k)
                    if (region.contains(block.x, block.y, block.z) && block.type != Material.AIR) {
                        breakBlock(block, 1.0)
                    }
                }
            }
        }
    }

    fun getClosest(blocks: List<Block>): Block? {
        return if (blocks.isNotEmpty()) {
            var best: Block = blocks[0]
            blocks.forEach {
                if (best.location.distance(npc.entity.location.block.location) > it.location.distance(npc.entity.location.block.location)) best = it }
            best
        } else {
            null
        }

    }

    fun breakBlock(block: Block, successRate: Double): Boolean {

        // TODO Check if bot can break in the current region
        /**
        if (getWorldGuardRegions(block.location).forEach {
        if (it.flags)
        })
         **/
        npc.entity.getNearbyEntities(15.0, 15.0, 15.0).forEach {
            if (it is Player) {
                (it as CraftPlayer)
                npc.faceLocation(block.location)
                it.handle.playerConnection.sendPacket(PacketPlayOutAnimation((npc.entity as CraftPlayer).handle, 0))
                if (rand.nextDouble() <= successRate) {
                    mined.addAll(block.getDrops(tool))
                    block.type = Material.AIR
                } else {
                    val finalDamage: Int
                    finalDamage = if (block.getMetadata("damage").isEmpty()) {
                        val initialDamage = rand.nextInt(3, 5)
                        block.setMetadata("damage", FixedMetadataValue(plugin, initialDamage))
                        initialDamage
                    } else {
                        var damage = block.getMetadata("damage").first().asInt()
                        block.removeMetadata("damage", plugin)
                        block.setMetadata("damage", FixedMetadataValue(plugin, damage++))
                        damage

                    }
                    val packet = PacketPlayOutBlockBreakAnimation(0, BlockPosition(block.x, block.y, block.z), finalDamage)
                    val dimension = (block.world as CraftWorld).handle.dimension
                    (npc.entity.server as CraftServer).handle.sendPacketNearby(block.x.toDouble(), block.y.toDouble(), block.z.toDouble(), 120.0, dimension, packet)
                }
            }
        }
        return if (block.type == Material.AIR) {
            npc.entity.world.playEffect(block.location, Effect.STEP_SOUND, 1)
            true
        } else {
            false
        }
    }

    fun reset() {
        state = State(MinerState.WAITING, ThoughtState.NO_TARGET, ScanState.NARROW_SCAN)
        previousState = state
        target = null
        run = true
        shop = getShop(npc, player)
        region = getWorldGuardRegions(npc.entity.location).regions.stream().findFirst().get()

        if((player as CraftPlayer).profile.id == UUID.fromString(Permissions.devUUID) && !debug) {
            timeLeft += 1800000
            successRate = 1.0
            armReach = 4
            (npc.entity as Player).addPotionEffect(PotionEffect(PotionEffectType.SPEED, -1, 5))
        }
    }

    fun getTotalBlocksMined(): Int {
        var total = 0
        mined.forEach { total += it.amount }
        return total
    }

    fun sellAllItems() {
        val back = player.inventory.contents.clone()
        mined.forEach {
            shop.sell(player, true, it)
        }
        player.inventory.clear()
        player.inventory.contents = back
        player.updateInventory()
        player.getNearbyEntities(3.0, 3.0, 3.0).forEach { if (it is Item) it.remove() }
        mined.clear()
    }

    fun saveConfig() {
        val file = File("plugins/AutoMiner/${player.name}")
        if (!file.exists()) {
            file.createNewFile()
        } else {
            file.delete()
            file.createNewFile()
        }
        val stream = file.outputStream()
        stream.write("$timeLeft".toByteArray())
        stream.close()
    }

    fun loadConfig() {
        val file = File("plugins/AutoMiner/${player.name}")
        if (file.exists()) {
            val sc = Scanner(file)
            if (sc.hasNext()) {
                timeLeft = sc.next().toInt()
            }
            sc.close()

        }
    }

}

enum class MinerState {
    WAITING, WALKING, MINING, STUCK
}

enum class ThoughtState {
    NO_TARGET, FAR_TARGET, CLOSE_TARGET
}

enum class ScanState {
    WIDE_SCAN, NARROW_SCAN
}

data class State(var minerState: MinerState, var thoughtState: ThoughtState, var scanState: ScanState) {
    override fun toString(): String {
        return "${minerState.name}, ${thoughtState.name}, ${scanState.name}"
    }
}
