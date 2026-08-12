package com.perengano99.villagium.client.gui.screens;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.entity.VillagiumMob;
import com.perengano99.villagium.social.profile.ProfileData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class NpcMenuScreen<T extends VillagiumMob<T>> extends NpcInteractionScreen {
	
	private static final Identifier CONTAINER = Identifier.fromNamespaceAndPath(Villagium.MODID, "container/interaction_menu");
	
	
	public final T entity;
	public final ProfileData profile;
	
	protected NpcMenuScreen(T entity, ProfileData profile) {
		super(null);
		Minecraft mc = Minecraft.getInstance();
		this.entity  = entity;
		this.profile = profile;
	}
}
