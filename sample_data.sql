-- =====================================================================
-- ArtConnect - Jeu de donnees d'exemple (sample_data.sql)
-- ---------------------------------------------------------------------
-- A executer apres database_schema.sql.
-- Le script est idempotent : il vide les tables (FK desactivees) avant
-- de re-inserer un jeu coherent.
--
-- Donnees :
--   * Artistes : 12 figures reelles (historiques + contemporains)
--   * Galeries : 6 musees reels (Louvre, Tate Modern, MoMA, ...)
--   * Oeuvres  : 15 oeuvres reelles correctement attribuees
--   * Expositions : 6 (passees / en cours / a venir)
--   * Membres  : 10 personnes fictives mais credibles
--   * Workshops : 6 ateliers fictifs (dates futures, > 2026-05-19,
--                 contrainte trg_workshop_date_validation)
--   * Bookings, reviews, tags d'oeuvres
--
-- Toutes les dates respectent les triggers du schema :
--   - workshop.date > NOW()
--   - exhibition.end_date >= start_date
--   - artwork.price >= 0
--   - review.rating entre 1 et 5
-- =====================================================================

USE artconnect_db;

-- ---------------------------------------------------------------------
-- Reset (FK desactivees pour vider dans n'importe quel ordre)
-- ---------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE review;
TRUNCATE TABLE booking;
TRUNCATE TABLE artwork_tag;
TRUNCATE TABLE artist_discipline;
TRUNCATE TABLE workshop;
TRUNCATE TABLE artwork;
TRUNCATE TABLE exhibition;
TRUNCATE TABLE community_member;
TRUNCATE TABLE gallery;
TRUNCATE TABLE artist;
TRUNCATE TABLE discipline;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 1. DISCIPLINES
-- ---------------------------------------------------------------------
INSERT INTO discipline (name, description) VALUES
('Painting',         'Peinture (huile, acrylique, aquarelle...)'),
('Sculpture',        'Art tridimensionnel - marbre, bronze, terre...'),
('Photography',      'Capture d''images, argentique ou numerique'),
('Digital Art',      'Oeuvres realisees a l''aide d''outils numeriques'),
('Street Art',       'Art urbain, graffiti, pochoir, fresques'),
('Performance Art',  'Art performatif, le corps comme medium'),
('Installation Art', 'Installation in situ, environnement immersif');

-- ---------------------------------------------------------------------
-- 2. ARTISTES (figures reelles)
-- ---------------------------------------------------------------------
-- Note : pour les artistes encore vivants on garde is_active = TRUE ;
-- les autres restent TRUE car la BD represente leur catalogue accessible.
INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active) VALUES
('Leonardo da Vinci',    'Polymathe italien de la Renaissance, peintre du Quattrocento.', 1452, 'contact@davinci-foundation.it', '+39-055-1234567', 'Florence',    'www.leonardodavinci.com',  '@davinci_official',   TRUE),
('Vincent van Gogh',     'Peintre neerlandais post-impressionniste, auteur d''auto-portraits emblematiques.', 1853, 'estate@vangoghmuseum.nl',    '+31-20-5705200',  'Auvers-sur-Oise','www.vangoghmuseum.nl',  '@vincent_vangogh',    TRUE),
('Claude Monet',         'Fondateur de l''impressionnisme francais, jardins de Giverny.',                    1840, 'contact@fondation-monet.fr', '+33-2-32512128',  'Giverny',      'www.fondation-monet.com', '@claude_monet',       TRUE),
('Pablo Picasso',        'Co-fondateur du cubisme, peintre, sculpteur et ceramiste espagnol.',               1881, 'museu.picasso@bcn.cat',      '+34-93-2563000',  'Barcelona',    'www.museupicasso.bcn.cat', '@picasso_museu',     TRUE),
('Frida Kahlo',          'Peintre mexicaine, oeuvre marquee par la douleur, l''identite et le folklore.',    1907, 'museo@museofridakahlo.org',  '+52-55-55545999', 'Mexico City',  'www.museofridakahlo.org', '@frida_kahlo',        TRUE),
('Auguste Rodin',        'Sculpteur francais, precurseur de la sculpture moderne.',                          1840, 'info@musee-rodin.fr',        '+33-1-44186110',  'Paris',        'www.musee-rodin.fr',    '@museerodin',         TRUE),
('Salvador Dali',        'Peintre, sculpteur et ecrivain catalan, figure majeure du surrealisme.',           1904, 'info@salvador-dali.org',     '+34-972-677500',  'Figueres',     'www.salvador-dali.org', '@salvadordaliofficial', TRUE),
('Henri Matisse',        'Peintre francais, fondateur du fauvisme, decoupages tardifs celebres.',            1869, 'contact@musee-matisse-nice.org','+33-4-93810808','Nice',         'www.musee-matisse-nice.org', '@henrimatisse',  TRUE),
('Andy Warhol',          'Figure du Pop Art americain, serigraphies de Marilyn et boites de soupe.',        1928, 'info@warhol.org',            '+1-412-2378300',  'New York',     'www.warhol.org',        '@andywarhol',         TRUE),
('Yayoi Kusama',         'Artiste japonaise contemporaine, installations a pois et infinity rooms.',         1929, 'studio@yayoi-kusama.jp',     '+81-3-32261111',  'Tokyo',        'www.yayoi-kusama.jp',   '@yayoikusama_',       TRUE),
('Banksy',               'Artiste de rue britannique anonyme, oeuvres politiques au pochoir.',               1974, 'contact@pestcontroloffice.com','+44-20-71831234','Bristol',      'www.banksy.co.uk',      '@banksy',             TRUE),
('Marina Abramovic',     'Pionniere de la performance art, ex-Yougoslave installee a New York.',             1946, 'office@mai-hudson.org',      '+1-518-9436900',  'New York',     'www.marinaabramovicinstitute.org','@abramovicinstitute', TRUE);

-- ---------------------------------------------------------------------
-- 3. ARTIST_DISCIPLINE (relation many-to-many)
-- ---------------------------------------------------------------------
INSERT INTO artist_discipline (artist_name, discipline_id) VALUES
('Leonardo da Vinci',  (SELECT id FROM discipline WHERE name='Painting')),
('Leonardo da Vinci',  (SELECT id FROM discipline WHERE name='Sculpture')),
('Vincent van Gogh',   (SELECT id FROM discipline WHERE name='Painting')),
('Claude Monet',       (SELECT id FROM discipline WHERE name='Painting')),
('Pablo Picasso',      (SELECT id FROM discipline WHERE name='Painting')),
('Pablo Picasso',      (SELECT id FROM discipline WHERE name='Sculpture')),
('Frida Kahlo',        (SELECT id FROM discipline WHERE name='Painting')),
('Auguste Rodin',      (SELECT id FROM discipline WHERE name='Sculpture')),
('Salvador Dali',      (SELECT id FROM discipline WHERE name='Painting')),
('Salvador Dali',      (SELECT id FROM discipline WHERE name='Sculpture')),
('Henri Matisse',      (SELECT id FROM discipline WHERE name='Painting')),
('Andy Warhol',        (SELECT id FROM discipline WHERE name='Painting')),
('Andy Warhol',        (SELECT id FROM discipline WHERE name='Digital Art')),
('Yayoi Kusama',       (SELECT id FROM discipline WHERE name='Installation Art')),
('Yayoi Kusama',       (SELECT id FROM discipline WHERE name='Painting')),
('Banksy',             (SELECT id FROM discipline WHERE name='Street Art')),
('Marina Abramovic',   (SELECT id FROM discipline WHERE name='Performance Art'));

-- ---------------------------------------------------------------------
-- 4. GALERIES / MUSEES (lieux reels)
-- ---------------------------------------------------------------------
INSERT INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES
('Musee du Louvre',     'Rue de Rivoli, 75001 Paris, France',           'Etat francais',  'Mer-Lun 9h-18h, ferme Mar',   '+33-1-40205050', 4.8, 'www.louvre.fr'),
('Tate Modern',         'Bankside, London SE1 9TG, Royaume-Uni',        'Tate Trust',     'Tlj 10h-18h',                 '+44-20-78878888',4.7, 'www.tate.org.uk'),
('Museum of Modern Art','11 West 53rd Street, New York, NY 10019',      'MoMA Board',     'Tlj 10h30-17h30',             '+1-212-7089400', 4.7, 'www.moma.org'),
('Centre Pompidou',     'Place Georges-Pompidou, 75004 Paris, France',  'Etat francais',  'Mer-Lun 11h-21h, ferme Mar',  '+33-1-44781233', 4.6, 'www.centrepompidou.fr'),
('Museo Reina Sofia',   'Calle Santa Isabel 52, 28012 Madrid, Espagne', 'Estado espanol', 'Lun-Sam 10h-21h, Dim 10h-14h30','+34-91-7741000',4.7, 'www.museoreinasofia.es'),
('Musee d''Orsay',      '1 Rue de la Legion d''Honneur, 75007 Paris',   'Etat francais',  'Mar-Dim 9h30-18h, ferme Lun', '+33-1-40494814', 4.8, 'www.musee-orsay.fr');

-- ---------------------------------------------------------------------
-- 5. EXPOSITIONS (dates : passees / en cours / a venir, base = 2026-05-19)
-- ---------------------------------------------------------------------
INSERT INTO exhibition (title, start_date, end_date, description, gallery_name, curator_name, theme) VALUES
('Renaissance Revisited',     '2026-03-15', '2026-07-30', 'Relecture des grands maitres de la Renaissance italienne.',          'Musee du Louvre',     'Beatrice Salmon',    'Renaissance'),
('Impressionnisme et Lumiere','2026-04-10', '2026-09-15', 'Parcours autour de l''ecole impressionniste francaise.',              'Musee d''Orsay',      'Sylvie Patry',       'Impressionnisme'),
('Pop Forever',               '2026-05-01', '2026-10-12', 'Andy Warhol et la circulation des images dans le Pop Art.',           'Tate Modern',         'Frances Morris',     'Pop Art'),
('Cubisme et Modernite',      '2026-06-20', '2026-11-08', 'Picasso, Braque et l''invention du cubisme.',                         'Museo Reina Sofia',   'Manuel Borja-Villel','Cubisme'),
('Infinity Mirrors',          '2026-07-05', '2026-12-15', 'Les chambres d''infini de Yayoi Kusama.',                             'Museum of Modern Art','Mika Yoshitake',     'Installation'),
('Voix de la Rue',            '2025-11-10', '2026-03-30', 'Retrospective du street art - Banksy, JR, Shepard Fairey.',           'Centre Pompidou',     'Bernard Blistene',   'Street Art');

-- ---------------------------------------------------------------------
-- 6. OEUVRES (toutes correctement attribuees)
-- ---------------------------------------------------------------------
INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_name) VALUES
('Mona Lisa',                  1503, 'Painting',    'Huile sur peuplier',       '77 x 53 cm',   'Portrait enigmatique de Lisa Gherardini.',                        1000000.00, 'EXHIBITED', 'Leonardo da Vinci'),
('La Cene',                    1498, 'Painting',    'Tempera sur platre',       '460 x 880 cm', 'Fresque du refectoire de Santa Maria delle Grazie.',              0.00,       'EXHIBITED', 'Leonardo da Vinci'),
('La Nuit etoilee',            1889, 'Painting',    'Huile sur toile',          '74 x 92 cm',   'Vue depuis l''asile de Saint-Remy-de-Provence.',                  900000.00,  'EXHIBITED', 'Vincent van Gogh'),
('Les Tournesols',             1888, 'Painting',    'Huile sur toile',          '92 x 73 cm',   'Serie peinte a Arles, jaunes de chrome.',                         500000.00,  'EXHIBITED', 'Vincent van Gogh'),
('Nympheas',                   1916, 'Painting',    'Huile sur toile',          '200 x 200 cm', 'Bassin du jardin de Giverny, serie tardive.',                     750000.00,  'EXHIBITED', 'Claude Monet'),
('Impression, soleil levant',  1872, 'Painting',    'Huile sur toile',          '48 x 63 cm',   'Oeuvre fondatrice de l''impressionnisme.',                        850000.00,  'EXHIBITED', 'Claude Monet'),
('Guernica',                   1937, 'Painting',    'Huile sur toile',          '349 x 776 cm', 'Denonciation du bombardement de Guernica.',                       0.00,       'EXHIBITED', 'Pablo Picasso'),
('Les Demoiselles d''Avignon', 1907, 'Painting',    'Huile sur toile',          '244 x 234 cm', 'Acte de naissance du cubisme.',                                   0.00,       'EXHIBITED', 'Pablo Picasso'),
('Les Deux Fridas',            1939, 'Painting',    'Huile sur toile',          '173 x 173 cm', 'Double autoportrait de Frida Kahlo.',                             450000.00,  'EXHIBITED', 'Frida Kahlo'),
('Le Penseur',                 1904, 'Sculpture',   'Bronze',                   '189 x 98 cm',  'Figure assise emblematique d''Auguste Rodin.',                    600000.00,  'EXHIBITED', 'Auguste Rodin'),
('La Persistance de la memoire',1931,'Painting',    'Huile sur toile',          '24 x 33 cm',   'Montres molles, paysage onirique.',                               700000.00,  'EXHIBITED', 'Salvador Dali'),
('La Danse',                   1910, 'Painting',    'Huile sur toile',          '260 x 391 cm', 'Cinq figures en ronde, fauvisme.',                                550000.00,  'EXHIBITED', 'Henri Matisse'),
('Marilyn Diptych',            1962, 'Painting',    'Serigraphie sur toile',    '205 x 289 cm', 'Cinquante portraits de Marilyn Monroe.',                          400000.00,  'EXHIBITED', 'Andy Warhol'),
('Infinity Mirror Room',       2013, 'Installation','Miroirs, LED, eau',        'Variable',     'Chambre des miroirs, immersion infinie.',                         300000.00,  'EXHIBITED', 'Yayoi Kusama'),
('Girl with Balloon',          2002, 'Street Art',  'Pochoir sur mur',          '100 x 75 cm',  'Petite fille au ballon en forme de coeur, Londres.',              250000.00,  'FOR_SALE',  'Banksy');

-- ---------------------------------------------------------------------
-- 7. TAGS D'OEUVRES
-- ---------------------------------------------------------------------
INSERT INTO artwork_tag (artwork_title, tag_name) VALUES
('Mona Lisa',                 'portrait'),
('Mona Lisa',                 'renaissance'),
('La Cene',                   'religieux'),
('La Nuit etoilee',           'paysage'),
('La Nuit etoilee',           'post-impressionnisme'),
('Les Tournesols',            'nature-morte'),
('Nympheas',                  'paysage'),
('Nympheas',                  'impressionnisme'),
('Impression, soleil levant', 'impressionnisme'),
('Guernica',                  'politique'),
('Guernica',                  'cubisme'),
('Les Demoiselles d''Avignon','cubisme'),
('Les Deux Fridas',           'autoportrait'),
('Les Deux Fridas',           'surrealisme'),
('Le Penseur',                'bronze'),
('La Persistance de la memoire','surrealisme'),
('La Danse',                  'fauvisme'),
('Marilyn Diptych',           'pop-art'),
('Marilyn Diptych',           'serigraphie'),
('Infinity Mirror Room',      'immersif'),
('Infinity Mirror Room',      'contemporain'),
('Girl with Balloon',         'street-art'),
('Girl with Balloon',         'pochoir');

-- ---------------------------------------------------------------------
-- 8. MEMBRES DE LA COMMUNAUTE (fictifs)
-- ---------------------------------------------------------------------
INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) VALUES
('Alice Dubois',     'alice.dubois@artconnect.fr',     1992, '+33-6-12345678', 'Paris',     'premium'),
('Hugo Lemoine',     'hugo.lemoine@artconnect.fr',     1988, '+33-7-22334455', 'Lyon',      'free'),
('Sofia Martinez',   'sofia.martinez@artconnect.es',   1995, '+34-612-345678', 'Madrid',    'premium'),
('Liam O''Connor',   'liam.oconnor@artconnect.uk',     1990, '+44-7700-900123','London',    'free'),
('Yuki Tanaka',      'yuki.tanaka@artconnect.jp',      1998, '+81-90-12345678','Tokyo',     'premium'),
('Emma Bernard',     'emma.bernard@artconnect.fr',     2001, '+33-6-87654321', 'Marseille', 'free'),
('Noah Schmidt',     'noah.schmidt@artconnect.de',     1985, '+49-151-2345678','Berlin',    'premium'),
('Camille Petit',    'camille.petit@artconnect.fr',    1996, '+33-6-98765432', 'Paris',     'free'),
('Marco Rossi',      'marco.rossi@artconnect.it',      1993, '+39-333-1234567','Rome',      'premium'),
('Aisha Khan',       'aisha.khan@artconnect.uk',       1991, '+44-7700-900456','Manchester','free');

-- ---------------------------------------------------------------------
-- 9. WORKSHOPS (dates > 2026-05-19 pour passer le trigger)
-- ---------------------------------------------------------------------
INSERT INTO workshop (title, date, duration_minutes, max_participants, price, instructor_name, location, description, level) VALUES
('Initiation a l''huile',         '2026-06-15 10:00:00', 180, 12, 80.00,  'Claude Monet',     'Atelier Giverny - Salle 1',     'Premiers gestes de la peinture a l''huile, palette impressionniste.', 'beginner'),
('Maitriser la lumiere',          '2026-06-28 14:00:00', 240, 10, 120.00, 'Vincent van Gogh', 'Centre Pompidou - Studio B',    'Travailler les contrastes de lumiere et la touche post-impressionniste.','intermediate'),
('Sculpter le bronze',            '2026-07-12 09:30:00', 360, 8,  250.00, 'Auguste Rodin',    'Musee Rodin - Atelier nord',    'Initiation au modelage et tirage en bronze.',                          'advanced'),
('Pop Art et serigraphie',        '2026-07-25 13:00:00', 180, 15, 90.00,  'Andy Warhol',      'Tate Modern - Workshop Hall',    'Realiser sa propre serigraphie pop : Marilyn, soupe, banane.',         'beginner'),
('Surrealisme et reve',           '2026-08-08 10:00:00', 210, 12, 110.00, 'Salvador Dali',    'Museo Reina Sofia - Sala 4',    'Techniques d''ecriture automatique et compositions surrealistes.',     'intermediate'),
('Performance et presence',       '2026-09-20 14:00:00', 180, 10, 150.00, 'Marina Abramovic', 'MoMA - Performance Space',      'Travail sur la duree, l''attention et la presence en performance.',    'advanced');

-- ---------------------------------------------------------------------
-- 10. BOOKINGS (reservations de membres aux ateliers)
-- ---------------------------------------------------------------------
-- On utilise des sous-requetes pour resoudre les ids des membres et workshops.
INSERT INTO booking (member_id, workshop_id, booking_date, status) VALUES
((SELECT id FROM community_member WHERE name='Alice Dubois'),  (SELECT id FROM workshop WHERE title='Initiation a l''huile'),    '2026-05-12','CONFIRMED'),
((SELECT id FROM community_member WHERE name='Hugo Lemoine'),  (SELECT id FROM workshop WHERE title='Initiation a l''huile'),    '2026-05-14','CONFIRMED'),
((SELECT id FROM community_member WHERE name='Emma Bernard'),  (SELECT id FROM workshop WHERE title='Maitriser la lumiere'),     '2026-05-15','CONFIRMED'),
((SELECT id FROM community_member WHERE name='Camille Petit'), (SELECT id FROM workshop WHERE title='Maitriser la lumiere'),     '2026-05-16','CONFIRMED'),
((SELECT id FROM community_member WHERE name='Marco Rossi'),   (SELECT id FROM workshop WHERE title='Sculpter le bronze'),       '2026-05-10','CONFIRMED'),
((SELECT id FROM community_member WHERE name='Sofia Martinez'),(SELECT id FROM workshop WHERE title='Pop Art et serigraphie'),   '2026-05-17','CONFIRMED'),
((SELECT id FROM community_member WHERE name='Liam O''Connor'),(SELECT id FROM workshop WHERE title='Pop Art et serigraphie'),   '2026-05-18','CONFIRMED'),
((SELECT id FROM community_member WHERE name='Noah Schmidt'),  (SELECT id FROM workshop WHERE title='Surrealisme et reve'),      '2026-05-09','CONFIRMED'),
((SELECT id FROM community_member WHERE name='Yuki Tanaka'),   (SELECT id FROM workshop WHERE title='Performance et presence'),  '2026-05-11','CONFIRMED'),
((SELECT id FROM community_member WHERE name='Aisha Khan'),    (SELECT id FROM workshop WHERE title='Performance et presence'),  '2026-05-13','PENDING');

-- ---------------------------------------------------------------------
-- 11. REVIEWS (avis sur oeuvres)
-- ---------------------------------------------------------------------
INSERT INTO review (member_id, artwork_title, rating, comment, review_date) VALUES
((SELECT id FROM community_member WHERE name='Alice Dubois'),    'Mona Lisa',                  5, 'Toujours saisissant, peu importe le nombre de visites.',           '2026-04-10'),
((SELECT id FROM community_member WHERE name='Hugo Lemoine'),    'La Nuit etoilee',            5, 'Les bleus tourbillonnent vraiment quand on s''approche.',          '2026-04-12'),
((SELECT id FROM community_member WHERE name='Sofia Martinez'),  'Guernica',                   5, 'Impressionnant en vrai, on ne peut pas detacher les yeux.',        '2026-04-15'),
((SELECT id FROM community_member WHERE name='Emma Bernard'),    'Nympheas',                   4, 'Magnifique - le format change tout l''experience.',                '2026-04-20'),
((SELECT id FROM community_member WHERE name='Liam O''Connor'),  'Marilyn Diptych',            4, 'Iconique. Aurait souhaite plus de contexte historique.',           '2026-04-25'),
((SELECT id FROM community_member WHERE name='Yuki Tanaka'),     'Infinity Mirror Room',       5, 'Experience immersive incroyable. Reserver tot.',                   '2026-05-01'),
((SELECT id FROM community_member WHERE name='Marco Rossi'),     'Le Penseur',                 5, 'La presence du bronze est palpable.',                              '2026-05-03'),
((SELECT id FROM community_member WHERE name='Noah Schmidt'),    'La Persistance de la memoire',5,'Plus petit que je ne pensais mais hypnotisant.',                    '2026-05-05'),
((SELECT id FROM community_member WHERE name='Camille Petit'),   'Les Deux Fridas',            5, 'Tres emouvant, double-portrait magistral.',                        '2026-05-08'),
((SELECT id FROM community_member WHERE name='Aisha Khan'),      'Girl with Balloon',          3, 'Sympa, mais surprise par la taille modeste.',                      '2026-05-10');

-- =====================================================================
-- Fin du script d'insertion.
-- Pour controler rapidement :
--   SELECT COUNT(*) FROM artist;          -- 12
--   SELECT COUNT(*) FROM gallery;         -- 6
--   SELECT COUNT(*) FROM artwork;         -- 15
--   SELECT COUNT(*) FROM exhibition;      -- 6
--   SELECT COUNT(*) FROM community_member;-- 10
--   SELECT COUNT(*) FROM workshop;        -- 6
--   SELECT COUNT(*) FROM booking;         -- 10
--   SELECT COUNT(*) FROM review;          -- 10
-- =====================================================================
