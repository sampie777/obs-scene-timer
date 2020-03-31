package objects;

import java.util.ArrayList;

public class TScene {
    private String name = "";
    private ArrayList<TSource> sources = new ArrayList<>();

    public ArrayList<TSource> getSources() {
        return sources;
    }

    public void setSources(ArrayList<TSource> sources) {
        this.sources = sources;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
