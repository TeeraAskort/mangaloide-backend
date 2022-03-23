package com.alderaeney.mangaloidebackend.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Scanlation {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "scanlation_id")
        private Long id;

        @ManyToMany(cascade = CascadeType.ALL)
        @JoinTable(name = "scan_users", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = {
                        @JoinColumn(name = "scanlation_id") })
        private List<User> users;

        @ManyToMany(cascade = CascadeType.ALL)
        @JoinTable(name = "scan_comics", joinColumns = { @JoinColumn(name = "comic_id") }, inverseJoinColumns = {
                        @JoinColumn(name = "scanlation_id") })
        private List<Comic> comics;
}
