const NodeCache = require('node-cache');

const cache = new NodeCache({ stdTTL: 60, checkperiod: 120 });

/**
 * Express middleware: caches responses of GET requests by URL (with query params).
 * X-Cache: HIT  - returned from cache
 * X-Cache: MISS - fetched and cached from DB
 */
const cacheMiddleware = (req, res, next) => {
    const key = req.originalUrl;
    const cached = cache.get(key);

    if (cached !== undefined) {
        res.setHeader('X-Cache', 'HIT');
        return res.json(cached);
    }

    // Intercept res.json to save data before sending
    const originalJson = res.json.bind(res); // bind ensures `this` stays correct
    res.json = (data) => {
        cache.set(key, data);
        res.setHeader('X-Cache', 'MISS');
        return originalJson(data);
    };

    next();
};

/**
 * Deletes all keys that start with prefix from cache.
 * Call after any mutation (POST, PUT, DELETE).
 */
const invalidate = (prefix) => {
    cache.keys()
        .filter(k => k.startsWith(prefix))
        .forEach(k => cache.del(k));
};

module.exports = { cacheMiddleware, invalidate };
