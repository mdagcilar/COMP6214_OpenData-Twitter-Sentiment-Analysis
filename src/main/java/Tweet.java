/**
 * Created by damyanrusinov on 04/04/2017.
 */
public class Tweet {

    private String owner;
    private String text;

    public Tweet(String owner, String text) {
        this.owner = owner;
        this.text = text;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getTweet() {
        return this.text;
    }
}
