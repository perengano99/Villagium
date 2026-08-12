package com.perengano99.villagium.social.condition;

import com.google.gson.JsonElement;
import com.perengano99.villagium.social.context.SocialEventContext;

public interface ISocialCondition {

	enum ExecutionSide {
		BOTH,
		SERVER,
		CLIENT
	}

	boolean check(SocialEventContext context, JsonElement params);

	ExecutionSide getSide();
}
