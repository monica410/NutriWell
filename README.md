# NutriWell

## Tech
Frontend: Vite/React
Backend: Springboot
Database: MySQL with JDBC

## How to run

### Database
Create a MySQL database named "nutriwell" locally.
The backend will automatically execute the SQL files under `backend/src/main/resources` when it starts.

### Frontend
1. cd into frontend
2. npm install
3. npm run dev
4. Go to the link given in the console output

### Backend (using intellij)
1. Add as Maven Project if IntelliJ has not recognized it.
2. Run the main method of `BackendApplication.java`
   - `Nurtriwell/backend/src/main/java/com/nutriwell/backend/BackendApplication.java`
   - Add your database password to "Environment variables" in the run configurations
     - DB_PASSWORD=your_mysql_password
3. To verify that the backend is working, open http://localhost:8080/api/hello.

## Sign up API

The Sprint 2 sign-up flow is available at `POST /api/auth/signup`.

Example request:

```json
{
  "username": "alex",
  "email": "alex@example.com",
  "password": "password123"
}
```

The endpoint validates the fields, rejects duplicate usernames or email addresses, hashes the password with BCrypt, and stores the new account in MySQL. A successful request returns HTTP `201 Created`.

The React sign-up page sends requests to `http://localhost:8080/api/auth/signup`. Start the backend before submitting the form.

## Current Sprint 2 progress

- Database setup and README: completed.
- Sign-up API and React sign-up form: implemented locally and ready for team testing.
- Personal nutrition goals: next backend feature.
- Water, exercise, and sleep tracking: planned after the account flow.
