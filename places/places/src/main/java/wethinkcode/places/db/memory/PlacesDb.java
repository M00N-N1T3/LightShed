package wethinkcode.places.db.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import wethinkcode.loadshed.commonHelpers;
import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

/**
 * I am a concrete database that implements the {@code Places} API. I do
 * not persist anything to permanent storage, but simply hold the entire
 * dataset in memory. Accordingly, I must get initialised with the data
 * for every instance of me that gets created.
 */
public class PlacesDb implements Places
{
    private final Set<Town> towns;
    public PlacesDb(Set<Town> towns){
            this.towns = towns;
    }


    @Override
    public Collection<String> provinces(){
        Set<String> provinces = new HashSet<>();
        towns.forEach(town -> provinces.add(town.getProvince()));
        return provinces;
    }

    @Override
    public Collection<Town> townsIn( String aProvince ){

        // making it dynamic that way whether you use dash or space it will work
        String province =  (aProvince.contains("-")
                && !aProvince.equalsIgnoreCase("kwazulu-natal"))
                ? aProvince.replace("-"," ") : aProvince;

        Set<Town> towns = this.towns.stream().filter(town1 -> town1.getProvince().equalsIgnoreCase(Helpers.capitalizeString(province)))
                .collect(Collectors.toSet());
        return towns;
    }

    public Collection<Town> getTowns(String aTown){
        Set<Town> towns = this.towns.stream().filter(town1 -> town1.getName().equalsIgnoreCase(aTown))
                .collect(Collectors.toSet());

        return towns;
    }

    public Town getTownFromProvince(String aTown, String aProvince){
        Set<Town> towns = (Set<Town>) townsIn(aProvince);
        Town town = towns.stream().filter(town1 -> town1.getName().equalsIgnoreCase(aTown))
                .findAny().orElse(null);

        return town;
    }

    @Override
    public int size(){
        return this.towns.size();
    }
}