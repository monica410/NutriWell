# nutriwell

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
3. to verify it's working: go to http://localhost:8080/api/hello to see Hello World!