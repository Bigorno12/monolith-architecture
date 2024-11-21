ALTER TABLE _user
    ADD created_date       TIMESTAMP(6),
    ADD last_modified_data TIMESTAMP(6);

ALTER TABLE todo
    ADD created_date       TIMESTAMP(6),
    ADD last_modified_data TIMESTAMP(6);

ALTER TABLE post
    ADD created_date       TIMESTAMP(6),
    ADD last_modified_data TIMESTAMP(6);

ALTER TABLE comments
    ADD created_date       TIMESTAMP(6),
    ADD last_modified_data TIMESTAMP(6);