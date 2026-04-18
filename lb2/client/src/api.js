const BASE = '/api';
const token = () => localStorage.getItem('token');
const authHeader = () => ({ Authorization: `Bearer ${token()}` });

export const getArticles = (params = {}) =>
    fetch(`${BASE}/articles?` + new URLSearchParams(params)).then(r => r.json());

export const getArticle = id =>
    fetch(`${BASE}/articles/${id}`).then(r => r.json());

export const getCategories = () =>
    fetch(`${BASE}/categories`).then(r => r.json());

export const createArticle = body =>
    fetch(`${BASE}/articles`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeader() },
        body: JSON.stringify(body),
    }).then(r => r.json());

export const addComment = (articleId, body) =>
    fetch(`${BASE}/articles/${articleId}/comments`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeader() },
        body: JSON.stringify(body),
    }).then(r => r.json());

export const login = body =>
    fetch(`${BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    }).then(r => r.json());

export const register = body =>
    fetch(`${BASE}/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    }).then(r => r.json());
