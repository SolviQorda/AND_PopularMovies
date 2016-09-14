package qorda_projects.popularmovies;

/**
 * Created by sorengoard on 12/09/16.
 */
public class MovieReview {
    String review_author;
    String review_content;

    //constructor
    public MovieReview(){}

    //G&S author
    public String getAuthor() {return review_author;}
    public void setAuthor(String author) {this.review_author = author;}

    //G&S content
    public String getContent() {return review_content;}
    public void setContent(String content) {this.review_content = content;}

}
