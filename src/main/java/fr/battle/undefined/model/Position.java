package fr.battle.undefined.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode(of = { "x", "y" })
public class Position {

	private final int x;
	private final int y;
}
