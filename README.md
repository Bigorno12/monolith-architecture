# modular-monolith-architecture
***
## WHAT IS A MODULAR MONOLITH ARCHITECTURE?
- It is a software development approach that leverages a single, large codebase instead of multiple, isolated, small codebases.
- The codebase is logically divided into separate modules that can be developed, tested, and maintained independently.
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
***
### TODOS
1. Add projection using @Query
