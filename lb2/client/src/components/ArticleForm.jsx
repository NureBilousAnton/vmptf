import { useState, useEffect } from 'react';
import { createArticle, getCategories } from '../api';

export default function ArticleForm({ onCreated }) {
    const [form, setForm] = useState({ title: '', content: '', category_id: '' });
    const [cats, setCats] = useState([]);

    useEffect(() => { getCategories().then(setCats); }, []);

    const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }));

    const submit = async () => {
        const user = JSON.parse(localStorage.getItem('user') || 'null');
        const res = await createArticle({ ...form, author_id: user?.id });
        if (!res.error) { setForm({ title: '', content: '', category_id: '' }); onCreated?.(); }
        else alert(res.error);
    };

    return (
        <fieldset>
            <legend>Нова стаття</legend>
            <input placeholder="Заголовок" value={form.title} onChange={set('title')} />
            <textarea placeholder="Текст" value={form.content} onChange={set('content')} />
            <select value={form.category_id} onChange={set('category_id')}>
                <option value="">- Категорія -</option>
                {cats.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <button onClick={submit}>Додати</button>
        </fieldset>
    );
}
