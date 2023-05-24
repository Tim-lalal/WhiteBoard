package manager;

import java.io.Serializable;

public class TextData implements Serializable {
    private String name;

    private String text;

    public TextData(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + ": " + text;
    }
}
