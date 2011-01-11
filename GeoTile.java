import java.util.ArrayList;

public class GeoTile extends ArrayList {

	public GeoTile() {
		super(4);
	}

	public void addPlace(Place pl) {
		add(pl);
	}

	public Place getPlace(int index) {
		return (Place) get(index);
	}

}