package fr.battle.undefined.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum Action {
	NORD("N"), SUD("S"), EST("E"), OUEST("O"), JUMP_NORD("JN"), JUMP_SUD(
			"JS"), JUMP_EST("JE"), JUMP_OUEST("JO"), NORD2("N"), SUD2(
					"S"), EST2("E"), OUEST2("O"), NORD3("N"), SUD3("S"), EST3(
							"E"), OUEST3("O");
	private final String code;
}
