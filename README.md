# Budbørsen - Auksjonsapplikasjon

Dette prosjektet er en fullstack webapplikasjon utviklet. Applikasjonen lar brukere opprette auksjoner, laste opp bilder av gjenstander, og legge inn bud i sanntid.

## Teknologi

* **Backend:** Java 21, Spring Boot 3, Spring Security (JWT)
* **Database:** SQLite
* **Frontend:** Vue 3 (Composition API), TypeScript, Pinia, Vite
* **Testing:** JUnit 5, Mockito

## Kom i gang

### Forutsetninger
* Java 17 eller nyere
* Node.js (v18+)
* Git

### Kjøre Backend
1.  Naviger til backend-mappen: `cd backend`
2.  Start applikasjonen: `./mvnw spring-boot:run`
3.  Swagger UI er tilgjengelig på: `http://localhost:8080/swagger-ui.html`

### Kjøre Frontend
1.  Naviger til frontend-mappen: `cd frontend`
2.  Installer avhengigheter: `npm install`
3.  Start utviklingsserver: `npm run dev`
4.  Åpne nettleseren på: `http://localhost:5173`


### Skal endres til dockercompose

## Testing
For å kjøre testene med coverage-rapport:
```bash
cd backend
./mvnw test
```

## Prosjekt struktur
