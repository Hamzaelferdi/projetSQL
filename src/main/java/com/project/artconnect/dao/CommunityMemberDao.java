package com.project.artconnect.dao;

import com.project.artconnect.model.CommunityMember;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour l'entite CommunityMember.
 *
 * <p>L'application identifie les membres par leur {@code name} ; en base, la PK
 * est un {@code id} auto-incremente, mais on expose ici une API de recherche
 * par nom (et par email) plus naturelle.</p>
 */
public interface CommunityMemberDao {

    /** Recherche par id technique (PK). */
    Optional<CommunityMember> findById(Long id);

    /** Recherche par nom (identifiant naturel cote app). */
    Optional<CommunityMember> findByName(String name);

    /** Liste tous les membres. */
    List<CommunityMember> findAll();

    /** Insere un nouveau membre. */
    void save(CommunityMember member);

    /** Met a jour un membre existant (identifie par son nom). */
    void update(CommunityMember member);

    /** Supprime un membre par nom. */
    void delete(String name);
}
