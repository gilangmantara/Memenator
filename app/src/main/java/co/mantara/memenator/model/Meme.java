package co.mantara.memenator.model;

/**
 * Created by Gilang on 04/09/2016.
 * gilangmantara@gmail.com
 */
public class Meme {
    private String name;
    private String path;

    public Meme(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
