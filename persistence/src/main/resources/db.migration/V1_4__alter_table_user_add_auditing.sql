# add new columns
ALTER TABLE _user
    ADD created_by varchar(255),
    ADD modified_by varchar(255);

ALTER TABLE comments
    ADD created_by varchar(255),
    ADD modified_by varchar(255);

ALTER TABLE post
    ADD created_by varchar(255),
    ADD modified_by varchar(255);

ALTER TABLE todo
    ADD created_by varchar(255),
    ADD modified_by varchar(255);