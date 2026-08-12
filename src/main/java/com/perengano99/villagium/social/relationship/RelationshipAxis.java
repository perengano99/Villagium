package com.perengano99.villagium.social.relationship;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum RelationshipAxis {
	FRIENDSHIP(0, 1, 2, 3, -100f, 100f),
	TRUST(4, 5, 6, 7, -100f, 100f),
	LOVE(8, 9, 10, 11, 0f, 100f),
	POLITICAL_ALIGNMENT(12, 13, 14, 15, -100f, 100f),
	RESENTMENT(16, 17, 18, 19, 0f, 100f),
	RESPECT(20, 21, 22, 23, -100f, 100f),
	EMPATHY(24, 25, 26, 27, 0f, 100f),
	ENVY(28, 29, 30, 31, 0f, 100f),
	FEAR(32, 33, 34, 35, 0f, 100f);

	public static final int ATLAS_SIZE = 35;
	private final int hp, p, n, hn;
	private final float min;
	private final float max;

	RelationshipAxis(int hp, int p, int n, int hn, float min, float max) {
		this.hp = hp;
		this.p  = p;
		this.n  = n;
		this.hn = hn;
		this.min = min;
		this.max = max;
	}

	public float getMin() {
		return min;
	}

	public float getMax() {
		return max;
	}

	public int getAtlasIndex(EffectLevel level) {
		return switch(level) {
			case HIGH_POSITIVE -> hp;
			case POSITIVE -> p;
			case NEGATIVE -> n;
			case HIGH_NEGATIVE -> hn;
		};
	}

	public enum EffectLevel {
		HIGH_POSITIVE, POSITIVE, NEGATIVE, HIGH_NEGATIVE
	}

	public static final Codec<RelationshipAxis> CODEC = Codec.STRING.xmap(
			s -> valueOf(s.toUpperCase(Locale.ROOT)),
			Enum::name
	);
}
