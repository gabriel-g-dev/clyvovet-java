# 🐾 ClyvoVet Java API

API RESTful desenvolvida em Java com Spring Boot para gestão da jornada contínua de saúde do pet — FIAP Challenge 2026 em parceria com a Clyvo VET.

## 👥 Integrantes
- Gabriel Garcia — RM563298
- Andre Bellandi — RM564662
- Vitor Augusto — RM564227

## 📋 Descrição do Projeto

A ClyvoVet API resolve o problema da fragmentação na jornada de saúde animal. Hoje, tutores só interagem com clínicas em situações de urgência. Nossa solução oferece uma infraestrutura digital para organizar, lembrar e personalizar o cuidado preventivo e terapêutico do pet de forma contínua.

## 🚀 Tecnologias Utilizadas

- Java 17
- Spring Boot 3.3.5
- Spring Data JPA
- Banco H2 (in-memory)
- Swagger / OpenAPI (springdoc)
- Maven

## 📁 Estrutura do Projeto
src/main/java/br/com/fiap/clyvovet/
├── controller/
│   ├── TutorController.java
│   ├── PetController.java
│   ├── ConsultaController.java
│   └── VacinaController.java
├── model/
│   ├── Tutor.java
│   ├── Pet.java
│   ├── Consulta.java
│   └── Vacina.java
├── repository/
│   ├── TutorRepository.java
│   ├── PetRepository.java
│   ├── ConsultaRepository.java
│   └── VacinaRepository.java
└── exception/
└── GlobalExceptionHandler.java

## 🔗 Rotas da API

### Tutores
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | /api/tutores | Lista todos os tutores |
| GET | /api/tutores/{id} | Busca tutor por ID |
| GET | /api/tutores/email/{email} | Busca tutor por email |
| GET | /api/tutores/{id}/pets | Lista pets do tutor |
| POST | /api/tutores | Cadastra novo tutor |
| PUT | /api/tutores/{id} | Atualiza tutor |
| DELETE | /api/tutores/{id} | Remove tutor |

### Pets
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | /api/pets | Lista todos os pets |
| GET | /api/pets/{id} | Busca pet por ID |
| GET | /api/pets/especie/{especie} | Busca por espécie |
| GET | /api/pets/raca/{raca} | Busca por raça |
| GET | /api/pets/tutor/{tutorId} | Busca pets do tutor |
| POST | /api/pets | Cadastra novo pet |
| PUT | /api/pets/{id} | Atualiza pet |
| DELETE | /api/pets/{id} | Remove pet |

### Consultas
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | /api/consultas | Lista todas as consultas |
| GET | /api/consultas/{id} | Busca consulta por ID |
| GET | /api/consultas/veterinario/{nome} | Busca por veterinário |
| GET | /api/consultas/periodo?inicio=&fim= | Busca por período |
| GET | /api/consultas/pet/{petId} | Consultas do pet |
| POST | /api/consultas | Cadastra nova consulta |
| PUT | /api/consultas/{id} | Atualiza consulta |
| DELETE | /api/consultas/{id} | Remove consulta |

### Vacinas
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | /api/vacinas | Lista todas as vacinas |
| GET | /api/vacinas/{id} | Busca vacina por ID |
| GET | /api/vacinas/pendentes | Lista vacinas pendentes |
| GET | /api/vacinas/nome/{nome} | Busca por nome |
| GET | /api/vacinas/proximas?dias=30 | Próximas vacinas |
| GET | /api/vacinas/pet/{petId} | Vacinas do pet |
| POST | /api/vacinas | Cadastra nova vacina |
| PUT | /api/vacinas/{id} | Atualiza vacina |
| DELETE | /api/vacinas/{id} | Remove vacina |

## ⚙️ Como Rodar o Projeto

### Pré-requisitos
- Java 17
- Maven

### Instalação

**1. Clone o repositório:**
```bash
git clone https://github.com/gabriel-g-dev/clyvovet-java.git
cd clyvovet-java
```

**2. Execute o projeto:**
```bash
./mvnw spring-boot:run
```

**3. Acesse o Swagger:**
http://localhost:8080/swagger-ui/index.html

**4. Acesse o console H2:**
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:clyvovetdb
User: sa
Password: (vazio)

## 📊 Exemplos de Requisições

### Cadastrar um Tutor
```json
POST /api/tutores
{
  "nome": "João Silva",
  "email": "joao@email.com",
  "telefone": "11999999999"
}
```

### Cadastrar um Pet
```json
POST /api/pets
{
  "nome": "Rex",
  "especie": "Cachorro",
  "raca": "Labrador",
  "dataNascimento": "2020-03-15",
  "tutor": { "id": 1 }
}
```

### Cadastrar uma Consulta
```json
POST /api/consultas
{
  "data": "2026-05-10T10:00:00",
  "descricao": "Consulta de rotina",
  "veterinario": "Dr. Carlos",
  "observacoes": "Pet saudável",
  "pet": { "id": 1 }
}
```

### Cadastrar uma Vacina
```json
POST /api/vacinas
{
  "nome": "Antirrábica",
  "dataAplicacao": "2026-05-10",
  "proximaDose": "2027-05-10",
  "aplicada": true,
  "pet": { "id": 1 }
}
```
