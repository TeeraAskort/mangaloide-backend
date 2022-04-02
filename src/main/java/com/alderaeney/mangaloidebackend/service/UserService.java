package com.alderaeney.mangaloidebackend.service;

import java.util.List;
import java.util.Optional;

import com.alderaeney.mangaloidebackend.model.User;
import com.alderaeney.mangaloidebackend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> getUserById(Long id) {
        return repository.findById(id);
    }

    public void removeUserById(Long id) {
        repository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public Page<User> findAllByNameContaining(String name, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return repository.findByNameIgnoreCaseContaining(name, pageable);
    }

    public User addUser(User user) {
        return repository.save(user);
    }

    public Optional<User> findByName(String username) {
        return repository.findByName(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = repository.findByName(username);
        if (user.isPresent()) {
            return user.get();
        }
        return null;
    }

}
