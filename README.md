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

## USDA food API

Set `USDA_API_KEY=your_usda_api_key` in your backend run configuration alongside `DB_PASSWORD`. 

Get a key from the [USDA API guide](https://fdc.nal.usda.gov/api-guide/).
This app searches Foundation, Survey (FNDDS), and Branded foods.

With the backend running:
- Search: `http://localhost:8080/api/foods/search?query=apple`
  - accepts optional `pageSize` (default 20 items) and `pageNumber` (default 1).
- Food details: `http://localhost:8080/api/foods/123` (replace 123 with an fdcId from the search results)

## Current Sprint 3 progress

- Database setup and README: completed.
- Sign-up API and React sign-up form: implemented locally and ready for team testing.
- Nutrition tracking backend + add food in frontend
- Personal nutrition goals: next backend feature.
- Water, exercise, and sleep tracking: planned after the account flow.
