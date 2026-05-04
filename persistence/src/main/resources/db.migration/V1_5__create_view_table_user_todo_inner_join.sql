CREATE VIEW user_todo_complete AS
    SELECT u.username, u.firstname, u.lastname, u.age, t.title, t.completed FROM _user AS u
    INNER JOIN todo AS t
    ON u.id = t.user_id
    WHERE t.completed = true;
