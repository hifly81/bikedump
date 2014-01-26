package org.hifly.geomapviewer.domain;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

/**
 * @author
 * @date 26/01/14
 */
public class Track {
    @DatabaseField(id = true)
    protected int id;
    @DatabaseField(canBeNull = false)
    protected String name;
    @DatabaseField(canBeNull = false)
    protected Date startDate;
    @DatabaseField(canBeNull = false)
    protected Date endDate;
    @DatabaseField(canBeNull = false, foreign = true)
    protected Author author;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String toString() {
        return "[Track] --> "+id+","+name+","+author;
    }

}
