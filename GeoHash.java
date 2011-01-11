import java.util.*;


public class GeoHash {

	// mean circumference of earth in kilometers
	private static final double cir = 40041.47;
	// circumference of earth divided by 360
	private static final double cird360 = 111.22631;
	// cosine of latitude in berlin
	private static final double cosine_lat_berlin = Math.cos(Math.toRadians(52.5));


	// hash tile size at reference latitude, this is the optimal radius for nearby search at reference latitude
	private double tile_size_at_ref_lat;

	// reference latitude
	// largest positive latitude that requires only search for direct neighbors with radius equal to tile size at reference latitude
	private double max_lat_with_simple_lookup;


	// scaling factors, these factors need to by multiplied by latitude or longitude, the resulting integer part is the hash key
	private double hash_scale_lat;
	private	double hash_scale_lon;

	private HashMap<Integer,GeoTile> hm;


	public GeoHash(double tile_size_at_ref_lat, double max_lat_with_simple_lookup, int initialCapacity) {
		this.tile_size_at_ref_lat = tile_size_at_ref_lat;
		this.max_lat_with_simple_lookup = max_lat_with_simple_lookup;

		set_lat_scale();
		set_lon_scale();

        //System.out.println(hash_scale_lat);
        //System.out.println(hash_scale_lon);

		hm = new HashMap<Integer,GeoTile>(initialCapacity);

	}


    public static void main(String[] args) {


		GeoHash gh = new GeoHash(1.0, 58.0, 100);


/*
                // somewhere in berlin
                double lat1 = 52.5004;
                double lat2 = 52.50035;
                double lon1 = 13.4006;
                double lon2 = 13.40055 ;


        //System.out.println(distance(lat1,lat2,lon1,lon2));




		// inserting a place into the hashmap
		Place pl = new Place(lat1,lon1);
		gh.addPlace(pl);


		// resize arrays to actual size
		gh.compress();


		// nearby search
		double search_radius = 1; // 1 km
		ArrayList<Place> search_results;
		search_results = gh.nearBySearch(lat1, lon1, search_radius, 0);

		System.out.println(search_results.size());
		for(int i=0;i<search_results.size();i++) {
			System.out.println(search_results.get(i));
		}


        //System.out.println(gh.get_num_horizontal_tiles(search_radius, lat1));
        //System.out.println(gh.get_num_vertical_tiles(search_radius));
*/


    }





	// circumference of earth along latitude at given latitude
	public static double cir(double lat) {
		return Math.cos(Math.toRadians(lat))*cir;
	}


	// distance of lat-lon coordinates, hopefully faster than Haversine, difference to Haversine: 1.5km for Berlin-Paris, 4m for Berlin-Potsdam (25km distance)
	public static double distance(double lat1, double lat2, double lon1, double lon2) {
            //    System.out.println("debug nearbysearch2 " + lat1 + " " + lat2 + " " + lon1 + " " + lon2);
		if(Math.abs(lat1-lat2)>180) // distances accross the date line
			if(lat1<lat2)
				lat1 = lat1 + 360;
			else
				lat2 = lat2 + 360;

		return cird360*Math.sqrt(Math.pow(lat1-lat2,2)+Math.pow(Math.cos(Math.toRadians((lat1+lat2)*0.5))*(lon1-lon2),2));
	}

	public static double distance(double lat, double lon, Place pl) {
		return distance(lat, pl.latitude, lon, pl.longitude);
	}


	// distance exact for Berlin, approximate for other latitudes
	public static double distance_approx(double lat1, double lat2, double lon1, double lon2) {
		return cird360*Math.sqrt(Math.pow(lat1-lat2,2)+Math.pow(cosine_lat_berlin*(lon1-lon2),2));
	}


	// lat scaling factor for geo hash for given tile size
	private void set_lat_scale() {
		hash_scale_lat = cird360/tile_size_at_ref_lat;
	}


	// lon scaling factor for geo hash for given latitude and given tile size
	private void set_lon_scale() {
		hash_scale_lon = (cird360*Math.cos(Math.toRadians(max_lat_with_simple_lookup))/tile_size_at_ref_lat);
	}




	// number of horizontal tiles that need to be searched in one direction for given radius and lat
	public int get_num_horizontal_tiles(double radius, double lat) {
		if(Math.abs(lat) < max_lat_with_simple_lookup && radius <= tile_size_at_ref_lat) {
			// short cut
			return 1;
		}
		return (int) Math.ceil(radius*hash_scale_lon*360/cir(lat));
	}

	// number of vertical tiles that need to be searched in one direction for given radius
	public int get_num_vertical_tiles(double radius) {
		if(radius <= tile_size_at_ref_lat) {
			// short cut
			return 1;
		}
		return (int) Math.ceil(radius*hash_scale_lat/cird360);
	}




	// compute hash key from lat-lon pair
	public int get_hash_key(double lat, double lon) {
		return get_hash_key((int) (lat*hash_scale_lat), (int) (lon*hash_scale_lon));
	}

	public int get_hash_key2(int lat_hash, int lon_hash) {
		if(Math.abs(lat_hash) <= 32767)
			return (lat_hash << 16)^lon_hash;
		else
			return hash32shift(lat_hash)^hash32shift(101*lon_hash);
	}


	public int get_hash_key(int lat_hash, int lon_hash) {
		return (lat_hash^lon_hash)^(lat_hash << 16);
	}



	public double getHash_scale_lat() {
		return hash_scale_lat;
	}

	public double getHash_scale_lon() {
		return hash_scale_lon;
	}


	private static int hash32shift(int key)
	{
		key = ~key + (key << 15); // key = (key << 15) - key - 1;
		key = key ^ (key >>> 12);
		key = key + (key << 2);
		key = key ^ (key >>> 4);
		key = key * 2057; // key = (key + (key << 3)) + (key << 11);
		key = key ^ (key >>> 16);
		return key;
	}


	public void addPlace(Place pl) {
		Integer key = new Integer(get_hash_key(pl.latitude, pl.longitude));
		GeoTile gt;
		gt = hm.get(key);
		if(gt!=null) {
			gt.addPlace(pl);
		}
		else {
			gt = new GeoTile();
			gt.addPlace(pl);
			hm.put(key,gt);
		}
	}


    public ArrayList<Place> nearBySearch(final double lat, final double lon, final double search_radius, int useEllipsoidFilter) {
        int lat_hash = (int) (lat*hash_scale_lat);
        int lon_hash = (int) (lon*hash_scale_lon);
        int htiles = get_num_horizontal_tiles(search_radius, lat);
        int vtiles = get_num_vertical_tiles(search_radius);

        ArrayList<Place> results = new ArrayList<Place>(100);

        for (int v = -vtiles; v <= vtiles; v++) {
            for (int h = -htiles; h <= htiles; h++) {
                GeoTile gt = hm.get(get_hash_key(v + lat_hash, h + lon_hash));
                if (gt != null) {
                    // filter tiles outside of the search radius in the tile indexing space
                    if (useEllipsoidFilter==0
                            || (Math.abs(v) <= Math.round(Math.sqrt(1 - Math.pow(h, 2) / Math.pow(htiles + 1, 2))
                            * (vtiles + 1)))) {
                        for(int k=0;k<gt.size();k++) {
                            double distance = distance(lat, lon, gt.getPlace(k));
                            if (distance <= search_radius) {
                              //  System.out.println("xxxxx debug nearbysearch" + search_radius + " " + distance + " " + lat + " " + gt.getPlace(k).latitude + " " + lon + " " + gt.getPlace(k).longitude);
                                results.add(gt.getPlace(k));
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

/*
	public ArrayList<Place> nearBySearch(double lat, double lon, double search_radius, int useEllipsoidFilter) {
		int lat_hash = (int) (lat*hash_scale_lat);
		int lon_hash = (int) (lon*hash_scale_lon);
		int htiles = get_num_horizontal_tiles(search_radius, lat);
		int vtiles = get_num_vertical_tiles(search_radius);
		GeoTile gt;
        //System.out.println(lat_hash+" " + lon_hash + " " + htiles + " " +vtiles);
        ArrayList<Place> results = new ArrayList<Place>(100);

		if(useEllipsoidFilter <= 0) {
			for(int i=lat_hash-vtiles;i<=lat_hash+vtiles;i++) {
				for(int j=lon_hash-htiles;j<=lon_hash+htiles;j++) {
					gt = hm.get(get_hash_key(i, j));
					//System.out.println(i+" " + j + " " + get_hash_key(i, j) + " " + gt);
					if(gt!=null) {
						//System.out.println("gt non null "+ gt.size());
						for(int k=0;k<gt.size();k++) {
							if(distance(lat, lon, gt.getPlace(k)) <= search_radius)
								results.add(gt.getPlace(k));
						}
					}
				}
			}
		}
		else {
			// assume ellipsoid and filter out tiles outside of ellipsoid, needs to be tested
			//System.out.println("with ellipsoid filter "+ htiles + " " + vtiles);
			for(int i=-vtiles;i<=vtiles;i++) {
				for(int j=-htiles;j<=htiles;j++) {
					if(Math.abs(i) <= Math.round(Math.sqrt(1-Math.pow(j,2)/Math.pow(htiles+1,2))*(vtiles+1))) {
						//System.out.println(i + " " + j + " " + (lat_hash+i)+" " + (lon_hash+j) + " " + get_hash_key(lat_hash+i, lon_hash+j));
						gt = hm.get(get_hash_key(i, j));
						if(gt!=null) {
							for(int k=0;k<gt.size();k++) {
								if(distance(lat, lon, gt.getPlace(k)) <= search_radius)
									results.add(gt.getPlace(k));
							}
						}
					}
				}
			}
		}
		return results;
	}

*/


	// trim arrays to actual size
	// iterate over hash, iterate over ArrayLists and call trimToSize()
	public void compress() {
		Collection hm_values = hm.values();
		Iterator hm_it = hm_values.iterator();
		while(hm_it.hasNext()) {
			((ArrayList) hm_it.next()).trimToSize();
		}
	}


}