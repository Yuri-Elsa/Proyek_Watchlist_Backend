# WatchList Backend — Ktor + PostgreSQL

REST API untuk aplikasi WatchList Android. Dibangun dengan **Kotlin + Ktor + Exposed ORM + PostgreSQL**.

---

## Teknologi

| Komponen | Versi |
|---|---|
| Kotlin | 2.0.0 |
| Ktor Server (Netty) | 2.3.12 |
| Exposed ORM | 0.52.0 |
| PostgreSQL Driver | 42.7.3 |
| HikariCP | 5.1.0 |
| BCrypt | 0.10.2 |
| Java JWT (Auth0) | 4.4.0 |
| Java | 17 |

---

## Struktur Proyek

```
src/main/kotlin/org/delcom/watchlist/
├── Application.kt
├── auth/
│   ├── model/AuthModels.kt
│   ├── repository/AuthRepository.kt
│   ├── route/AuthRoute.kt          # POST /auth/register, /login, /logout, /refresh
│   └── table/AuthTokensTable.kt
├── user/
│   ├── model/UserModels.kt
│   ├── repository/UserRepository.kt
│   ├── route/UserRoute.kt          # GET/PUT /users/me, /me/password, /me/about, /me/photo
│   └── table/UsersTable.kt
├── todo/
│   ├── model/TodoModels.kt
│   ├── repository/TodoRepository.kt
│   ├── route/TodoRoute.kt          # CRUD /todos, /todos/{id}/cover, /todos/stats
│   └── table/TodosTable.kt
└── common/
    ├── config/AppConfig.kt
    ├── plugins/                    # Auth, CORS, DB, Routing, Serialization, StatusPages
    └── util/                       # ApiResponse, ImageService, UUIDUtils
```

---

## Quick Start

### Docker Compose (Direkomendasikan)

```bash
cd watchlist-backend
docker compose up -d
docker compose logs -f api
# API: http://localhost:8080
```

Opsional — buka pgAdmin di http://localhost:5050:
```bash
docker compose --profile dev up -d
# email: admin@watchlist.local  |  password: admin
```

### Jalankan Lokal

```bash
# Prasyarat: JDK 17+, PostgreSQL running lokal
createdb watchlist

export DATABASE_URL="jdbc:postgresql://localhost:5432/watchlist"
export DATABASE_USER="postgres"
export DATABASE_PASSWORD="postgres"
export JWT_SECRET="ganti-dengan-string-panjang-acak"

./gradlew run
```

---

## Konfigurasi (Environment Variables)

| Env Var | Default | Keterangan |
|---|---|---|
| `PORT` | `8080` | Port server |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/watchlist` | JDBC URL |
| `DATABASE_USER` | `postgres` | Username DB |
| `DATABASE_PASSWORD` | `postgres` | Password DB |
| `JWT_SECRET` | `change-me-...` | **Wajib diganti di production!** |
| `UPLOAD_DIR` | `uploads` | Direktori gambar |

---

## API Endpoints

Header auth wajib (kecuali `/auth/*` dan `/images/*`):
```
Authorization: Bearer <authToken>
```

### Auth
| Method | Path | Deskripsi |
|--------|------|-----------|
| POST | `/auth/register` | Daftar: `{name, username, password}` |
| POST | `/auth/login` | Login: `{username, password}` → `{authToken, refreshToken}` |
| DELETE | `/auth/logout` | Logout: `{authToken}` |
| POST | `/auth/refresh` | Rotate token: `{authToken, refreshToken}` |

### Users
| Method | Path | Deskripsi |
|--------|------|-----------|
| GET | `/users/me` | Data profil |
| PUT | `/users/me` | Update `{name, username}` |
| PUT | `/users/me/password` | Ganti `{password, newPassword}` |
| PUT | `/users/me/about` | Update `{about}` |
| PUT | `/users/me/photo` | Upload foto (multipart, field: `file`) |
| GET | `/images/users/{userId}` | Serve foto profil (publik) |

### Todos (Film)
| Method | Path | Deskripsi |
|--------|------|-----------|
| GET | `/todos` | List film — query: `page`, `perPage`, `search`, `urgency`, `isDone` |
| POST | `/todos` | Tambah: `{title, description, isDone, urgency}` |
| GET | `/todos/stats` | Statistik total/done/pending |
| GET | `/todos/{id}` | Detail film |
| PUT | `/todos/{id}` | Edit film |
| DELETE | `/todos/{id}` | Hapus film + poster |
| PUT | `/todos/{id}/cover` | Upload poster (multipart, field: `file`) |
| GET | `/images/todos/{id}` | Serve poster (publik) |

### Nilai Urgency
| Value | Status di Android |
|---|---|
| `low` | 🔵 Sedang Ditonton |
| `medium` | 🟣 Belum Ditonton |
| `high` | 🟢 Sudah Ditonton |

---

## Build

```bash
# Build fat JAR
./gradlew buildFatJar
# Output: build/libs/watchlist-backend-all.jar

# Jalankan langsung
java -jar build/libs/watchlist-backend-all.jar
```

---

## Catatan Production

- **JWT Secret** wajib diganti — minimal 32 karakter acak
- **CORS** saat ini mengizinkan semua host — batasi sesuai domain di production
- **Gambar disimpan lokal** di `uploads/` — pertimbangkan S3/object storage untuk production
- **Tabel dibuat otomatis** saat server pertama jalan (Exposed auto-migrate)
