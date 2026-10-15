package com.coinis.splitter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    // Iznos u centima, nikad float
    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payer_id", nullable = false)
    private Participant payer;

    protected Expense() {
    }

    public Expense(String title, long amountCents, Participant payer) {
        this.title = title;
        this.amountCents = amountCents;
        this.payer = payer;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public Participant getPayer() {
        return payer;
    }
}
