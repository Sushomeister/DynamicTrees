package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VanillaSaplingEventHandler {

	@SubscribeEvent //EVENT_BUS
	public void onPlayerPlaceBlock(PlaceEvent event) {
		IBlockState blockState = event.getPlacedBlock();

		if (!TreeRegistry.saplingReplacers.containsKey(blockState)) {
			return;
		}

		final Species species = TreeRegistry.saplingReplacers.get(blockState);
		final World world = event.getWorld();
		final BlockPos pos = event.getPos();

		world.setBlockToAir(pos); // Set the block to air so the plantTree function won't automatically fail.

		if (!species.plantSapling(world, pos)) { // If it fails then give a seed back to the player.
			ItemUtils.spawnItemStack(world, pos, species.getSeedStack(1));
		}
	}

	@SubscribeEvent    //TERRAIN_GEN_BUS
	public void onSaplingGrowTree(SaplingGrowTreeEvent event) {
		final World world = event.getWorld();
		final BlockPos pos = event.getPos();
		final IBlockState blockState = world.getBlockState(pos);

		if (!TreeRegistry.saplingReplacers.containsKey(blockState)) {
			return;
		}

		final Species species = TreeRegistry.saplingReplacers.get(blockState);

		world.setBlockToAir(pos); // Set the block to air so the plantTree function won't automatically fail.
		event.setResult(Result.DENY);

		if (species.isValid()) {
			if (BlockDynamicSapling.canSaplingStay(world, species, pos)) {
				species.transitionToTree(world, pos);
			}
		}
	}

}
