package com.alderaeney.mangaloidebackend.model;

import java.util.List;

import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendPage<T> {
    int number;
    int size;
    long totalElements;
    int totalPages;
    List<T> content;
    Sort sort;
}
