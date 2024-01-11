# Installation
## mariadb env variable requirements 
MARIADB_ROOT_PASSWORD

MARIADB_DATABASE

## Application env requirements 
APPS_MARIADB_HOST

APPS_MARIADB_PORT

APPS_MARIADBDB_DBNAME

APPS_MARIADBDB_ROOT_PASSWORD


# Documentation

## Register

### Root Page Before Login

Endpoint : 
- GET /
- GET /api


Response Body (Success) :

```json
{
  "data" : "Hello, guest",
  "errors" : null
}
```

### Register User

Endpoint : POST /api/users

Request Body :

```json
{
  "first_name" : "kupipancongid",
  "last_name" : "kupipancongid",
  "username" : "kupipancongid",
  "email" : "idkupipancong@gmail.com",
  "password" : "kupipancongid",
  "password_confirmation" : "kupipancongid"
}
```

Response Body (Success) :

```json
{
  "data" : "OK",
  "errors" : null
}
```

Response Body (Failed) :

```json
{
  "data": null,
  "errors" : "email must not blank, username already exist"
}
```

### Email Verification

Endpoint : GET /api/users?verificatin-code=XXX

Response Body (Success) :

```json
{
  "data" : "OK",
  "errors" : null
}
```

Response Body (Failed) :

```json
{
  "data": null,
  "errors" : "invalid token"
}
```

Response Body (Failed) :

```json
{
  "data": null,
  "errors" : "User verified"
}
```
Response Body (Failed) :

```json
{
  "data": null,
  "errors" : "Token not found"
}
```

## Login

### Login User

Endpoint : POST /api/auth/login

Request Body :

```json
{
  "email_or_username" : "idkupipancong@gmail.com",
  "password" : "secret" 
}
```

Response Body (Success) :

```json
{
  "data": {
    "access_token": "xxx",
    "refresh_token": "yyy"
  },
  "errors": null
}
```

Response Body (Failed, 401) :

```json
{
  "errors" : "Wrong credentials"
}
```

### Refresh Token

Endpoint : POST /api/auth/refresh

Request Header :

- X-API-ACCESS-TOKEN : Token (Mandatory)
- X-API-REFRESH-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data": {
    "access_token": "xxx",
    "refresh_token": "yyy"
  },
  "errors": null
}
```

Response Body (Failed, 401) :

```json
{
  "data": null,
  "errors" : "invalid token"
}
```

## Current User

### Root Page After Login

Endpoint :
- GET /
- GET /api

Request Header :

- X-API-ACCESS-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data" : "Hello, kupipancongid",
  "errors" : null
}
```

### Get User

Endpoint : GET /api/users/current

Request Header :

- X-API-ACCESS-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data" : {
    "profile_picture" : null,
    "first_name" : "kupipancongid",
    "last_name" : "kupipancongid",
    "username" : "kupipancongid",
    "email" : "idkupipancong@gmail.com",
    "phone_number" : null,
    "province_id" : null,
    "city_id" : null,
    "sub_district_id_id" : null,
    "village_id" : null,
    "address" : null
  },
  "errors": null
}
```

Response Body (Failed, 401) :

```json
{
  "data": null,
  "errors" : "Unauthorized"
}
```

### Update User

Endpoint : PATCH /api/users/current

Request Header :

- X-API-ACCESS-TOKEN : Token (Mandatory)

Request Body :

```json
{
  "profile_picture" : "photo.jpg",
  "first_name" : "kupipancongid",
  "last_name" : "kupipancongid",
  "username" : "kupipancongid",
  "email" : "idkupipancong@gmail.com",
  "phone_number" : "+62xxx",
  "province_id" : "X",
  "city_id" : "X",
  "sub_district_id_id" : "X",
  "village_id" : "X",
  "address" : "X"
}
```

Response Body (Success) :

```json
{
  "data" : {
    "profile_picture" : "photo.jpg",
    "first_name" : "kupipancongid",
    "last_name" : "kupipancongid",
    "username" : "kupipancongid",
    "email" : "idkupipancong@gmail.com",
    "phone_number" : "+62xxx",
    "province_id" : "X",
    "city_id" : "X",
    "sub_district_id_id" : "X",
    "village_id" : "X",
    "address" : "X"
  },
  "errors" : "Unauthorized"
}
```

Response Body (Failed, 401) :

```json
{
  "data": null,
  "errors" : "Unauthorized"
}
```

## Logout

### Logout User

Endpoint : DELETE /api/auth/logout

Request Header :

- X-API-ACCESS-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
  "data" : "OK",
  "errors" : null
}
```