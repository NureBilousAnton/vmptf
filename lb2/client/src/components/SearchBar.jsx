import { useEffect, useState } from 'react';
import { getCategories } from '../api';

export default function SearchBar({ onChange }) {
    const [search, setSearch] = useState('');
    const [category, setCategory] = useState('');
    const [cats, setCats] = useState([]);

    useEffect(() => { getCategories().then(setCats); }, []);

    return (
        <div>
            <input
                placeholder="Пошук..."
                value={search}
                onChange={e => setSearch(e.target.value)}
            />
            <select value={category} onChange={e => setCategory(e.target.value)}>
                <option value="">Всі категорії</option>
                {cats.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <button onClick={() => onChange({ search, category })}>Фільтр</button>
            <button onClick={() => { setSearch(''); setCategory(''); onChange({}); }}>Скинути</button>
        </div>
    );
}

