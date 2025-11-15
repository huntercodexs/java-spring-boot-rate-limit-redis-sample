# java-spring-boot-rate-limit-redis-sample
Sample application to demonstration rate limit + redis management

### Details

In this example, we demonstrate how to implement a rate limiting mechanism using Redis in a Spring Boot application.
The application limits the number of requests a client can make to a specific endpoint within a defined time window.
The rate limiting is based on a key provided in the request header (e.g., "X-Client-Id").
It is possible to customize the limit and time window as needed and limit the processing of requests based on the 
key by user, client, or any other identifier.


### How to test

- Allowed

```shell
curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userA", "content": "test1"}'
# Result: HTTP 200 OK
```

- Allowed

```shell
curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "2", "userId": "userA", "content": "test2"}'
# Result: HTTP 200 OK
```

- Denied - Limit Exceeded

```shell
curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "3", "userId": "userA", "content": "test3"}'
# Result: HTTP 429 Too Many Requests - Message Limit exceeded for key 'userA'...
```

- Allowed for different user

```shell
curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "4", "userId": "userB", "content": "test4"}'
# Result: HTTP 200 OK
```

Samples Results

```text


user@host$ curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userA", "content": "test1"}'
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 48
Date: Sat, 15 Nov 2025 19:35:40 GMT

Message processed successfully for userId: userA

user@host$ curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userA", "content": "test1"}'
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 48
Date: Sat, 15 Nov 2025 19:35:43 GMT

Message processed successfully for userId: userA

user@host$ curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userA", "content": "test1"}'
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Sat, 15 Nov 2025 19:35:44 GMT

Limit of requests exceeded. Please try again later.

user@host$ curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userA", "content": "test1"}'
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Sat, 15 Nov 2025 19:35:45 GMT

Limit of requests exceeded. Please try again later.

user@host$ curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userA", "content": "test1"}'
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Sat, 15 Nov 2025 19:35:46 GMT

Limit of requests exceeded. Please try again later.

user@host$ curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userB", "content": "test1"}'
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 48
Date: Sat, 15 Nov 2025 19:35:50 GMT

Message processed successfully for userId: userB

user@host$ curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userB", "content": "test1"}'
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 48
Date: Sat, 15 Nov 2025 19:35:52 GMT

Message processed successfully for userId: userB

user@host$ curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userB", "content": "test1"}'
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Sat, 15 Nov 2025 19:35:53 GMT

Limit of requests exceeded. Please try again later.

user@host$ curl -i -X POST http://localhost:8080/simulate-queue-process -H "Content-Type: application/json" -d '{"id": "1", "userId": "userC", "content": "test1"}'
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 48
Date: Sat, 15 Nov 2025 19:35:57 GMT

Message processed successfully for userId: userC
```