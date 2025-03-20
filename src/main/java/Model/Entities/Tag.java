package Model.Entities;

import java.util.Objects;

public class Tag {
    private int tagId;
    private String name;
    private String color;

    public Tag(int tagId, String name, String color) {
        this.tagId = tagId;
        this.name = name;
        this.color = color;
    }

    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name) && Objects.equals(color, tag.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, name, color);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "tagId=" + tagId +
                ", name='" + name + '\'' +
                ", color=" + color +
                '}';
    }
}
