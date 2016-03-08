package fr.battle.undefined.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class Player {

	private final int id;
	private final String name;
	private final Position caddy;
}
