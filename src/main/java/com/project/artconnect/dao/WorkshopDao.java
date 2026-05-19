package com.project.artconnect.dao;

import com.project.artconnect.model.Workshop;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour l'entite Workshop.
 *
 * <p>Le {@code title} est utilise comme identifiant naturel cote application
 * meme si la PK de la table est un {@code id} auto-incremente : cela permet
 * de garder une API coherente avec les autres DAO (Artist, Artwork, ...).</p>
 */
public interface WorkshopDao {

    /** Retourne le workshop dont l'id technique correspond. */
    Optional<Workshop> findById(Long id);

    /** Retourne le workshop dont le titre correspond. */
    Optional<Workshop> findByTitle(String title);

    /** Retourne tous les workshops. */
    List<Workshop> findAll();

    /** Insere un nouveau workshop. */
    void save(Workshop workshop);

    /** Met a jour un workshop existant (identifie par son titre). */
    void update(Workshop workshop);

    /** Supprime un workshop par titre. */
    void delete(String title);
}
