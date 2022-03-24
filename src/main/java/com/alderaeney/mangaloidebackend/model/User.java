package com.alderaeney.mangaloidebackend.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NonNull
    private String name;

    @NonNull
    @JsonIgnore
    private String password;

    @ElementCollection(targetClass = GrantedAuthority.class, fetch = FetchType.EAGER)
    private List<GrantedAuthority> authorities = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "comic_following", joinColumns = { @JoinColumn(name = "comic_id") }, inverseJoinColumns = {
            @JoinColumn(name = "user_id") })
    private List<Comic> comicsFollowing;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "chapters_read", joinColumns = { @JoinColumn(name = "chapter_id") }, inverseJoinColumns = {
            @JoinColumn(name = "user_id") })
    private List<Chapter> chaptersRead;

    @Override
    public String getUsername() {
        return this.name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
