import { useState } from 'react';
import { login, register } from '../api';

export default function Login() {
    const [form, setForm] = useState({ username: '', password: '' });
    const [msg, setMsg] = useState('');

    const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }));

    const handle = async (fn) => {
        const data = await fn(form);
        if (data.token) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify(data.user));
            setMsg(`Вітаємо, ${data.user.username}!`);
        } else {
            setMsg(data.error ?? 'Помилка');
        }
    };

    return (
        <div>
            <h2>Вхід / Реєстрація</h2>
            <input placeholder="Логін" value={form.username} onChange={set('username')} />
            <input placeholder="Пароль" value={form.password} onChange={set('password')} type="password" />
            <button onClick={() => handle(login)}>Увійти</button>
            <button onClick={() => handle(register)}>Реєстрація</button>
            {msg && <p>{msg}</p>}
        </div>
    );
}
