# monolith-architecture
***
## WHAT IS A MONOLITH ARCHITECTURE?
- It is a software development approach that leverages a single, large codebase instead of multiple, isolated, small codebases.
***
## Swagger Ui
- Documenting REST APIs with Swagger
  - http://localhost:8080/swagger-ui.html
***
### Database Connection For Test 
- JDBC URL: jdbc:h2:mem:testdb
- UserName: sa
- Password: 
***
### Profile
- Add Profile using Maven Profile
- By default environment **test**
```
spring:
  profiles:
    active: @spring.profiles.active@
```
- To package the dev profile just run 
```
mvn clean package -Pdev
```
***
### Things to add
1. https://wimdeblauwe.github.io/error-handling-spring-boot-starter/4.3.0/#goal --> Try this to replace @RestControllerAdvice
2. https://www.datafaker.net/documentation/getting-started/ --> Add Data Faker
3. https://medium.com/@pavanapriya.u/implementing-a-two-level-cache-with-spring-boot-86a681e942ae --> for caching purpose
4. https://medium.com/@dixitsatish34/how-to-improve-webclient-response-time-in-spring-boot-3c0c898f06b4 --> improve response time for webclient
***
### TODOS
1. Add projection using @Query
***
### Virtual Threads and its pitfall
- Note: Pining issue block virtual and platform thread (synchronized) 
