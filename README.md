# java-spring-boot-rate-limit-redis-sample
Sample application to demonstration rate limit + redis management

### How to test

```shell
for i in {1..12}; do curl -i http://localhost:8080/api/test; echo ""; done
```

Result

```text
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 10
Date: Thu, 06 Nov 2025 01:24:43 GMT

Request OK
HTTP/1.1 429 
Content-Length: 35
Date: Thu, 06 Nov 2025 01:24:43 GMT

Too many requests - try again later
HTTP/1.1 429 
Content-Length: 35
Date: Thu, 06 Nov 2025 01:24:43 GMT

Too many requests - try again later
```