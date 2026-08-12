package com.perengano99.villagium.client.gui.screens;

import com.perengano99.villagium.client.animation.face.AngryStaticFaceAnimation;
import com.perengano99.villagium.client.animation.face.StaticFaceExpressionAnimation;
import com.perengano99.villagium.client.gui.widgets.NpcCardWidget;
import com.perengano99.villagium.client.gui.widgets.NpcPortraitWidget;
import com.perengano99.villagium.data.VillagiumData;
import com.perengano99.villagium.entity.npc.NvVillager;
import com.perengano99.villagium.network.packets.server.OpenNpcMenuPacket;
import com.perengano99.villagium.social.relationship.RelationTag;
import com.perengano99.villagium.social.relationship.RelationshipData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VillagerNpcMenuScreen extends NpcMenuScreen<NvVillager> {
	
	private RelationshipData.ClientData relationData;
	private NpcPortraitWidget portraitWidget;
	private boolean isAngryExpression = false;
	
	public VillagerNpcMenuScreen(OpenNpcMenuPacket payload) {
		super((NvVillager) Minecraft.getInstance().level.getEntity(payload.entityId()), payload.profileData());
		this.relationData = payload.relationData();
	}
	
	@Override
	protected void init() {
		List<NpcCardWidget.CardTag> cardTags = new ArrayList<>(relationData.activeTags().keySet().stream().map(id -> {
			RelationTag tag = VillagiumData.RELATION_TAGS.get(id);
			return tag != null ? new NpcCardWidget.CardTag(Component.translatable(tag.displayKey()), tag.displayColor()) : null;
		}).filter(Objects::nonNull).toList());
		
		int portraitSize = 54;
		int portraitX = this.width - 140 - portraitSize - 6;
		int portraitY = 10;
		this.portraitWidget = new NpcPortraitWidget(portraitX, portraitY, portraitSize, portraitSize, this.entity);
		addRenderableWidget(this.portraitWidget);
		
		addRenderableWidget(new NpcCardWidget(this.width - 140, 0, 140, 95, profile, cardTags));
		
		int btnWidth = 70;
		int btnHeight = 16;
		int btnX = portraitX - btnWidth - 4;
		
		addRenderableWidget(Button.builder(Component.literal("Alternar"), btn -> {
			isAngryExpression = !isAngryExpression;
			if (isAngryExpression)
				portraitWidget.setExpression(new AngryStaticFaceAnimation<>());
			else
				portraitWidget.setExpression(new StaticFaceExpressionAnimation<>());
		}).bounds(btnX, portraitY, btnWidth, btnHeight).build());
		addRenderableWidget(Button.builder(Component.literal("Enojado 5s"), btn -> portraitWidget.setTimedExpression(new AngryStaticFaceAnimation<>(), 100))
				.bounds(btnX, portraitY + btnHeight + 2, btnWidth, btnHeight).build());
	}
	
	public NpcPortraitWidget getPortraitWidget() {
		return portraitWidget;
	}
}
