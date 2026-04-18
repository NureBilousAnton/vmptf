const Database = require('better-sqlite3');
const bcrypt = require('bcryptjs');
const db = new Database('blog.sqlite');

db.exec(`
  CREATE TABLE IF NOT EXISTS categories (
    id   INTEGER PRIMARY KEY,
    name TEXT    NOT NULL UNIQUE
  );

  CREATE TABLE IF NOT EXISTS users (
    id       INTEGER PRIMARY KEY,
    username TEXT    NOT NULL UNIQUE,
    password TEXT    NOT NULL
  );

  CREATE TABLE IF NOT EXISTS articles (
    id          INTEGER PRIMARY KEY,
    title       TEXT    NOT NULL,
    content     TEXT    NOT NULL,
    author_id   INTEGER REFERENCES users(id),
    category_id INTEGER REFERENCES categories(id),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS comments (
    id         INTEGER PRIMARY KEY,
    article_id INTEGER NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    author_id  INTEGER REFERENCES users(id),
    text       TEXT    NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
  );
`);

// Only if the database is empty
if (db.prepare('SELECT COUNT(*) as n FROM categories').get().n === 0) {
    ['Технології', 'Наука', 'Культура'].forEach(name =>
        db.prepare('INSERT INTO categories (name) VALUES (?)').run(name)
    );

    const hash = bcrypt.hashSync('password', 10);
    const userId = db.prepare('INSERT INTO users (username, password) VALUES (?, ?)').run('admin', hash).lastInsertRowid;
    const catId = db.prepare("SELECT id FROM categories WHERE name = 'Технології'").get().id;

    db.prepare('INSERT INTO articles (title, content, author_id, category_id) VALUES (?, ?, ?, ?)')
        .run('Перша стаття', 'Текст першої статті про технології.', userId, catId);
}

module.exports = db;
