package com.devtau.popularmoviess2.model;

/**
 * Объекты этого класса являются частью листа внутри фильма
 * Objects of this class are part of list in every movie
 */
public class Trailer {
    private long id = -1;
    private String source;
    private String name;
    private String size;
    private String type;
    private long movieId;

    public Trailer(String source, String name, String size, String type, long movieId) {
        this.source = source;
        this.name = name;
        this.size = size;
        this.type = type;
        this.movieId = movieId;
    }

    //getters
    public long getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public long getMovieId() {
        return movieId;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass() || id == -1) return false;
        Trailer that = (Trailer) obj;
        if (that.id == -1) return false;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id != -1 ? 31 * id : 0);
    }
}
