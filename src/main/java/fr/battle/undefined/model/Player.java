package fr.battle.undefined.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class Player {

	private final long id;
	private final Position caddy;
	@Setter
	private boolean full;

	// Is Carrying <logo>
	private int superPowerCount = 3;

	public void decreaseSuperPower() {
		superPowerCount--;
	}
}
