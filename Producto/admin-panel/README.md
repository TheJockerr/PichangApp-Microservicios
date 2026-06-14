# PichangApp — Panel de Administración

Panel web de administración para PichangApp. Gestión de usuarios, karma y eventos deportivos.

## Stack

- React 18 + Vite 5
- TanStack React Query v5
- React Router v6
- Recharts (gráfico de eventos)
- Lucide React (iconografía)
- CSS puro con variables de diseño (sin UI framework)

## Arranque local

```bash
npm install
npm run dev       # http://localhost:5173
npm run build     # genera dist/ para Vercel
```

Requiere `Producto/admin-panel/.env`:
```
VITE_API_URL=http://localhost:8080
```

## Estructura

```
src/
├── api/          # authApi, usersApi, eventsApi, karmaApi, client.js (axios + interceptor JWT)
├── components/   # StatCard, UserAvatar, KarmaBadge, SportBadge, StatusBadge,
│                 # ConfirmModal, EmptyState, LoadingSpinner, Navbar, Topbar, ProtectedRoute
├── hooks/        # AuthContext.jsx (estado React del token), useAuth.js (re-export)
├── pages/        # LoginPage, DashboardPage, UsersPage, UserDetailPage, EventsPage
└── styles/       # variables.css (tokens), animations.css (keyframes)
```

## Credenciales por defecto

| Campo | Valor |
|---|---|
| Correo | `admin@pichangapp.cl` |
| Contraseña | `Admin@2024!` |
