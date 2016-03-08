package fr.battle.undefined.model;

@Getter
@ToString
@RequiredArgsConstructor
public class Player {

	private final int id;
	private final String name;
	private final Position caddy;
	@Setter
	private boolean full;
}
