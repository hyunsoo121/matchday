-- ==============================
-- SPORT
-- ==============================
INSERT INTO sport (code, name_ko, name_en, category, display_order) VALUES
  ('football',    '축구',    'Football',    'sports',      1),
  ('basketball',  '농구',    'Basketball',  'sports',      2),
  ('baseball',    '야구',    'Baseball',    'sports',      3),
  ('lol',         '리그 오브 레전드', 'League of Legends', 'esports', 4),
  ('valorant',    '발로란트', 'Valorant',    'esports',     5),
  ('f1',          'F1',      'Formula 1',   'motorsports', 6);

-- ==============================
-- LEAGUE
-- ==============================

-- 축구
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'epl',          '프리미어리그',     'Premier League',       'England',  1 FROM sport WHERE code = 'football';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'laliga',       '라리가',           'La Liga',              'Spain',    2 FROM sport WHERE code = 'football';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'serie_a',      '세리에A',          'Serie A',              'Italy',    3 FROM sport WHERE code = 'football';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'bundesliga',   '분데스리가',       'Bundesliga',           'Germany',  4 FROM sport WHERE code = 'football';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'ligue1',       '리그 1',           'Ligue 1',              'France',   5 FROM sport WHERE code = 'football';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'ucl',          'UEFA 챔피언스리그', 'UEFA Champions League', NULL,      6 FROM sport WHERE code = 'football';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'kleague1',     'K리그 1',          'K League 1',           'Korea',    7 FROM sport WHERE code = 'football';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'kleague2',     'K리그 2',          'K League 2',           'Korea',    8 FROM sport WHERE code = 'football';

-- 농구
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'nba',  'NBA',  'NBA',  'USA',   1 FROM sport WHERE code = 'basketball';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'kbl',  'KBL',  'KBL',  'Korea', 2 FROM sport WHERE code = 'basketball';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'wkbl', 'WKBL', 'WKBL', 'Korea', 3 FROM sport WHERE code = 'basketball';

-- 야구
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'mlb', 'MLB', 'MLB', 'USA',   1 FROM sport WHERE code = 'baseball';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'kbo', 'KBO', 'KBO', 'Korea', 2 FROM sport WHERE code = 'baseball';

-- LoL
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'lck',          'LCK',          'LCK',              'Korea',  1 FROM sport WHERE code = 'lol';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'lpl',          'LPL',          'LPL',              'China',  2 FROM sport WHERE code = 'lol';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'lol_worlds',   '월드 챔피언십', 'World Championship', NULL,   3 FROM sport WHERE code = 'lol';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'lol_msi',      'MSI',          'Mid-Season Invitational', NULL, 4 FROM sport WHERE code = 'lol';
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'lol_first_stand', 'First Stand', 'First Stand',    NULL,     5 FROM sport WHERE code = 'lol';

-- 발로란트
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'vct', 'VCT', 'VCT', NULL, 1 FROM sport WHERE code = 'valorant';

-- F1
INSERT INTO league (sport_id, code, name_ko, name_en, country, display_order)
SELECT id, 'f1_season', 'F1 시즌', 'F1 Season', NULL, 1 FROM sport WHERE code = 'f1';
