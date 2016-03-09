package fr.battle.undefined.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor
public class Player {

	private final long id;
	private final Position caddy;

	// Is Carrying <logo>
	private int superPowerCount = 3;

	public void decreaseSuperPower() {
		superPowerCount--;
	}
}
