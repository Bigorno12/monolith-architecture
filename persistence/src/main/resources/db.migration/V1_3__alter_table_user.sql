# Drop useless column
ALTER TABLE _user
DROP COLUMN name,
DROP COLUMN city,
DROP COLUMN company_bs,
DROP COLUMN company_catch_phrase,
DROP COLUMN company_name,
DROP COLUMN geo_lat,
DROP COLUMN geo_lng,
DROP COLUMN phone,
DROP COLUMN street,
DROP COLUMN website,
DROP COLUMN zipcode;

# alter username name to unique
ALTER TABLE _user
ADD UNIQUE (username);

# add new columns
ALTER TABLE _user
ADD COLUMN firstname VARCHAR(50) NOT NULL,
ADD COLUMN lastname VARCHAR(50) NOT NULL,
ADD COLUMN age INTEGER NOT NULL,
ADD COLUMN gender ENUM('FEMALE', 'MALE', 'OTHER') NOT NULL,
ADD COLUMN email VARCHAR(255) NOT NULL,
ADD COLUMN password VARCHAR(255) NOT NULL,
ADD COLUMN role ENUM('ADMIN', 'USER') NOT NULL;