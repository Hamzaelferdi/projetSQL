# Prompt utilisé pour générer `sample_data.sql`

Conformément à l'énoncé de l'étape 3 (« vous pouvez utiliser un LLM... à condition de conserver le prompt »).

## Contexte fourni au LLM

- Schéma SQL complet (`database_schema.sql`) avec ses contraintes :
  - `artist.name` est clé primaire (VARCHAR(100))
  - `gallery.name` est UNIQUE et référencé par `exhibition.gallery_name`
  - Triggers actifs : `workshop.date > NOW()`, `exhibition.end_date >= start_date`, `artwork.price >= 0`
  - CHECK : `review.rating ∈ [1,5]`
  - Date de référence : 2026-05-19
- Volume cible : ~12 artistes, 5-6 galeries, 15 œuvres, 6 expositions, 10 membres, 6 ateliers.

## Prompt

> Tu vas générer le fichier `sample_data.sql` qui peuple la base ArtConnect.
>
> Contraintes impératives :
> 1. Le script doit être idempotent — désactive les FK, `TRUNCATE` toutes les tables dans le bon ordre, ré-active les FK.
> 2. Tous les artistes, galeries, œuvres et expositions doivent être **réels** et **correctement attribués** (pas d'invention type « Starry Night par Da Vinci »).
> 3. Les membres et les workshops sont fictifs mais crédibles.
> 4. Toutes les dates de workshops doivent être strictement supérieures à 2026-05-19 (trigger `trg_workshop_date_validation`).
> 5. Les expositions ont `end_date >= start_date` et couvrent un mélange : passées, en cours autour de mai 2026, à venir.
> 6. Aucune œuvre n'a un prix négatif. Œuvres non-aliénables (Mona Lisa, Guernica, Cène) : prix à 0 et statut `EXHIBITED`.
> 7. Renseigne la table de jonction `artist_discipline` ainsi que `artwork_tag`, `booking`, `review` pour démontrer toutes les relations du MCD.
> 8. Utilise des sous-requêtes `(SELECT id FROM ... WHERE name=...)` pour résoudre les FKs vers les tables à clé auto-incrémentée (membres, workshops).
> 9. Évite les caractères accentués hors apostrophes échappées par doublement (`O''Connor`, `d''huile`).
> 10. Documente chaque section avec un en-tête de commentaire SQL.

## Vérification après exécution

```sql
SELECT COUNT(*) FROM artist;           -- doit retourner 12
SELECT COUNT(*) FROM gallery;          -- 6
SELECT COUNT(*) FROM artwork;          -- 15
SELECT COUNT(*) FROM exhibition;       -- 6
SELECT COUNT(*) FROM community_member; -- 10
SELECT COUNT(*) FROM workshop;         -- 6
SELECT COUNT(*) FROM booking;          -- 10
SELECT COUNT(*) FROM review;           -- 10

-- Vérifier que les attributions sont cohérentes :
SELECT a.title, a.artist_name FROM artwork a ORDER BY a.artist_name;

-- Vérifier qu'aucune date n'enfreint les triggers :
SELECT title, date FROM workshop WHERE date <= NOW();   -- doit être vide
SELECT title FROM exhibition WHERE end_date < start_date; -- doit être vide
```
