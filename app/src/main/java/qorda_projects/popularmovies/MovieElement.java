package qorda_projects.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sorengoard on 12/08/16.
 */
public class MovieElement implements Parcelable {
    String movie_title;
    String movie_synopsis;
    String movie_releaseDate;
    String movie_userRating;
    String movie_posterUrl;
    String movie_id;
    MovieReview[] movie_reviews;
    MovieTrailer[] movie_trailers;
    int favourite_status;

    public MovieElement() {}

    public String getTitle() {return movie_title;}

    public void setTitle(String title) {
        this.movie_title = title;
    }

    public String getSynopsis() {
        return movie_synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.movie_synopsis = synopsis;
    }

    public String getReleaseDate() {return movie_releaseDate;}

    public void setReleaseDate(String releaseDate) {
        this.movie_releaseDate = releaseDate;
    }

    public String getUserRating() {
        return movie_userRating;
    }

    public void setUserRating(String userRating) {
        this.movie_userRating = userRating;
    }

    public String getPosterUrl() {
        return movie_posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.movie_posterUrl = posterUrl;
    }

    public String getMovieId() {return movie_id;}

    public void setMovieId(String id){this.movie_id = id;}

    public MovieReview[] getMovieReviews() {return movie_reviews;}

    public void setMovieReviews(MovieReview[] movieReviews) {this.movie_reviews = movieReviews;}

    public MovieTrailer[] getMovieTrailers() {return movie_trailers;}

    public void setMovie_trailers(MovieTrailer[] movieTrailers) {this.movie_trailers = movieTrailers;}

    public int getFavouriteStatus() {return favourite_status;}

    public void setFavouriteStatus(int favouriteStatus) {this.favourite_status = favouriteStatus;}


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel pc, int flags) {
        pc.writeString(movie_title);
        pc.writeString(movie_synopsis);
        pc.writeString(movie_releaseDate);
        pc.writeString(movie_userRating);
        pc.writeString(movie_posterUrl);
        pc.writeString(movie_id);
    }
    public static final Parcelable.Creator<MovieElement> CREATOR = new Parcelable.Creator<MovieElement>() {
        public MovieElement createFromParcel(Parcel pc)
        {
            return new MovieElement(pc);
        }
        public MovieElement[] newArray(int size) {
            return new MovieElement[size];
        }
    };

    public MovieElement(Parcel pc) {
        movie_title = pc.readString();
        movie_synopsis = pc.readString();
        movie_releaseDate = pc.readString();
        movie_userRating = pc.readString();
        movie_posterUrl = pc.readString();
        movie_id = pc.readString();
    }
}
