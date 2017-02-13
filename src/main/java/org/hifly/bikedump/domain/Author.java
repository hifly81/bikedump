package org.hifly.bikedump.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

@DatabaseTable(tableName = "author")
public class Author {
    @DatabaseField(id = true)
    protected int id;
    @DatabaseField(canBeNull = false)
    protected String name;
    @DatabaseField
    protected String email;
    @ForeignCollectionField(eager = false)
    protected Collection<Track> tracks;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Collection<Track> getTracks() {
        return tracks;
    }

    public void setTracks(Collection<Track> tracks) {
        this.tracks = tracks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String toString() {
        return "[Author]<br>"+id+","+name+","+email+"<br>"+
                (tracks==null?"":tracks.size());
    }

}
