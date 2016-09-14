package qorda_projects.popularmovies;

/**
 * Created by sorengoard on 12/09/16.
 */
public class MovieTrailer {
    String trailer_key;
    String trailer_name;

    //constructor
    public MovieTrailer() {}

    //G&S key
    public String getTrailerKey() {return trailer_key;}
    public void setTrailerKey(String key) {this.trailer_key = key;}

    //G&S name
    public String getTrailerName() {return trailer_name;}
    public void setTrailerName(String name) {this.trailer_name = name;}
}
