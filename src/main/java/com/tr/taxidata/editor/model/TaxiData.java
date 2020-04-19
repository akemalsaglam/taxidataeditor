package com.tr.taxidata.editor.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "bursa_taxi_data")
public class TaxiData {

    private Long id;
    private Long taxiId;
    private Timestamp date;
    private int speed;
    private int length;
    private String direction;
    private String position;
    private String line;

    public TaxiData() {
    }

    @Id
    @Column(name = "id", unique = true)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "taxi_id")
    public Long getTaxiId() {
        return taxiId;
    }

    public void setTaxiId(Long taxiId) {
        this.taxiId = taxiId;
    }

    @Column(name = "date")
    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Column(name = "speed")
    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Column(name = "length")
    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Column(name = "direction")
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Column(name = "position")
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Column(name = "line")
    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }
}