# java-spring-boot-rate-limit-redis-sample
Sample application to demonstration rate limit + redis management

### How to test

```shell
for i in {1..10}; do sleep 1; curl -i -H "X-Client-Id: client-1" http://localhost:8080/api/limited; echo; done
```

Result

```text
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 30
Date: Fri, 07 Nov 2025 22:26:53 GMT

Request Allowed. Limit: 3/10s.
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 30
Date: Fri, 07 Nov 2025 22:26:54 GMT

Request Allowed. Limit: 3/10s.
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 30
Date: Fri, 07 Nov 2025 22:26:55 GMT

Request Allowed. Limit: 3/10s.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 22:26:56 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 22:26:57 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 22:26:58 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 22:26:59 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 22:27:00 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 22:27:01 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 22:27:02 GMT

Limite de requisições excedido. Tente novamente mais tarde.

```