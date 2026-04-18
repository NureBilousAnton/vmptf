const router = require('express').Router();
const db = require('../db');
const authMiddleware = require('../middleware/auth');
const { cacheMiddleware, invalidate } = require('../middleware/cache');

// Base join query to select articles
const BASE_SQL = `
  SELECT a.*, u.username AS author, c.name AS category
  FROM articles a
  LEFT JOIN users u ON u.id = a.author_id
  LEFT JOIN categories c ON c.id = a.category_id
`;

router.get('/', cacheMiddleware, (req, res) => {
    const { search, category } = req.query;
    let sql = BASE_SQL + ' WHERE 1=1';
    const params = [];

    if (search) {
        sql += ' AND (a.title LIKE ? OR a.content LIKE ?)';
        params.push(`%${search}%`, `%${search}%`);
    }
    if (category) {
        sql += ' AND a.category_id = ?';
        params.push(Number(category));
    }

    sql += ' ORDER BY a.created_at DESC';
    res.json(db.prepare(sql).all(...params));
});

router.get('/:id', cacheMiddleware, (req, res) => {
    const article = db.prepare(BASE_SQL + ' WHERE a.id = ?').get(req.params.id);
    if (!article) return res.status(404).json({ error: 'Not found' });

    const comments = db.prepare(`
    SELECT c.*, u.username AS author FROM comments c
    LEFT JOIN users u ON u.id = c.author_id
    WHERE c.article_id = ? ORDER BY c.created_at
  `).all(req.params.id);

    res.json({ ...article, comments });
});

router.post('/', authMiddleware, (req, res) => {
    const { title, content, category_id } = req.body;
    if (!title || !content) return res.status(400).json({ error: 'title та content обовʼязкові' });
    const result = db.prepare(
        'INSERT INTO articles (title, content, author_id, category_id) VALUES (?, ?, ?, ?)'
    ).run(title, content, req.user.id, category_id ?? null);
    invalidate('/api/articles'); // do not forget to invalidate the cache
    res.status(201).json({ id: result.lastInsertRowid });
});

module.exports = router;

