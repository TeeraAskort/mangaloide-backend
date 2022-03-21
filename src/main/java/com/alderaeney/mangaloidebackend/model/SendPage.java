package com.alderaeney.mangaloidebackend.model;

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
