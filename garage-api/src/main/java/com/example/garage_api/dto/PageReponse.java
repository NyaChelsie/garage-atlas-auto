package com.example.garage_api.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

// <T> : "type quelconque". Le meme record sert pour les clients, les vehicules, les pieces.
public record PageReponse<T>(
        List<T> contenu,
        int page,
        int taille,
        long totalElements,
        int totalPages
) {
    // Convertit une Page d'entites (E) en PageReponse de DTO (T).
    // Function<E, T> : "une fonction qui transforme un E en T" (ex : ClientReponse::depuis).
    public static <E, T> PageReponse<T> de(Page<E> page, Function<E, T> convertisseur) {
        return new PageReponse<>(
                // map applique le convertisseur a chaque element de la page.
                page.getContent().stream().map(convertisseur).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}